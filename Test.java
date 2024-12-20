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
        .retryWhen(errors -> errors
            .filter(error -> error instanceof WebClientException)  // Retry only on WebClient errors
            .delayElements(Duration.ofSeconds(2))  // Add delay of 2 seconds between retries
            .take(3)  // Retry 3 times
        )
        .doOnSuccess(response -> log.info("Successfully retrieved AVScanFileResponse: {}", response))
        .doOnError(error -> log.error("Error during AV scan file retrieval: {}", error.getMessage()));
}
