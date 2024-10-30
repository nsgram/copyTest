@GetMapping("/latestforgroup")
	@Operation(summary = "Retrieve recent quote details", description = "Group and Quote must exist")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<DashboardResponse> getRecentQuotesForEachGroup(
			@Schema(example = "ILLUS/MEDUW", description = "Quote Type") @RequestParam(value="quoteType", required=false) String quoteType,
			@RequestHeader("keyIdentifiers") String keyIdentifiers) throws Exception {
		log.info("DashboardController Class - /recentquotes Service");
		KeyIdentifiers key=objMapper.readValue(keyIdentifiers, KeyIdentifiers.class);
		key.setPerformerRole(key.getPerformerRole().toUpperCase(Locale.ENGLISH));
		if (!isValidPerformerRole(key.getPerformerRole()))
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);

		DashboardResponse dashboardResponse = quotesService.getRecentQuotesForEachGroup(quoteType, key);
		return new ResponseEntity<>(dashboardResponse, HttpStatus.OK);
	}



public DashboardResponse getRecentQuotesForEachGroup(String quoteType, KeyIdentifiers keyIdentifiers) {
		log.info("QuotesServiceImpl - getRecentQuotesForEachGroup()");
		DashboardResponse dashboardResponse = new DashboardResponse();
		List<DashboardDTO> groupDTOList = new ArrayList<>();
		try {
			if (StringUtils.isNotBlank(quoteType)) {
				groupDTOList = entityManager.createNamedQuery("LatestQuotesByType", DashboardDTO.class)
						.setParameter(1, quoteType).getResultList();
				if (!CollectionUtils.isEmpty(groupDTOList)) {
					groupDTOList.stream().forEach(dashboard -> dashboard.setQuoteActions(
							getSpecticActions(dashboard.getQuoteStatusCd(), keyIdentifiers.getPerformerRole())));
				}
			} else {
				log.info("getRecentQuotesForEachGroup: By All Quote Types");
				groupDTOList = entityManager.createNamedQuery("LatestQuotes", DashboardDTO.class).getResultList();
				if (!CollectionUtils.isEmpty(groupDTOList)) {
					groupDTOList.stream().forEach(dashboard -> dashboard.setQuoteActions(
							getSpecticActions(dashboard.getQuoteStatusCd(), keyIdentifiers.getPerformerRole())));
				}
			}
		} catch (Exception exception) {
			log.error("Exception Occurred on getRecentQuotesForEachGroup(): " + exception.getLocalizedMessage());
		}
		dashboardResponse.setDashboardDTOList(groupDTOList);
		return dashboardResponse;
	}


public class KeyIdentifiers {
	private String performerId;//proxyIdOfBroker
	private String performerAgencyId;//proxyIdOfRelatedAgency
	private String performerType;//EXTERNAL/INTERNAL
	private String performerRole;//SUPERUSER/READONLY/BROKER
	private String performerPrivilege;//SHARED/NONSHARED
}


@Test
	void testGetAllGroups() throws Exception {

		ObjectMapper objMapper=new ObjectMapper();
		KeyIdentifiers key=objMapper.readValue(stringKey, KeyIdentifiers.class);
		
		quotesController.getRecentQuotesForEachGroup(qyoteType, stringKey);
		then(quotesService).should(atLeastOnce()).getRecentQuotesForEachGroup(qyoteType, key);
	}


gettting below error in above test


rgument(s) are different! Wanted:
quotesService.getRecentQuotesForEachGroup(
    "ILLUS",
    KeyIdentifiers(performerId=2993443, performerAgencyId=288888, performerType=EXTERNAL, performerRole=SUPERUSER, performerPrivilege=SHARED)
);
-> at com.aetna.asgwy.controller.QuotesControllerTest.testGetAllGroups(QuotesControllerTest.java:63)
Actual invocations have different arguments at position [1]:
quotesService.getRecentQuotesForEachGroup(
    "ILLUS",
    KeyIdentifiers(performerId=2993443, performerAgencyId=288888, performerType=EXTERNAL, performerRole=SUPERUSER, performerPrivilege=SHARED)
);
-> at com.aetna.asgwy.controller.QuotesController.getRecentQuotesForEachGroup(QuotesController.java:59)

	at com.aetna.asgwy.controller.QuotesControllerTest.testGetAllGroups(QuotesControllerTest.java:63)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
