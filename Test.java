@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*"); // Use specific domains in production
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
}

private AVScanFileResponse downloadAVScanFile(String fileReference) {
    try {
        Log.info("downloadAVScanFile() Start...");
        Log.info("downloadAVScanFile() fileReference ---> " + fileReference);

        return WebClient.builder()
                .build()
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder.path(uploadUrl).queryParam("fileReference", fileReference).build())
                .header("x-api-key", "your-api-key-here")
                .header("Authorization", "Bearer " + getJwtToken(fileReference))
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> {
                            Log.error("Error in AV scan download file API");
                            return clientResponse
                                    .bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new AsgwyGlobalException("AV scan error: " + errorBody)));
                        }
                )
                .bodyToMono(AVScanFileResponse.class)
                .toFuture()
                .get();
    } catch (InterruptedException | ExecutionException e) {
        Log.error("Error in AV download API :: {}", e.getLocalizedMessage());
        throw new AsgwyGlobalException("Error in AV download API :: " + e.getLocalizedMessage(), e);
    }
}
