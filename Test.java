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
		requestSpecification.body(request);

		successResponse = requestSpecification.post("/v1/quote/esign/banking-consent");

	}
getting Below error

 Resolved [org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Cannot deserialize value of type `java.util.Date` from String "03/28/2025": not a valid representation (error: Failed to parse Date value '03/28/2025': Cannot parse date "03/28/2025": not compatible with any of standard forms ("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyy-MM-dd"))]
