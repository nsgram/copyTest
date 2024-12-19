package com.aetna.asgwy.webmw.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.server.ServerWebExchange;

import com.aetna.asgwy.webmw.common.model.AVScanFileRequest;
import com.aetna.asgwy.webmw.common.model.AVScanFileResponse;
import com.aetna.asgwy.webmw.common.model.FileUploadDatail;
import com.aetna.asgwy.webmw.common.model.FileUploadResponse;
import com.aetna.asgwy.webmw.exception.AsgwyGlobalException;
import com.aetna.asgwy.webmw.util.AVScanFileUtil;
import com.aetna.framework.security.javacrypto.JavaCrypto;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AVScanFileService {

	@Value("${avscan.header.kid}")
	private String kid;

	@Value("${avscan.header.x-api-key}")
	private String apiKey;

	@Value("${avscan.upload.url}")
	private String uploadUrl;

	@Value("${gatewayapi.uri}")
	private String gatewayapiUri;

	@Value("${gatewayapi.basePath}")
	private String gatewayapiBasePath;

	@Value("${gatewayapi.fileURL}")
	private String fileURL;
	

	private final WebClient.Builder builder;

	public AVScanFileService(Builder builder) {
		this.builder = builder;
	}

	public ResponseEntity<FileUploadResponse> uploadFile(AVScanFileRequest avScanFileRequest,
			ServerWebExchange exchange) {
		log.info("AVScanFileService.uploadFile() start:: {}", avScanFileRequest.getUploadData().getQuoteId());
		// decode the encrypted token
		Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
		String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
		String avFilClean = (String) decodeJson.get("cvs_av_is_file_clean");
		String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
		// download file from AVScan and upload on AFS
		if (avFilClean.equalsIgnoreCase("Y") && !avFileRef.isBlank()) {
			log.info("uploaded file is clean");
			AVScanFileResponse avDownloadResponse = downloadAVScanFile(avFileRef);
			String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
			String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

			String tempDir = System.getProperty("java.io.tmpdir");

			// Write to a temporary file
			File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);
			try (FileOutputStream fos = new FileOutputStream(tempFile)) {
				String decodeUpladTxt = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);

				// Decode the Base64 string
				byte[] decodedAvFileBytes = Base64.getDecoder().decode(decodeUpladTxt);

				fos.write(decodedAvFileBytes);
				return uploadFileOnGateway(tempFile, avScanFileRequest.getUploadData(), exchange);

			} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				log.error("AVScanFileService.uploadFile() ::{}", e.getLocalizedMessage());
				throw new AsgwyGlobalException("AVScanFileService.uploadFile() ::" + e.getMessage());
			} finally {
				tempFile.delete();
			}

		} else {
			log.error("The uploaded file appears to be unsafe.");
			return new ResponseEntity<FileUploadResponse>(
					FileUploadResponse.builder().docNm(avOriginalFileName).fileUploadResponse("The uploaded file appears to be unsafe.").build(),
					HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	private Map<String, Object> decodeEncryptedToken(String token) {
		log.info("decodeEncryptedToken() start...");
		Map<String, Object> jsonObject = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
			// Decrypt JWE Token
			JWEObject jweObject = JWEObject.parse(token);
			jweObject.decrypt(new RSADecrypter(privateKey));
			String plaintext = jweObject.getPayload().toString();
			// Parse JWT
			SignedJWT signedJWT = SignedJWT.parse(plaintext);
			JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
			jsonObject = claimsSet.toJSONObject();
			log.info("decodeEncryptedToken() end...");
			return jsonObject;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | ParseException | JOSEException e) {
			log.error("error in decode token ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("error in decode token ::" + e.getLocalizedMessage());
		}
	}

	private AVScanFileResponse downloadAVScanFile(String fileReference) {
		try {
			log.info("downloadAVScanFile() Start...");
			log.info("downloadAVScanFile() fileReference--->"+fileReference);
			return builder.build().method(HttpMethod.GET).uri(uploadUrl, fileReference).header("x-api-key", apiKey)
					.header("Authorization", "Bearer " + getJwtToken(fileReference)).retrieve()
					.onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(AVScanFileResponse.class).flatMap(errorBody -> {
								log.error("Error in  avscan donload file api");
								throw new AsgwyGlobalException("Error in avscan download file api");
							}))
					.bodyToMono(AVScanFileResponse.class).toFuture().get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error in AV download api ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in AV download api ::" + e.getLocalizedMessage());
		}
	}

	private String getJwtToken(String fileReference) {
		log.info("getJwtToken() Start...");
		String token = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
			// Generate Download Token
			Map<String, Object> header = getHeader();
			Map<String, Object> payload = new HashMap<>();
			payload.put("cvs_av_file_ref", fileReference);
			payload.put("x-lob", "security-engineering");
			payload.put("scope", "openid email");
			payload.put("jti", Long.toString((long) ((Math.random() + 1) * 1_000_000), 36).substring(2));
			payload.put("aud", "CVS-AVScan");
			payload.put("iss", "Aetna-Sales-Gateway");
			payload.put("sub", "download_bearer_token");
			token = createJwt(payload, header, privateKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			log.error("Error in getJwtToken() :: {}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in getJwtToken() ::" + e.getLocalizedMessage());
		}
		log.info("generated JWT token --->Bearer "+token);
		log.info(" getJwtToken() End");
		return token;
	}

	private Map<String, Object> getHeader() {
		Map<String, Object> header = new HashMap<>();
		header.put("alg", "RS256");
		header.put("typ", "JWT");
		header.put("kid", kid);
		header.put("expiresIn", new Date(System.currentTimeMillis() + 3600 * 1000));
		return header;
	}

	private String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey) {
		try {
			log.info("createJwt() Start...");
			JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse(header)).build();
			JWSObject jwsObject = new JWSObject(jwsHeader, new com.nimbusds.jose.Payload(payload));
			jwsObject.sign(new RSASSASigner(privateKey));
			log.info("createJwt() end...");
			return jwsObject.serialize();
		} catch (ParseException | JOSEException e) {
			log.error("Error in createJwt() :: {}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in createJwt() ::" + e.getLocalizedMessage());
		}
	}

	private ResponseEntity<FileUploadResponse> uploadFileOnGateway(File tempFile, FileUploadDatail uploadDatail,
			ServerWebExchange exchange) {
		log.info("uploadFileOnGateway() Start...");
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("quoteId", uploadDatail.getQuoteId());
		formData.add("groupId", uploadDatail.getGroupId());
		formData.add("docSubcategory", uploadDatail.getDocSubcategory());
		formData.add("quoteMissinfoDocInd", uploadDatail.getQuoteMissinfoDocInd());
		formData.add("quoteConcessDocInd", uploadDatail.getQuoteConcessDocInd());
		formData.add("uploadedUsrId", uploadDatail.getUploadedUsrId());
		formData.add("docTyp", uploadDatail.getDocTyp());
		formData.add("uploadedUsrNm", uploadDatail.getUploadedUsrNm());
		formData.add("docCategory", uploadDatail.getDocCategory());
		formData.add("quoteId", uploadDatail.getQuoteConcessionId());
		formData.add("docSize", uploadDatail.getDocSize());
		formData.add("docQuoteStage", uploadDatail.getDocQuoteStage());
		formData.add("quoteSubmitDocInd", uploadDatail.getQuoteSubmitDocInd());
		formData.add("quoteConcessionId", uploadDatail.getQuoteConcessionId());
		formData.add("file", new FileSystemResource(tempFile));
		if(uploadDatail.getDocKey() != null){
			formData.add("docKey",uploadDatail.getDocKey());
		}
		
		List<String> requestHeader = exchange.getRequest().getHeaders().get("tokenvalues");

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
//			headers.add("tokenvals", token);
			log.info("--decrypted tokenvals -- " + jc.decrypt(token));
			headers.add("tokenvals", jc.decrypt(token));
		}

		try {
			FileUploadResponse response = builder.build().method(HttpMethod.POST)
					.uri(gatewayapiUri + gatewayapiBasePath + fileURL).contentType(MediaType.MULTIPART_FORM_DATA)
					.bodyValue(formData).headers(header -> header.addAll(headers)).retrieve()
					.onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(FileUploadResponse.class).flatMap(errorBody -> {
								log.error("Exception in gateway upload file api");
								throw new AsgwyGlobalException("Exception in  gateway upload file api");
							}))
					.bodyToMono(FileUploadResponse.class).toFuture().get();
			if (response != null) {
				log.info("uploadFileOnGateway() End...");
				return new ResponseEntity<FileUploadResponse>(response, HttpStatus.CREATED);
			} else {
				throw new AsgwyGlobalException("Null Response from gateway upload api");
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error("Error in gateway upload api   ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in gateway upload api ::" + e.getLocalizedMessage());
		}

	}
}


name: xxx-Vscan-privatecert
Resource Group: xxx-use2-app
Subscription: 2c3709fcffb
Thumbprint : 4574FC
Issuer :DigiCert Global G2
Subject Name : asgwy.wsdev.xxxx.com


i have added cert in the name of "xxx-Vscan-privatecert"  
the passkey for Private cert is: devpass

asgwy.wsdev.xxxx.com.pfx
 
try to use this cert in code try to access the download api
 
dev cert name : "xxx-Vscan-privatecert"
