 webClientBuilder.build().post().uri(gatewayapiUri + gatewayapiBasePath + fileURL)
				.contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(formData).headers(h -> h.addAll(headers))
				.retrieve().onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							log.error(" Exception raised in gateway file upload api at gateway end ");
							throw new AsgwyGlobalException("Exception in gateway file upload api");
						}))
				.bodyToMono(FileUploadResponse.class).doOnSuccess(
						response -> log.info("Upload successful, document ID :::" + response.getQuoteDocumentId()));


If any exception occoured get response body is it 
my api this throwing 413 excetion i want exact same response body to return as response
