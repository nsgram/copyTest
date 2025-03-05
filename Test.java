private Mono<FileUploadResponse> uploadFileOnGateway(File tempFile, AVScanFileRequest avScanFileRequest,
			ServerWebExchange exchange) {
		log.info("uploadFileOnGateway() Start...");
		MultiValueMap<String, Object> formData = getGatewayFileUploadRequest(tempFile, avScanFileRequest);
		List<String> requestHeader = exchange.getRequest().getHeaders().get(WebConstants.TOKENVALUES);

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
			headers.add(TOKENVALS, jc.decrypt(token));
		}
		return webClientBuilder.build().post().uri(gatewayapiUri + gatewayapiBasePath + fileURL)
				.contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(formData).headers(h -> h.addAll(headers))
				.retrieve().onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							log.error(" Exception raised in gateway file upload api at gateway end ");
							throw new AsgwyGlobalException("Exception in gateway file upload api");
						}))
				.bodyToMono(FileUploadResponse.class).doOnSuccess(
						response -> log.info("Upload successful, document ID :::" + response.getQuoteDocumentId()));
	}
