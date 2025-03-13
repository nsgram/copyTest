public String createAgreementWithTransientDocument(AdobeSignRequest adobeSignRequest) {

		String url = getBaseUri() + "/api/rest/v6/agreements";
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = null;
		try {
			jsonRequest = mapper.writeValueAsString(adobeSignRequest);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION.toString(), authorizationKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
		HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

		ResponseEntity<Map<String, String>> response = proxyRestTemplate.exchange(url, HttpMethod.POST, requestEntity,
				new ParameterizedTypeReference<>() {
				});
		Map<String, String> responseBody = response.getBody();

		if (responseBody != null && responseBody.containsKey("id")) {
			return responseBody.get("id");
		} else {
			throw new AsgwyGlobalException(404, "id not Found");
		}

	}
	
	
	getting below error
	
o.s.web.client.RestTemplate - Response 403 FORBIDDEN
[2025-03-13 23:47:44.649] asgwy-api-oci-qa [DEBUG] http-nio-9080-exec-3 []-[]--[2] o.s.w.s.m.m.a.ExceptionHandlerExceptionResolver - Using @ExceptionHandler com.aetna.asgwy.exception.GlobalExceptionHandler#handleException(HttpServletRequest, Exception)
[2025-03-13 23:47:44.649] asgwy-api-oci-qa [ERROR] http-nio-9080-exec-3 []-[]--[2] c.a.a.e.GlobalExceptionHandler - handleException : 
org.springframework.web.client.HttpClientErrorException$Forbidden: 403 Forbidden: "{"code":"DOCUMENT_VISIBILITY_DISABLED","message":"Document visibility is disabled for this agreement."}"
