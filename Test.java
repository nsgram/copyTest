package com.aetna.asgwy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aetna.asgwy.constants.AsgwyConstants;
import com.aetna.asgwy.request.BankingConsentSignInRequest;
import com.aetna.asgwy.request.KeyIdentifiers;
import com.aetna.asgwy.service.PdfFormAdobeSignInService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/quote")
@Slf4j
public class PdfFormAdobeSignInController {
	@Autowired
	PdfFormAdobeSignInService pdfFormAdobeSignInService;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * This API is used to fill the banking form and Email to plan sponsor for sign
	 * 
	 * @param BankingConsentSignInRequest
	 * @return ResponseEntity<String>
	 */

	@PostMapping(value = "/esign/banking-consent", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> generateAndSendBankingConsentForm(
			@Schema(example = AsgwyConstants.HEADER_KEY_INTERNAL, description = "request header") @RequestHeader("tokenvals") String keyIdentifiers,
			@RequestBody BankingConsentSignInRequest bankingConsentSignInRequest) throws JsonProcessingException {
		log.info("banking form consent started for ::{}", bankingConsentSignInRequest.getGroupId());
		KeyIdentifiers key = mapper.readValue(keyIdentifiers, KeyIdentifiers.class);

		String requestedUserId = null;
		if (key.getUsertype().equalsIgnoreCase(AsgwyConstants.USER_TYPE_EXTERNAL)) {
			requestedUserId = key.getProxyid();
		} else if (key.getUsertype().equalsIgnoreCase(AsgwyConstants.USER_TYPE_INTERNAL)) {
			requestedUserId = key.getAetnaid();
		}
		String responseMsg = pdfFormAdobeSignInService.generateAndSendBankingConsent(bankingConsentSignInRequest,
				requestedUserId);
		return new ResponseEntity<>(responseMsg, HttpStatus.CREATED);
	}
}
