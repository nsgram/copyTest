 private ResponseEntity<FileUploadResponse> uploadFileOnGateway(File tempFile, FileUploadDatail uploadDatail,
			ServerWebExchange exchange) {
		log.info("uploadFileOnGateway() Start...");
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("quoteId", uploadDatail.getQuoteId());
		formData.add("groupId", uploadDatail.getGroupId());
		formData.add("docSubcategory", uploadDatail.getDocSubcategory());
		formData.add("quoteMissinfoDocInd", uploadDatail.getQuoteMissinfoDocInd());
		formData.add("quoteConcessDocInd", uploadDatail.getQuoteConcessDocInd());
		formData.add("uploadedUsrId", uploadDatail.getUploadedUsrId());
		formData.add("docTyp", uploadDatail.getDocTyp());
		formData.add("uploadedUsrNm", uploadDatail.getUploadedUsrNm());
		formData.add("docCategory", uploadDatail.getDocCategory());
		formData.add("quoteId", uploadDatail.getQuoteConcessionId());
		formData.add("docSize", uploadDatail.getDocSize());
		formData.add("docQuoteStage", uploadDatail.getDocQuoteStage());
		formData.add("quoteSubmitDocInd", uploadDatail.getQuoteSubmitDocInd());
		
		formData.add("file", new FileSystemResource(tempFile));
		
		if (uploadDatail.getQuoteConcessionId() != null) {
			formData.add("quoteConcessionId", uploadDatail.getQuoteConcessionId());
		}
		if (uploadDatail.getDocKey() != null) {
			formData.add("docKey", uploadDatail.getDocKey());
		}

		List<String> requestHeader = exchange.getRequest().getHeaders().get("tokenvalues");

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
			log.info("--decrypted tokenvals -- " + jc.decrypt(token));
			headers.add("tokenvals", jc.decrypt(token));
		}

		try {
			FileUploadResponse response = builder.build().method(HttpMethod.POST)
					.uri(gatewayapiUri + gatewayapiBasePath + fileURL).contentType(MediaType.MULTIPART_FORM_DATA)
					.bodyValue(formData).headers(header -> header.addAll(headers)).retrieve()
					.onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(FileUploadResponse.class).flatMap(errorBody -> {
								log.error("Exception in gateway upload file api");
								throw new AsgwyGlobalException("Exception in  gateway upload file api");
							}))
					.bodyToMono(FileUploadResponse.class).toFuture().get();
			if (response != null) {
				log.info("uploadFileOnGateway() End...");
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				throw new AsgwyGlobalException("Null Response from gateway upload api");
			}

		} catch (InterruptedException e) {
			log.error("Error in gateway upload api   ::{}", e.getLocalizedMessage());
			Thread.currentThread().interrupt();
			throw new AsgwyGlobalException("Error in gateway upload api ::" + e.getLocalizedMessage());
		} catch (ExecutionException e) {
			log.error("Error in gateway upload api   ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in gateway upload api ::" + e.getLocalizedMessage());
		}

	}

getting below error

"message": "Error in gateway upload api ::org.springframework.web.reactive.function.client.WebClientRequestException: The iterator returned a null value"


if We quoteConcessionId value in request then it working
quoteConcessionId is optional attribute please write code to make quoteConcessionId as optional
