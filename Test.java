private AVScanFileResponse downloadAVScanFile(String fileReference) {
    try {
        log.info("Starting AV Scan File download for fileReference: {}", fileReference);

        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 seconds
            .responseTimeout(Duration.ofSeconds(30))           // 30 seconds
            .option(ChannelOption.SO_KEEPALIVE, true);         // Enable keep-alive

        WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

        return webClient.method(HttpMethod.GET)
            .uri(uploadurl, fileReference)
            .header("x-api-key", apiKey)
            .header("Authorization", "Bearer " + getJwtToken(fileReference))
            .retrieve()
            .bodyToMono(AVScanFileResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))) // Retry on failure
            .block();
    } catch (Exception e) {
        log.error("Error downloading AV Scan file: {}", e.getMessage(), e);
        throw new AsgwyGlobalException("Error in AV download API", e);
    }
}
