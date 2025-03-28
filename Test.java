package com.aetna.asgwy.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.aetna.asgwy.enumeration.PdfFormStatusEnum;
import com.aetna.asgwy.esign.dto.AdobeSignInRequest;
import com.aetna.asgwy.esign.dto.FileInfo;
import com.aetna.asgwy.esign.dto.MemberInfo;
import com.aetna.asgwy.esign.dto.ParticipantSetsInfo;
import com.aetna.asgwy.exception.AsgwyGlobalException;
import com.aetna.asgwy.repository.GroupFormStatusRepository;
import com.aetna.asgwy.repository.entity.GroupFormStatus;
import com.aetna.asgwy.request.BankingConsentSignInRequest;
import com.aetna.asgwy.service.AdobeSignInApiService;
import com.aetna.asgwy.service.PdfFormAdobeSignInService;
import com.aetna.asgwy.service.PdfFormGenerationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfFormAdobeSignInServiceImpl implements PdfFormAdobeSignInService {

	private static final String ALLINA_BANKING = "Allina-Health-Aetna-Funding-Advantage-Banking-Form";
	private static final String BANNER_AETNA_FUNDING_ADVANTAGE_BANKING_FORM = "Banner-Aetna-Funding-Advantage-Banking-Form";
	private static final String BANNER_AETNA_SGPLUS_BANKING_CONSENT = "Banner-Aetna-SGplus-Banking-Consent-Form";
	private static final String AETNA_FUNDING_BANKING_CONSENT = "Aetna-Funding-Advantage-Banking-Consent-Form";
	private static final String SGPLUS_BANKING_CONSENT = "SGplus-Banking-Consent-Form";
	private static final String FORM_EXTENSION = ".pdf";

	@Autowired
	PdfFormGenerationService pdfFormGenerationService;

	@Autowired
	AdobeSignInApiService adobeSignInApiService;

	@Autowired
	GroupFormStatusRepository groupFormStatusRepository;

	/**
	 * This Service method implementation is used to fill the banking form and Email
	 * to plan sponsor for sign and the details into groups_form_status table
	 * 
	 * @param BankingConsentSignInRequest
	 * @param String                      of requestedUserId
	 * @return String of Gateway Form id
	 */

	@Override
	public String generateAndSendBankingConsent(BankingConsentSignInRequest bankingConsentSignInRequest,
			String requestedUserId) {
		log.info("generateAndSendBankingConsent() method call");
		String groupStateCd = bankingConsentSignInRequest.getGroupStateCd();
		boolean isBetween2to50 = bankingConsentSignInRequest.getCensusCount() > 2
				&& bankingConsentSignInRequest.getCensusCount() <= 50;
		boolean isGreaterThan50 = bankingConsentSignInRequest.getCensusCount() > 50;
		String formNm = "";
		// AZ State with census count 2-50, then SG Banner Banking form
		if (groupStateCd.equalsIgnoreCase("MN")) {
			formNm = ALLINA_BANKING;
		}
		// AZ State with census count 2-50, then SG Banner Banking form
		else if (groupStateCd.equalsIgnoreCase("AZ") && isBetween2to50) {
			formNm = BANNER_AETNA_FUNDING_ADVANTAGE_BANKING_FORM;

		} // AZ State with 51+ census, then SGplus Banner Banking form 
		else if (groupStateCd.equalsIgnoreCase("AZ") && isGreaterThan50) {
			formNm = BANNER_AETNA_SGPLUS_BANKING_CONSENT;
		}
		// VA or all other states with 2-50 census, normal AFA banking form 
		else if (isBetween2to50) {
			formNm = AETNA_FUNDING_BANKING_CONSENT;
		}
		// VA or all other states with 51+ census, SGPlus AFA banking form.
		else if (isGreaterThan50) {
			formNm = SGPLUS_BANKING_CONSENT;
		} else {
			throw new AsgwyGlobalException(HttpStatus.BAD_REQUEST.value(),
					"Invalid JSON Request to send Banking Consent");
		}

		byte[] fileContent = pdfFormGenerationService.generateBankingConsentForm(bankingConsentSignInRequest, formNm);
		String fileName = formNm.replace("-", "_") + "_" + bankingConsentSignInRequest.getGroupId() + FORM_EXTENSION;

		if (fileContent == null) {
			throw new AsgwyGlobalException(HttpStatus.NOT_FOUND.value(), "Unable to generated pdf form");
		}
		// upload file on adobe and get transientDocumentId
		String transientDocumentId = adobeSignInApiService.uploadTransientDocument(fileContent, fileName);

		// using transientDocumentId call the createAgreementWithTransientDocument() to
		// send mail for sign
		if (transientDocumentId == null) {
			throw new AsgwyGlobalException(HttpStatus.NOT_FOUND.value(), "Unable to get transientDocumentId");
		}
		String agreementId = triggerBankingConsentEmail(transientDocumentId, bankingConsentSignInRequest, formNm);

		Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("America/New_York")));
		GroupFormStatus formStatus = GroupFormStatus.builder().groupId(bankingConsentSignInRequest.getGroupId())
				.formType(PdfFormStatusEnum.BANKING_FORM.getType())
				.psEmailId(bankingConsentSignInRequest.getMemberInfo().getEmail()).quoteEffectiveDt(new Date())
				.agreementId(agreementId).formNm(formNm).sentToPs(currentTime).createDts(currentTime)
				.updateDts(currentTime).formStatus(PdfFormStatusEnum.FORM_SENT.getType()).createUsrId(requestedUserId)
				.updateUsrId(requestedUserId).transientDocumentId(transientDocumentId).build();

		Long groupFormId = groupFormStatusRepository.save(formStatus).getGroupFormId();
		log.info("Form status inserted into table ::" + groupFormId);
		return "Banking consent form sent successfully. [groupFormId:" + groupFormId + "]";
	}

	private String triggerBankingConsentEmail(String transientDocumentId, BankingConsentSignInRequest signInRequest,
			String templateName) {
		FileInfo info = FileInfo.builder().transientDocumentId(transientDocumentId).build();
		MemberInfo memberInfo = signInRequest.getMemberInfo();
		ParticipantSetsInfo participantSetsInfo = ParticipantSetsInfo.builder().memberInfos(List.of(memberInfo))
				.order(1).role(PdfFormStatusEnum.ROLE_SIGNER.getType()).build();
		AdobeSignInRequest adobeSignRequest = AdobeSignInRequest.builder().fileInfos(List.of(info)).name(templateName)
				.participantSetsInfo(List.of(participantSetsInfo))
				.signatureType(PdfFormStatusEnum.SIGNATURETPE_ESING.getType())
				.state(PdfFormStatusEnum.FORM_STATE_IN_PROCESS.getType()).message(emailMessage()).build();
		return adobeSignInApiService.createAgreementWithTransientDocument(adobeSignRequest);

	}

	private String emailMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("\nThank you for applying for insurance with Accendo Insurance Company. ").append(
				"This email contains instructions for Applicant, Owner (if applicable) to apply electronic signature on the application and related documents.\n\n")
				.append("Instructions for Applicant(s)\n\nBefore you sign, please review the application for accuracy and completeness. ")
				.append("If you have any questions, contact your agent. Please do not reply to this e-mail. ")
				.append("To get started, please click on the 'Review and Sign' button above. Your unique password is the last 4 digits of your SSN.\n\n")
				.append("Instructions for Owner(if applicable)\n\nBefore you sign, please review the application for accuracy and completeness. ")
				.append("If you have any questions, contact your agent. Please do not reply to this e-mail. ")
				.append("To get started, please click on the 'Review and Sign' button above. Your unique password is the last 4 digits of your SSN.\n\n")
				.append("After all email signatures are completed, application will be automatically sent to our home office for processing");
		return builder.toString();
	}
}
