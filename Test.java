JsonNode downloadJson = builder.build()
        .method(HttpMethod.GET)
        .uri(t -> t.host("https://sit1-api.cvshealth.com")
                   .path("/file/scan/download/v1/files")
                   .build(fileReference))
        .headers(t -> t.addAll(headers))
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            errorResponse -> errorResponse.bodyToMono(String.class).flatMap(errorBody -> {
                log.error("Exception from Plansponsor API: " + errorBody);
                throw new RoutingException(errorBody);
            })
        )
        .bodyToMono(String.class) // First get as String
        .flatMap(body -> {
            if (MediaType.APPLICATION_JSON_VALUE.equals(headers.getContentType())) {
                return Mono.just(new ObjectMapper().readTree(body)); // Parse JSON if applicable
            } else {
                log.error("Unexpected response content type");
                throw new UnsupportedOperationException("Unexpected content type");
            }
        })
        .toFuture()
        .get();
