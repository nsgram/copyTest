private Mono<AVScanFileResponse> downloadAVScanFile(String fileReference) {
    log.info("Starting AV Scan File download for fileReference: {}", fileReference);

    return builder.build()
        .method(HttpMethod.GET)
        .uri(uploadurl, fileReference)
        .header("x-api-key", apiKey)
        .header("Authorization", "Bearer " + getJwtToken(fileReference))
        .retrieve()
        .bodyToMono(AVScanFileResponse.class)
        .doOnNext(response -> log.info("Successfully received response: {}", response))
        .doOnError(error -> log.error("Error during file download: {}", error.getMessage(), error))
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))) // Retry on transient failures
        .onErrorResume(e -> {
            log.error("Fallback mechanism triggered: {}", e.getMessage());
            return Mono.error(new AsgwyGlobalException("Error in AV download API", e));
        });
}
