The last deleted message included an incomplete JUnit test for the PdfFormGenerationServiceImpl class. It was intended to test the generateBankingConsentForm method for scenarios with one contact and two contacts, using mocked inputs and checking for valid outputs.

Let me rewrite and complete the JUnit test for 100% code coverage. Here’s the corrected version:

⸻

Complete JUnit Test Code

package com.aetna.asgwy.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.aetna.asgwy.esign.dto.CustomerContact;
import com.aetna.asgwy.exception.AsgwyGlobalException;
import com.aetna.asgwy.request.BankingConsentSignInRequest;

class PdfFormGenerationServiceImplTest {

    @InjectMocks
    private PdfFormGenerationServiceImpl pdfFormGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateBankingConsentForm_Success_OneContact() {
        BankingConsentSignInRequest signInRequest = mockRequest(1);
        String pdfTemplate = "Banner-Aetna-SGplus-Banking-Consent-Form";

        byte[] result = pdfFormGenerationService.generateBankingConsentForm(signInRequest, pdfTemplate);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGenerateBankingConsentForm_Success_TwoContacts() {
        BankingConsentSignInRequest signInRequest = mockRequest(2);
        String pdfTemplate = "SGplus-Banking-Consent-Form";

        byte[] result = pdfFormGenerationService.generateBankingConsentForm(signInRequest, pdfTemplate);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGenerateBankingConsentForm_TemplateNotFound() {
        BankingConsentSignInRequest signInRequest = mockRequest(1);
        String invalidTemplate = "InvalidTemplate";

        Exception exception = assertThrows(AsgwyGlobalException.class, () -> {
            pdfFormGenerationService.generateBankingConsentForm(signInRequest, invalidTemplate);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), ((AsgwyGlobalException) exception).getStatusCode());
    }

    @Test
    void testSetFormDataForOneContact_Exception() {
        BankingConsentSignInRequest signInRequest = mockRequest(2); // Exceeding contact size for error
        String pdfTemplate = "Banner-Aetna-SGplus-Banking-Consent-Form";

        Exception exception = assertThrows(AsgwyGlobalException.class, () -> {
            pdfFormGenerationService.generateBankingConsentForm(signInRequest, pdfTemplate);
        });

        assertTrue(exception.getMessage().contains("Unexpected request size of customer contact"));
    }

    @Test
    void testSetFormDataForTwoContact_Exception() {
        BankingConsentSignInRequest signInRequest = mockRequest(3); // Exceeding contact size for error
        String pdfTemplate = "SGplus-Banking-Consent-Form";

        Exception exception = assertThrows(AsgwyGlobalException.class, () -> {
            pdfFormGenerationService.generateBankingConsentForm(signInRequest, pdfTemplate);
        });

        assertTrue(exception.getMessage().contains("Unexpected request size of customer contact"));
    }

    private BankingConsentSignInRequest mockRequest(int contactCount) {
        BankingConsentSignInRequest request = new BankingConsentSignInRequest();
        request.setGroupNm("Test Group");
        request.setEffectiveDt(new Date());

        CustomerContact contact1 = new CustomerContact();
        contact1.setName("Contact 1");
        contact1.setTitle("Manager");
        contact1.setPhone("1234567890");
        contact1.setEmail("contact1@test.com");

        CustomerContact contact2 = new CustomerContact();
        contact2.setName("Contact 2");
        contact2.setTitle("Director");
        contact2.setPhone("9876543210");
        contact2.setEmail("contact2@test.com");

        if (contactCount == 1) {
            request.setCustomerContact(Arrays.asList(contact1));
        } else if (contactCount == 2) {
            request.setCustomerContact(Arrays.asList(contact1, contact2));
        } else {
            request.setCustomerContact(Arrays.asList(contact1, contact2, contact1)); // Invalid for testing
        }

        return request;
    }
}



⸻

Test Case Coverage
	•	Positive Scenarios:
	•	Successfully generates the PDF for one contact.
	•	Successfully generates the PDF for two contacts.
	•	Negative Scenarios:
	•	Throws AsgwyGlobalException when the template is not found.
	•	Throws AsgwyGlobalException when more contacts than expected are provided.

⸻

This should provide 100% code coverage for the PdfFormGenerationServiceImpl class.
