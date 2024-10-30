@Test
void testGetAllGroups() throws Exception {
    String keyString = "{\"performerId\": \"2993443\", \"performerAgencyId\": \"288888\", \"performerType\": \"EXTERNAL\", \"performerRole\": \"BROKER\", \"performerPrivilege\": \"SHARED\"}";

    ObjectMapper objMapper = new ObjectMapper();

    // Parse keyString to KeyIdentifiers to verify the structure
    KeyIdentifiers keyIdentifiers = objMapper.readValue(keyString, KeyIdentifiers.class);

    // Mock ObjectMapper's behavior to return a KeyIdentifiers object
    when(objMapper.readValue(anyString(), KeyIdentifiers.class)).thenReturn(keyIdentifiers);

    // Invoke the controller method
    quotesController.getRecentQuotesForEachGroup(qyoteType, keyString);

    // Verify the service method call
    then(quotesService).should(atLeastOnce()).getRecentQuotesForEachGroup(qyoteType, performerTypeSuperUser, keyIdentifiers);
}
