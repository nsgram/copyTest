JsonNode downloadJson = builder.build().method(HttpMethod.GET)
					.uri(t -> t.host("https://sit1-api.cvshealth.com/file/scan/download/v1/files").path(fileReference)
							.build())
					.headers(t -> t.addAll(headers)).retrieve().onStatus(status -> !status.is2xxSuccessful(),
							errorResponse -> errorResponse.bodyToMono((JsonNode.class)).flatMap(errorBody -> {
								log.error("Exception from Plansponsor API " + errorBody);
								throw new RoutingException("");
							}))
					.bodyToMono(JsonNode.class).toFuture().get();

	getting below exception

	Error downloading AVScan file: org.springframework.web.reactive.function.UnsupportedMediaTypeException:
	 Content type 'text/html;charset=us-ascii' not supported for bodyType=com.fasterxml.jackson.databind.JsonNode"
