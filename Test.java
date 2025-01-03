Argument(s) are different! Wanted:
quoteDocumentRepository.updateDocStatus(
    <any long>,
    <any string>,
    <any string>,
    2025-01-03 03:54:08.3921773,
    <any string>
);
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:225)
Actual invocations have different arguments at position [3]:
quoteDocumentRepository.updateDocStatus(
    1L,
    "STAGE_AFS",
    "Junit",
    2025-01-03 03:54:08.4491766,
    "STAGE_TEMP"
);
-> at com.aetna.asgwy.service.impl.MissingInfoServiceImpl.updateMissingInfoStatus(MissingInfoServiceImpl.java:106
