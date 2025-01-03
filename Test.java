verify(quoteDocumentRepository, times(1)).updateDocStatus(eq(1l),eq("STAGE_AFS"),eq("JUNIT"),eq(currentTime), eq("STAGE_TEMP"));


Argument(s) are different! Wanted:
quoteDocumentRepository.updateDocStatus(
    1L,
    "STAGE_AFS",
    "JUNIT",
    2025-01-03 04:04:57.3032187,
    "STAGE_TEMP"
);
-> at com.aetna.asgwy.service.MissingInfoServiceImplTest.testUpdateQuoteAndDocStatus_Success(MissingInfoServiceImplTest.java:225)
Actual invocations have different arguments at positions [2, 3]:
quoteDocumentRepository.updateDocStatus(
    1L,
    "STAGE_AFS",
    "Junit",
    2025-01-03 04:04:57.3662193,
    "STAGE_TEMP"
);
