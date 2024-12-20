public Mono<AVScanFileResponse> downloadAVScanFile(String fileReference) {
    return proxyWebClient.method(HttpMethod.GET)
        .uri(uploadUrl, fileReference)
        .header("x-api-key", apiKey)
        .header("Authorization", "Bearer " + getJwtToken(fileReference))
        .retrieve()
        .onStatus(status -> !status.is2xxSuccessful(),
                  response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                      log.error("Error in AV scan download file API: {}", errorBody);
                      return Mono.error(new AsgwyGlobalException("Error in AV scan download file API: " + errorBody));
                  }))
        .bodyToMono(AVScanFileResponse.class)
        .doOnSuccess(response -> log.info("AVScanFileResponse: {}", response))
        .doOnError(error -> log.error("Error while downloading AV scan file: ", error));
}



WebClient.builder()
         .baseUrl(baseURL)
         .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
             .responseTimeout(Duration.ofSeconds(30))
             .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30))
                                       .addHandlerLast(new WriteTimeoutHandler(30)))))
         .build();


logging:
  level:
    org.springframework.web.reactive.function.client: DEBUG
