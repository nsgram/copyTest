@Test
void testGenerateAndSendBankingConsentAZand2To50Success() {
    // Prepare test data
    request.setGroupStateCd("AZ");
    request.setCensusCount(25);

    byte[] fileContent = "PDF Content".getBytes();
    String transientDocumentId = "transient123";
    String agreementId = "agreementId123";

    // Mock dependencies
    when(pdfFormGenerationService.generateBankingConsentForm(any(), eq("Banner-Aetna-Funding-Advantage-Banking-Form")))
            .thenReturn(fileContent);
    when(adobeSignInApiService.uploadTransientDocument(eq(fileContent),
            eq("Banner_Aetna_Funding_Advantage_Banking_Form_1234.pdf"))).thenReturn(transientDocumentId);
    when(adobeSignInApiService.createAgreementWithTransientDocument(any())).thenReturn(agreementId);

    GroupFormStatus groupFormStatus = GroupFormStatus.builder().groupFormId(1L).build();
    when(groupFormStatusRepository.save(any())).thenReturn(groupFormStatus);

    // Call the method under test
    String response = pdfFormAdobeSignInServiceImpl.generateAndSendBankingConsent(request, "testUser");

    // Assertions
    assertEquals("Banking consent form sent successfully. [groupFormId:1]", response);

    // Verify interactions
    verify(pdfFormGenerationService).generateBankingConsentForm(request, "Banner-Aetna-Funding-Advantage-Banking-Form");
    verify(adobeSignInApiService).uploadTransientDocument(eq(fileContent),
            eq("Banner_Aetna_Funding_Advantage_Banking_Form_1234.pdf"));
    verify(adobeSignInApiService).createAgreementWithTransientDocument(any());
    verify(groupFormStatusRepository).save(any());
}
