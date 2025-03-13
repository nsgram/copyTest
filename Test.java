Request 2
curl -X 'POST' \
  'https://api.na2.documents.adobe.com/api/rest/v6/transientDocuments' \
  -H 'accept: application/json' \
  -H 'Content-Type: multipart/form-data' \
  -H 'Authorization: Bearer 3AAABLblqZhAGDFfvYVDORbzoUnKCkbGeUqkWyvNkITfabRokoDZElC' \
  -F 'File-Name=Proposal' \
  -F 'File=@Proposal.pdf;type=application/pdf'
Response
{
  "transientDocumentId": "CBSCTBABDUAAABACAABAAeZ4mh3uIRO6nLuRJLKl5fpVy6oLf_OPbZvBd4mTYBXzIT7rNs5I-VaNYUzEBZ39mKO4qlZziB7XF3wLWEGq5l2OmlS8BWYfw4LqJuULDhlcgHtvVQOSot2OHxVGwRcfAEJmcqCZ4Qx-N3E-mDPW5T3lm6QkjHpfxhuy6Ix5xFkYPtZoz_mZHfoBB9kKX_d4R98mP9xrTk1-XYDyuta0mZC-d8TxldOyOJKbvbHj9ZR1AnS-phmKBGRS0YdbNs5AUzB57BPYXtZgSvn9DivZTwTytpyeIjriJnaiAV8DMBIJymWZLoprJK2a5RZzatj3G_jeyeCO3oUZ2i8nI8TqFo2KSyLN6FnDhzihKwOKiHfw*"
}

How to implement this api call in spring bott rest endpoint java 
I want to return transientDocumentId as response;

I have tried like below 
String url = getBaseUri() + "/api/rest/v6/transientDocuments";

		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION.toString(), authorizationKey);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		Resource fileResource = new ByteArrayResource(file.getBytes());
		
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("File-Name", file.getOriginalFilename());
		body.add("File", fileResource);

		ResponseEntity<Map<String, String>> response = proxyRestTemplate.exchange(url, HttpMethod.POST,
				new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
				});
		Map<String, String> responseBody = response.getBody();

		if (responseBody != null && responseBody.containsKey("transientDocumentId")) {
			return responseBody.get("transientDocumentId");
		} else {
			throw new AsgwyGlobalException(404, "transientDocumentId not Found");
		}

	}
getting exception
GlobalExceptionHandler - handleException : 
org.springframework.web.client.HttpClientErrorException$BadRequest: 400 Bad Request: "{"code":"INVALID_MULTIPART","message":"An invalid multipart was specified"}"
