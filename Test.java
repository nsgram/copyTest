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

import com.aetna.asgwy.webmw.common.model.*;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                AVScanFileRequest avScanFileRequest = objectMapper.readValue(body, AVScanFileRequest.class);
                
                Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
                String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
                String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");

                if ("Y".equalsIgnoreCase(avFileClean) && StringUtils.isNotEmpty(avFileRef)) {
                    log.info("File is clean, proceeding with download and upload.");
                    
                    return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
                            .flatMap(response -> sendResponse(exchange, chain, response))
                            .onErrorResume(e -> exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, e.getMessage()));
                } else {
                    log.error("The uploaded file appears to be unsafe.");
                    return exceptionResponse(exchange, chain, HttpStatus.UNPROCESSABLE_ENTITY, "The uploaded file appears to be unsafe.");
                }
            } catch (Exception e) {
                log.error("Error processing AVScanFileFilter: {}", e.getMessage(), e);
                return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, "Error processing request: " + e.getMessage());
            }
        });
    }

    private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
                                                          AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            File tempFile = createTempFile(decodeJson);
            writeToTempFile(tempFile, avFileRef, decodeJson);
            return tempFile;
        }).flatMap(tempFile -> uploadFileOnGateway(tempFile, avScanFileRequest, exchange)
                .doFinally(signalType -> cleanupTempFile(tempFile)));
    }

    private File createTempFile(Map<String, Object> decodeJson) throws IOException {
        String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
        String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
        File tempFile = File.createTempFile(avOriginalFileName, "." + avOriginalFileType);
        log.info("Temporary file created: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    private void writeToTempFile(File tempFile, String avFileRef, Map<String, Object> decodeJson) throws IOException {
        AVScanFileResponse avDownloadResponse = downloadAVScanFile(avFileRef);
        String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");
        
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
            byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
            fos.write(decodedBytes);
        }
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile.exists() && tempFile.delete()) {
            log.info("Temporary file deleted: {}", tempFile.getAbsolutePath());
        }
    }

    private AVScanFileResponse downloadAVScanFile(String fileReference) {
        log.info("Downloading file from AVScan...");
        return avProxyWebClient.get()
                .uri(uri + fileReference)
                .headers(headers -> {
                    headers.add("x-api-key", apiKey);
                    headers.add("Authorization", getJwtToken(fileReference));
                })
                .retrieve()
                .bodyToMono(AVScanFileResponse.class)
                .block();
    }

    private String getJwtToken(String fileReference) {
        try {
            PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
            Map<String, Object> payload = new HashMap<>();
            payload.put("cvs_av_file_ref", fileReference);
            payload.put("aud", "CVS-AVScan");
            payload.put("iss", "Aetna-Sales-Gateway");

            return "Bearer " + createJwt(payload, privateKey);
        } catch (Exception e) {
            log.error("Error in getJwtToken(): {}", e.getMessage());
            throw new AsgwyGlobalException("Error generating JWT token: " + e.getMessage());
        }
    }

    private String createJwt(Map<String, Object> payload, PrivateKey privateKey) throws JOSEException {
        JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(new JWSHeader.Builder().type("JWT")).build(),
                new com.nimbusds.jose.Payload(payload));
        jwsObject.sign(new RSASSASigner(privateKey));
        return jwsObject.serialize();
    }

    private Mono<Void> sendResponse(ServerWebExchange exchange, GatewayFilterChain chain, FileUploadResponse response) {
        exchange.getResponse().setStatusCode(HttpStatus.CREATED);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(objectMapper.writeValueAsBytes(response))));
    }
}
