webClientBuilder.build()
    .post()
    .uri(gatewayapiUri + gatewayapiBasePath + fileURL)
    .contentType(MediaType.MULTIPART_FORM_DATA)
    .bodyValue(formData)
    .headers(h -> h.addAll(headers))
    .retrieve()
    .onStatus(
        status -> !status.is2xxSuccessful(),
        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
            log.error("Gateway API error response: {}", errorBody);

            // Convert the error response to JSON format
            String jsonErrorResponse = convertErrorToJson(errorBody, clientResponse.statusCode());
            return Mono.error(new AsgwyGlobalException(jsonErrorResponse));
        })
    )
    .bodyToMono(FileUploadResponse.class)
    .doOnSuccess(response -> log.info("Upload successful, document ID: {}", response.getQuoteDocumentId()))
    .doOnError(AsgwyGlobalException.class, ex -> log.error("Error occurred: {}", ex.getMessage()));


private String convertErrorToJson(String errorBody, HttpStatus statusCode) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", statusCode.value());
        errorResponse.put("error", statusCode.getReasonPhrase());
        errorResponse.put("message", errorBody);
        errorResponse.put("timestamp", Instant.now().toString());

        return objectMapper.writeValueAsString(errorResponse);
    } catch (JsonProcessingException e) {
        log.error("Failed to convert error response to JSON: {}", e.getMessage());
        return "{\"status\":" + statusCode.value() + ",\"error\":\"" + statusCode.getReasonPhrase() + "\",\"message\":\"Unable to process error response\"}";
    }
}
