@When("Consumer wants to Generate & Send Banking Consent Form for GroupId {int}")
	public void consumer_wants_to_generate_send_banking_consent_form_for_group_id(Integer groupId)
			throws JsonProcessingException {
		RequestSpecification requestSpecification = restClient.getRequestSpecification();

		MemberInfo memberInfo = MemberInfo.builder().email("bddtest@test.com").build();
		CustomerContact customerContact = CustomerContact.builder().name("ABC").email("abc@text.com").title("title")
				.phone("11223344").build();
		BankingConsentSignInRequest request = BankingConsentSignInRequest.builder().groupId(groupId.longValue())
				.groupNm("Test BDD Group").effectiveDt(new java.util.Date()).groupStateCd("MN").censusCount(40)
				.customerContact(List.of(customerContact)).memberInfo(memberInfo).build();
		requestSpecification.header(AsgwyConstantsTest.KEY_IDENT_TOKEN_KEY, AsgwyConstantsTest.KEY_IDENT_TOKEN_VALUES);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		String req = objectMapper.writeValueAsString(request);
		requestSpecification.body(req);

		successResponse = requestSpecification.post("/v1/quote/esign/banking-consent");

	}

My attribute is 

@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
	@NotNull(message = "EffectiveDt must not be null")
	private Date effectiveDt;


getting below error

 Could not resolve parameter [1] in public org.springframework.http.ResponseEntity<java.lang.String>
 com.aetna.asgwy.controller.PdfFormAdobeSignInController.generateAndSendBankingConsentForm(java.lang.String,com.aetna.asgwy.request.BankingConsentSignInRequest)
 throws com.fasterxml.jackson.core.JsonProcessingException: JSON parse error:
 Cannot deserialize value of type `java.util.Date` from String "03/28/2025": 
 not a valid representation (error: Failed to parse Date value '03/28/2025': 
 Cannot parse date "03/28/2025": not compatible with any of standard forms ("yyyy-MM-dd'T'HH:mm:ss.SSSX",
 "yyyy-MM-dd'T'HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd"))
 
 
 
 .HttpMessageNotReadableException: JSON parse error: Cannot deserialize value of type `java.util.Date`
 from String "03/28/2025": not a valid representation (error: Failed to parse Date value '03/28/2025':
 Cannot parse date "03/28/2025": not compatible with any of standard forms 
 ("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd"))]
