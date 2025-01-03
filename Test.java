repo method like below

@Transactional
	@Modifying
	@Query(value = "update {h-schema}quote_document qd set doc_status =:newStatus, update_usr_id =:updatedUsrId, update_dts =:updateDts where qd.quote_id =:quoteId and qd.doc_status =:currentStatus ", nativeQuery = true)
	int updateDocStatus(@Param("quoteId") Long quoteId, @Param("newStatus") String newStatus,
			@Param("updatedUsrId") String updatedUsrId, @Param("updateDts") Timestamp updateDts,
			@Param("currentStatus") String currentStatus);


Calling test method like below
Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("America/New_York")));
verify(quoteDocumentRepository, times(1)).updateDocStatus(anyLong(),anyString(), anyString(), currentTime, anyString());



getting below error
Invalid use of argument matchers!
5 matchers expected, 4 recorded:
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:224)
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:224)
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:224)
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:224)

This exception may occur if matchers are combined with raw values:
    //incorrect:
    someMethod(any(), "raw String");
When using matchers, all arguments have to be provided by matchers.
For example:
    //correct:
    someMethod(any(), eq("String by matcher"));

For more info see javadoc for Matchers class.

	at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:224)
