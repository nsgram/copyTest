@Test
	void testGenerateAndSendBankingConsentAZand2To50Success() {
		byte[] fileContent = "PDF Content".getBytes();
		String transientDocumentId = "transient123";
		String agreementId = "agreementId123";

		when(pdfFormGenerationService.generateBankingConsentForm(any(), eq("Banner-Aetna-Funding-Advantage-Banking-Form")))
				.thenReturn(fileContent);
		when(adobeSignInApiService.uploadTransientDocument(any(),
				eq("Banner-Aetna-Funding-Advantage-Banking-Form_1234.pdf"))).thenReturn(transientDocumentId);
		when(adobeSignInApiService.createAgreementWithTransientDocument(any())).thenReturn(agreementId);
		when(groupFormStatusRepository.save(any())).thenReturn(groupFormStatus);

		String response = pdfFormAdobeSignInServiceImpl.generateAndSendBankingConsent(request, "testUser");
		assertEquals("Banking consent form sent successfully. [groupFormId:1]", response);
	}

getting below error

com.aetna.asgwy.exception.AsgwyGlobalException: Unable to get transientDocumentId
	at com.aetna.asgwy.service.impl.PdfFormAdobeSignInServiceImpl.generateAndSendBankingConsent(PdfFormAdobeSignInServiceImpl.java:102)
	at com.aetna.asgwy.service.PdfFormAdobeSignInServiceTest.testGenerateAndSendBankingConsentAZand2To50Success(PdfFormAdobeSignInServiceTest.java:65)
	
