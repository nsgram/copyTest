package com.example.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class AVScanFileFilter implements GlobalFilter {

    private final WebClient avProxyWebClient;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String uri;
    private final String gatewayApiUri;
    private final String gatewayApiBasePath;
    private final String fileURL;

    public AVScanFileFilter(WebClient avProxyWebClient, WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                            String apiKey, String uri, String gatewayApiUri, String gatewayApiBasePath, String fileURL) {
        this.avProxyWebClient = avProxyWebClient;
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.uri = uri;
        this.gatewayApiUri = gatewayApiUri;
        this.gatewayApiBasePath = gatewayApiBasePath;
        this.fileURL = fileURL;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("AVScanFileFilter.filter() Start...");

        return exchange.getRequest().getBody()
            .next()
            .flatMap(dataBuffer -> {
                try {
                    String body = dataBuffer.toString(StandardCharsets.UTF_8);
                    AVScanFileRequest avScanFileRequest = objectMapper.readValue(body, AVScanFileRequest.class);
                    Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());

                    String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
                    String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");

                    if ("Y".equalsIgnoreCase(avFileClean) && avFileRef != null) {
                        log.info("File is clean, proceeding with download and upload.");

                        return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
                            .flatMap(response -> {
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                exchange.getResponse().setStatusCode(HttpStatus.CREATED);

                                return exchange.getResponse().writeWith(
                                    Mono.fromSupplier(() -> {
                                        try {
                                            return exchange.getResponse().bufferFactory()
                                                .wrap(objectMapper.writeValueAsBytes(response));
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException("Error serializing response", e);
                                        }
                                    })
                                );
                            })
                            .onErrorResume(e -> {
                                log.error("Error during file processing: {}", e.getMessage(), e);
                                return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, e.getMessage());
                            });
                    } else {
                        log.error("The uploaded file appears to be unsafe.");
                        return exceptionResponse(exchange, chain, HttpStatus.UNPROCESSABLE_ENTITY, 
                            "The uploaded file appears to be unsafe.");
                    }
                } catch (Exception e) {
                    log.error("Error processing AVScanFileFilter: {}", e.getMessage(), e);
                    return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED, 
                        "Error processing request: " + e.getMessage());
                }
            });
    }

    private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
                                                          AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
        return downloadAVScanFile(avFileRef)
            .flatMap(avDownloadResponse -> {
                String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

                return Mono.fromCallable(() -> {
                    String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
                    byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);

                    File tempFile = File.createTempFile("upload_", ".tmp");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(decodedBytes);
                    }
                    return tempFile;
                }).flatMap(tempFile -> uploadFileOnGateway(tempFile, avScanFileRequest, exchange)
                    .doFinally(signalType -> {
                        boolean deleted = tempFile.delete();
                        log.info("Temporary file cleanup status: {}", deleted ? "SUCCESS" : "FAILED");
                    })
                );
            });
    }

    private Mono<AVScanFileResponse> downloadAVScanFile(String fileReference) {
        log.info("downloadAVScanFile() Start...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", apiKey);
        headers.add("Authorization", getJwtToken(fileReference));

        return avProxyWebClient.get()
            .uri(uri + fileReference)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> {
                log.error("Error in AVScan download file API");
                return Mono.error(new AsgwyGlobalException("Error in AVScan download file API"));
            })
            .bodyToMono(AVScanFileResponse.class)
            .doOnSuccess(response -> log.info("File Downloaded, statusDescription: {}", response.getStatusDescription()));
    }

    private Mono<FileUploadResponse> uploadFileOnGateway(File tempFile, AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
        log.info("uploadFileOnGateway() Start...");

        FileUploadDatail uploadDatail = avScanFileRequest.getUploadData();
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("quoteId", uploadDatail.getQuoteId());
        formData.add("groupId", uploadDatail.getGroupId());
        formData.add("docSubcategory", uploadDatail.getDocSubcategory());
        formData.add("file", new FileSystemResource(tempFile));

        HttpHeaders headers = new HttpHeaders();
        List<String> requestHeader = exchange.getRequest().getHeaders().get(WebConstants.TOKENVALUES);
        if (requestHeader != null) {
            JavaCrypto jc = new JavaCrypto();
            String decryptedToken = jc.decrypt(requestHeader.get(0));
            log.info("--decrypted tokenvals-- {}", decryptedToken);
            headers.add("tokenvals", decryptedToken);
        }

        return webClientBuilder.build()
            .post()
            .uri(gatewayApiUri + gatewayApiBasePath + fileURL)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(formData)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> {
                log.error("Exception in gateway upload file API");
                return Mono.error(new AsgwyGlobalException("Exception in gateway upload file API"));
            })
            .bodyToMono(FileUploadResponse.class)
            .doOnSuccess(response -> log.info("Upload successful, document ID: {}", response.getQuoteDocumentId()));
    }

    private Mono<Void> exceptionResponse(ServerWebExchange exchange, WebFilterChain chain, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
