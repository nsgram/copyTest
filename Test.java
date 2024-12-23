private AVScanFileResponse downloadAVScanFile(String fileReference) {
    try {
        log.info("downloadAVScanFile() Start...");
        log.info("downloadAVScanFile() fileReference--->" + fileReference);

        // Perform the WebClient call in a blocking-safe manner
        return proxyWebClient
                .get()
                .uri(uploadUrl, fileReference) // Dynamically replace placeholder
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
                .block(); // Ensure blocking behavior here
    } catch (Exception e) {
        log.error("Error in AV download API: {}", e.getMessage(), e);
        throw new AsgwyGlobalException("Error in AV download API: " + e.getMessage(), e);
    }
}
