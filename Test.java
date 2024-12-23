import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

private RestTemplate restTemplate;

public AVScanFileResponse downloadAVScanFile(String fileReference) {
    try {
        log.info("downloadAVScanFile() Start...");
        log.info("downloadAVScanFile() fileReference--->" + fileReference);

        // Construct the URL with the fileReference
        String resolvedUrl = uploadUrl.replace("{fileReference}", fileReference);

        // Prepare HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("Authorization", "Bearer " + getJwtToken(fileReference));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Execute the request
        ResponseEntity<AVScanFileResponse> responseEntity = restTemplate.exchange(
                resolvedUrl, 
                HttpMethod.GET, 
                requestEntity, 
                AVScanFileResponse.class
        );

        // Return the response body
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            throw new AsgwyGlobalException("Error in AV scan download file API: Unexpected response");
        }
    } catch (Exception e) {
        log.error("Error in AV download API: {}", e.getMessage(), e);
        throw new AsgwyGlobalException("Error in AV download API: " + e.getMessage(), e);
    }
}


@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    // Add SSL support
    try {
        SSLContext sslContext = createSslContext();
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLContext(sslContext).build());
        restTemplate.setRequestFactory(factory);
    } catch (Exception e) {
        log.error("Failed to set SSL for RestTemplate", e);
        throw new RuntimeException(e);
    }

    return restTemplate;
}

private SSLContext createSslContext() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    try (InputStream keyStoreStream = this.getClass().getClassLoader().getResourceAsStream("malware.qa.jks")) {
        keyStore.load(keyStoreStream, "malware".toCharArray());
    }

    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStore);

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

    return sslContext;
}









private AVScanFileResponse downloadAVScanFile(String fileReference) {
    log.info("downloadAVScanFile() Start...");
    log.info("downloadAVScanFile() fileReference--->" + fileReference);

    return Mono.fromCallable(() -> 
        proxyWebClient
            .get()
            .uri(uploadUrl, fileReference)
            .header("x-api-key", apiKey)
            .header("Authorization", "Bearer " + getJwtToken(fileReference))
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> 
                clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                    log.error("Error in AVScan download file API: {}", errorBody);
                    return Mono.error(new AsgwyGlobalException("Error in AVScan download file API"));
                })
            )
            .bodyToMono(AVScanFileResponse.class)
            .block() // Still blocking but managed here
    )
    .subscribeOn(Schedulers.boundedElastic()) // Executes in a blocking-safe thread-pool
    .block(); // Allows blocking behavior here safely
}
