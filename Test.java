Here is the complete JUnit test case and a Cucumber BDD test case for the PdfFormAdobeSignInController.

⸻

JUnit Test Case

package com.aetna.asgwy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aetna.asgwy.constants.AsgwyConstants;
import com.aetna.asgwy.request.BankingConsentSignInRequest;
import com.aetna.asgwy.request.KeyIdentifiers;
import com.aetna.asgwy.service.PdfFormAdobeSignInService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PdfFormAdobeSignInControllerTest {

    @Mock
    private PdfFormAdobeSignInService pdfFormAdobeSignInService;

    @InjectMocks
    private PdfFormAdobeSignInController pdfFormAdobeSignInController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(pdfFormAdobeSignInController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGenerateAndSendBankingConsentForm() throws Exception {
        // Mock input data
        BankingConsentSignInRequest request = new BankingConsentSignInRequest();
        request.setGroupId("12345");
        String requestBody = objectMapper.writeValueAsString(request);

        KeyIdentifiers keyIdentifiers = new KeyIdentifiers();
        keyIdentifiers.setUsertype(AsgwyConstants.USER_TYPE_INTERNAL);
        keyIdentifiers.setAetnaid("testUser");
        String keyIdentifiersJson = objectMapper.writeValueAsString(keyIdentifiers);

        // Mock response from the service
        String mockResponse = "Banking consent form sent successfully.";
        when(pdfFormAdobeSignInService.generateAndSendBankingConsent(any(), eq("testUser")))
                .thenReturn(mockResponse);

        // Perform POST request
        mockMvc.perform(post("/v1/quote/esign/banking-consent")
                .header("tokenvals", keyIdentifiersJson)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(mockResponse));

        // Verify interaction with the service
        verify(pdfFormAdobeSignInService, times(1))
                .generateAndSendBankingConsent(any(BankingConsentSignInRequest.class), eq("testUser"));
    }
}



⸻

Cucumber BDD Test Case

Feature File (src/test/resources/features/PdfFormAdobeSignIn.feature)

Feature: PdfFormAdobeSignInController API

  Scenario: Generate and send banking consent form
    Given the service is running
    And I have a valid keyIdentifiers JSON
    And I have a valid BankingConsentSignInRequest
    When I POST the data to "/v1/quote/esign/banking-consent"
    Then I should receive a 201 status code
    And the response message should be "Banking consent form sent successfully."

Step Definitions (PdfFormAdobeSignInSteps.java)

package com.aetna.asgwy.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.aetna.asgwy.constants.AsgwyConstants;
import com.aetna.asgwy.request.BankingConsentSignInRequest;
import com.aetna.asgwy.request.KeyIdentifiers;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class PdfFormAdobeSignInSteps {

    @Autowired
    private RestTemplate restTemplate;

    private String url;
    private HttpHeaders headers;
    private BankingConsentSignInRequest requestBody;
    private ResponseEntity<String> response;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Given("the service is running")
    public void theServiceIsRunning() {
        url = "http://localhost:8080/v1/quote/esign/banking-consent";
    }

    @And("I have a valid keyIdentifiers JSON")
    public void iHaveAValidKeyIdentifiersJSON() throws Exception {
        KeyIdentifiers keyIdentifiers = new KeyIdentifiers();
        keyIdentifiers.setUsertype(AsgwyConstants.USER_TYPE_INTERNAL);
        keyIdentifiers.setAetnaid("testUser");
        headers = new HttpHeaders();
        headers.add("tokenvals", objectMapper.writeValueAsString(keyIdentifiers));
    }

    @And("I have a valid BankingConsentSignInRequest")
    public void iHaveAValidBankingConsentSignInRequest() {
        requestBody = new BankingConsentSignInRequest();
        requestBody.setGroupId("12345");
    }

    @When("I POST the data to {string}")
    public void iPostTheDataTo(String endpoint) {
        response = restTemplate.postForEntity(url, requestBody, String.class, headers);
    }

    @Then("I should receive a {int} status code")
    public void iShouldReceiveAStatusCode(int statusCode) {
        assertThat(response.getStatusCodeValue(), equalTo(statusCode));
    }

    @And("the response message should be {string}")
    public void theResponseMessageShouldBe(String expectedMessage) {
        assertThat(response.getBody(), equalTo(expectedMessage));
    }
}



⸻

These test cases cover the controller’s functionality and validate the behavior through both unit and BDD testing.
