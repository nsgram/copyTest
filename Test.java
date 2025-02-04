package com.aetna.asgwy.webmw.avscan.filters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.aetna.asgwy.webmw.common.model.AVScanFileRequest;
import com.aetna.asgwy.webmw.common.model.AVScanFileResponse;
import com.aetna.asgwy.webmw.common.model.FileUploadDatail;
import com.aetna.asgwy.webmw.common.model.FileUploadResponse;
import com.aetna.asgwy.webmw.common.model.WebConstants;
import com.aetna.asgwy.webmw.exception.AsgwyGlobalException;
import com.aetna.asgwy.webmw.util.AVScanFileUtil;
import com.aetna.framework.security.javacrypto.JavaCrypto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AVScanFileFilter implements GatewayFilter {
	@Value("${avscan.header.kid}")
	private String kid;

	@Value("${avscan.header.x-api-key}")
	private String apiKey;

	@Value("${avscan.download.uri}")
	private String uri;

	@Value("${gatewayapi.uri}")
	private String gatewayapiUri;

	@Value("${gatewayapi.basePath}")
	private String gatewayapiBasePath;

	@Value("${gatewayapi.fileURL}")
	private String fileURL;

	private final WebClient.Builder webClientBuilder;

	private final WebClient avProxyWebClient;

	public AVScanFileFilter(WebClient.Builder webClientBuilder, WebClient avProxyWebClient) {
		this.webClientBuilder = webClientBuilder;
		this.avProxyWebClient = avProxyWebClient;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("AVScanFileFilter.filter() Start...");
		return exchange.getRequest().getBody().next().flatMap(dataBuffer -> {
			try {
				String body = dataBuffer.toString(StandardCharsets.UTF_8);
				AVScanFileRequest avScanFileRequest = new ObjectMapper().readValue(body, AVScanFileRequest.class);
				// Process the AVScan file
				Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
				String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
				String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");
				if ("Y".equalsIgnoreCase(avFileClean) && !StringUtils.isEmpty(avFileRef)) {
					log.info("File is clean, proceeding with download and upload.");
					return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange,chain)
							.flatMap(response -> {
								exchange.getAttributes().put("fileUploadResponse", response);
								exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
								exchange.getResponse().setStatusCode(HttpStatus.CREATED);
								return chain.filter(exchange.mutate().build()).then(Mono.defer(() -> {
									try {
										return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
												.bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(response))));
									} catch (JsonProcessingException e) {
										return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED,
												"File is clean but not able to proccess it ::"
														+ e.getLocalizedMessage());
									}
								}));

							});

				} else {
					log.error("The uploaded file appears to be unsafe.");
					return exceptionResponse(exchange, chain, HttpStatus.UNPROCESSABLE_ENTITY,
							"The uploaded file appears to be unsafe. ");
				}
			} catch (Exception e) {
				log.error("Error processing AVScanFileFilter: {}", e.getMessage(), e);
				return exceptionResponse(exchange, chain, HttpStatus.INTERNAL_SERVER_ERROR,
						"Error processing request: " + e.getMessage());
			}
		});
	}

	private Mono<? extends Void> exceptionResponse(ServerWebExchange exchange, GatewayFilterChain chain,
			HttpStatus status, String message) {
		exchange.getResponse().setStatusCode(status);
		return chain.filter(exchange.mutate().build()).then(Mono.defer(() -> {
			try {
				return exchange.getResponse().writeWith(Mono.just(
						exchange.getResponse().bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(message))));
			} catch (JsonProcessingException e) {
				return Mono
						.error(new AsgwyGlobalException("Exception at json processing ::" + e.getLocalizedMessage()));
			}
		}));
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

	private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
			AVScanFileRequest avScanFileRequest, ServerWebExchange exchange,GatewayFilterChain chain) {
		return Mono.using(
				// Resource Supplier
				() -> {
					String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
					String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
					String tempDir = System.getProperty("java.io.tmpdir");
					// Write to a temporary file
					File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);
					AVScanFileResponse avDownloadResponse = downloadAVScanFile(avFileRef);
					log.info("File Downloaded form av malware statusDescription::{} ",
							avDownloadResponse.getStatusDescription());
					String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");
					try (FileOutputStream fos = new FileOutputStream(tempFile)) {
						String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(),
								avFileDownloadKey);
						byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
						fos.write(decodedBytes);
					}
					return tempFile;
				},
				// Mono using Resource
				tempFile -> {
					log.info("Uploading file to gateway...");
					FileUploadResponse response = uploadFileOnGateway(tempFile, avScanFileRequest, exchange,chain);
					log.info("Uploading file to gateway document id..." + response.getQuoteDocumentId());
					return Mono.just(response);
				},
				// Cleanup Logic
				tempFile -> {
					if (tempFile != null && tempFile.exists()) {
						boolean deleted = tempFile.delete();
						log.info("Temporary file cleanup status: {}", deleted ? "SUCCESS" : "FAILED");
					}
				});
	}

	private AVScanFileResponse downloadAVScanFile(String fileReference) {

		try {
			log.info("downloadAVScanFile() Start...");

			HttpHeaders headers = new HttpHeaders();
			headers.add("x-api-key", apiKey);
			headers.add("Authorization", getJwtToken(fileReference));

			return avProxyWebClient.method(HttpMethod.GET).uri(uri + fileReference)
					.headers(header -> header.addAll(headers)).retrieve().onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(AVScanFileResponse.class).flatMap(errorBody -> {
								log.error("Error in avscan download file api");
								throw new AsgwyGlobalException("Error in avscan download file api");
							}))
					.bodyToMono(AVScanFileResponse.class).toFuture().get();

		} catch (InterruptedException e) {
			log.error("Error in AV download api ::{}", e.getLocalizedMessage());
			Thread.currentThread().interrupt();
			throw new AsgwyGlobalException("Error in AV download api ::" + e.getLocalizedMessage());

		} catch (ExecutionException e) {
			log.error("Error in AV download api ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in AV download api ::" + e.getLocalizedMessage());
		}

	}

	private FileUploadResponse uploadFileOnGateway(File tempFile, AVScanFileRequest avScanFileRequest,
			ServerWebExchange exchange,GatewayFilterChain chain) {
		log.info("uploadFileOnGateway() Start...");
		FileUploadDatail uploadDatail = avScanFileRequest.getUploadData();
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
		formData.add("docSize", uploadDatail.getDocSize());
		formData.add("docQuoteStage", uploadDatail.getDocQuoteStage());
		formData.add("quoteSubmitDocInd", uploadDatail.getQuoteSubmitDocInd());
		formData.add("docImqIndicator", uploadDatail.getDocImqIndicator());
		formData.add("docRcIndicator", uploadDatail.getDocRcIndicator());
		formData.add("docclaimsexpIndicator", uploadDatail.getDocclaimsexpIndicator());
		formData.add("file", new FileSystemResource(tempFile));

		if (uploadDatail.getQuoteConcessionId() != null) {
			formData.add("quoteConcessionId", uploadDatail.getQuoteConcessionId());
		}
		if (uploadDatail.getDocKey() != null) {
			formData.add("docKey", uploadDatail.getDocKey());
		}
		if (avScanFileRequest.getQuoteCommentDTO() != null) {
			formData.add("commentTxt", avScanFileRequest.getQuoteCommentDTO().getCommentTxt());
			formData.add("commentCategory", avScanFileRequest.getQuoteCommentDTO().getCommentCategory());
			formData.add("quoteSubmitCommentInd", avScanFileRequest.getQuoteCommentDTO().getQuoteSubmitCommentInd());
			formData.add("quoteConcessCommentInd", avScanFileRequest.getQuoteCommentDTO().getQuoteConcessCommentInd());
			formData.add("quoteMissinfoCommentInd",
					avScanFileRequest.getQuoteCommentDTO().getQuoteMissinfoCommentInd());
			formData.add("sentToFilenetInd", avScanFileRequest.getQuoteCommentDTO().getSentToFilenetInd());
		}
		List<String> requestHeader = exchange.getRequest().getHeaders().get(WebConstants.TOKENVALUES);

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
			log.info("--decrypted tokenvals -- " + jc.decrypt(token));
			headers.add("tokenvals", jc.decrypt(token));
		}
		try {
			FileUploadResponse response = webClientBuilder.build().method(HttpMethod.POST)
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
				return response;
			} else {
				throw new AsgwyGlobalException("Null Response from gateway upload api");
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error("Error in gateway upload api   ::{}", e.getLocalizedMessage());
			chain.filter(exchange.mutate().build());
			throw new AsgwyGlobalException("Error in gateway upload api ::" + e.getLocalizedMessage());
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
		log.info(" getJwtToken() End");
		return "Bearer " + token;
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
}
