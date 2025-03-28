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
		String req = objectMapper.writeValueAsString(request);
		requestSpecification.body(req);

		successResponse = requestSpecification.post("/v1/quote/esign/banking-consent");

	}
