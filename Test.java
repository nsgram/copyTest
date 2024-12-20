public Mono<AVScanFileResponse> downloadAVScanFile(String fileReference) {
    return proxyWebClient.method(HttpMethod.GET)
        .uri(uploadUrl, fileReference)
        .header("x-api-key", apiKey)
        .header("Authorization", "Bearer " + getJwtToken(fileReference))
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                log.error("Error in AV scan download file API: {}", errorBody);
                return Mono.error(new AsgwyGlobalException("Error in AV scan download file API: " + errorBody));
            })
        )
        .bodyToMono(AVScanFileResponse.class)
        .retryWhen(
            ReactorRetry.backoff(3, Duration.ofSeconds(2)) // Retry 3 times with 2 seconds between attempts
                .filter(throwable -> throwable instanceof WebClientException) // Retry for specific exceptions
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("Retry attempts exhausted for fileReference: {}", fileReference);
                    return new RuntimeException("Retry attempts exhausted");
                })
        )
        .doOnSuccess(response -> log.info("Successfully retrieved AVScanFileResponse: {}", response))
        .doOnError(error -> log.error("Error during AV scan file retrieval: {}", error.getMessage()));
}
