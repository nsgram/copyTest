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
                Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());

                String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
                String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");

                if ("Y".equalsIgnoreCase(avFileClean) && StringUtils.isNotBlank(avFileRef)) {
                    return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
                            .flatMap(response -> {
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                exchange.getResponse().setStatusCode(HttpStatus.CREATED);
                                return exchange.getResponse().writeWith(Mono.just(
                                        exchange.getResponse().bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(response))
                                ));
                            })
                            .onErrorResume(e -> exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, e.getMessage()));
                } else {
                    return exceptionResponse(exchange, chain, HttpStatus.UNPROCESSABLE_ENTITY, "File appears to be unsafe.");
                }
            } catch (Exception e) {
                log.error("Error processing AVScanFileFilter: {}", e.getMessage(), e);
                return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, "Error processing request: " + e.getMessage());
            }
        });
    }

    private Map<String, Object> decodeEncryptedToken(String token) {
        log.info("decodeEncryptedToken() start...");
        try {
            PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(new RSADecrypter(privateKey));

            SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
            return signedJWT.getJWTClaimsSet().toJSONObject();
        } catch (Exception e) {
            log.error("Error decoding token: {}", e.getLocalizedMessage());
            throw new AsgwyGlobalException("Error decoding token: " + e.getLocalizedMessage());
        }
    }

    private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
            AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {

        return Mono.fromCallable(() -> {
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, (String) decodeJson.get("cvs_av_original_file_name"));
            
            AVScanFileResponse avDownloadResponse = downloadAVScanFile(avFileRef);
            String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] decodedBytes = Base64.getDecoder().decode(AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey));
                fos.write(decodedBytes);
            }

            return tempFile;
        }).flatMap(tempFile -> {
            return uploadFileOnGateway(tempFile, avScanFileRequest, exchange)
                    .doFinally(signal -> tempFile.delete());
        });
    }

    private AVScanFileResponse downloadAVScanFile(String fileReference) {
        log.info("Downloading file from AVScan...");
        return avProxyWebClient.get()
                .uri(uri + fileReference)
                .header("x-api-key", apiKey)
                .header("Authorization", getJwtToken(fileReference))
                .retrieve()
                .bodyToMono(AVScanFileResponse.class)
                .block();
    }

    private FileUploadResponse uploadFileOnGateway(File tempFile, AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
        log.info("Uploading file to gateway...");
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(tempFile));

        return webClientBuilder.build().post()
                .uri(gatewayapiUri + gatewayapiBasePath + fileURL)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(FileUploadResponse.class)
                .block();
    }

    private String getJwtToken(String fileReference) {
        log.info("Generating JWT token...");
        return "Bearer " + "your_jwt_token";
    }

    private Mono<Void> exceptionResponse(ServerWebExchange exchange, GatewayFilterChain chain, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(message.getBytes())));
    }
}
