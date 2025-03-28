package com.aetna.asgwy.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.aetna.asgwy.esign.dto.CustomerContact;
import com.aetna.asgwy.exception.AsgwyGlobalException;
import com.aetna.asgwy.request.BankingConsentSignInRequest;
import com.aetna.asgwy.service.PdfFormGenerationService;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfFormGenerationServiceImpl implements PdfFormGenerationService {

	private static final String SOURE_DIR = "pdfTemplates/";
	private static final String FORM_EXTENSION = ".pdf";
	private static final String BANNER_AETNA_SGPLUS_BANKING_CONSENT = "Banner-Aetna-SGplus-Banking-Consent-Form";
	private static final String SGPLUS_BANKING_CONSENT = "SGplus-Banking-Consent-Form";

	/**
	 * This Service method implementation is used to fill the banking create PDF
	 * 
	 * @param BankingConsentSignInRequest
	 * @return byte[] of file content
	 */

	@Override
	public byte[] generateBankingConsentForm(BankingConsentSignInRequest signInRequest, String pdfTemplate) {
		log.info("Pdf form generation started for ::{}", pdfTemplate);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			InputStream templateStream = getClass().getClassLoader()
					.getResourceAsStream(SOURE_DIR + pdfTemplate + FORM_EXTENSION);
			if (templateStream == null) {
				log.error("Pdf template not found ::{}", pdfTemplate);
				throw new FileNotFoundException("Template PDF not found.");
			}
			PdfReader reader = new PdfReader(templateStream);
			PdfStamper stamper = new PdfStamper(reader, outputStream);
			if (pdfTemplate.equalsIgnoreCase(BANNER_AETNA_SGPLUS_BANKING_CONSENT)
					|| pdfTemplate.equalsIgnoreCase(SGPLUS_BANKING_CONSENT)) {
				setFormDataForOneContact(signInRequest, stamper);
			} else {
				setFormDataForTwoContact(signInRequest, stamper);
			}

			stamper.setFormFlattening(false);
			stamper.close();
			reader.close();
			/* un commnet for local testing
			String temppath = "src/main/resources/generated/";
			Files.createDirectories(Paths.get(temppath));
			String fileName = pdfTemplate.replace(FORM_EXTENSION, "") + System.currentTimeMillis() + FORM_EXTENSION;
			String filePath = temppath + fileName;
			Files.write(Paths.get(filePath.replace(SOURE_DIR, "")), outputStream.toByteArray());*/
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new AsgwyGlobalException(HttpStatus.NOT_FOUND.value(), e.getLocalizedMessage());
		}
	}

	private void setFormDataForOneContact(BankingConsentSignInRequest signInRequest, PdfStamper stamper)
			throws IOException {
		log.info("setFormDataForOneContact() started execution");
		AcroFields formFields = stamper.getAcroFields();

		formFields.setField("Customer Name", signInRequest.getGroupNm());// Customer Name
		formFields.setField("Effective Date", getMMddyyyyDate(signInRequest.getEffectiveDt()));// Effective date
		List<CustomerContact> customerContacts = signInRequest.getCustomerContact();
		if (customerContacts != null && !customerContacts.isEmpty()) {
			IntStream.range(0, customerContacts.size()).forEach(i -> {
				CustomerContact customerContact = customerContacts.get(i);
				try {
					if (i == 0) {
						// customer contact 1
						formFields.setField("Contact 1 1", customerContact.getName());// name
						formFields.setField("Contact 1 2", customerContact.getTitle());// title
						formFields.setField("Contact 1 3", customerContact.getPhone());// phone
						formFields.setField("Contact 1 4", customerContact.getEmail());// email
					} else {
						log.error("customer contact size expected 1 but " + i);
						throw new AsgwyGlobalException(400, "Unexpected request size of customer contact is :: " + i);
					}
				} catch (DocumentException | IOException e) {
					log.error("Exception raised in setFormDataForOneContact :::" + e.getMessage());
					throw new AsgwyGlobalException(400,
							"Exception catched in setFormDataForOneContact ::" + e.getLocalizedMessage());
				}
			});
		}
		log.info("setFormDataForOneContact() end");
	}

	private void setFormDataForTwoContact(BankingConsentSignInRequest signInRequest, PdfStamper stamper)
			throws IOException {
		log.info("setFormDataForTwoContact() started execution");
		AcroFields formFields = stamper.getAcroFields();
		formFields.setField("Text1", signInRequest.getGroupNm());// Customer Name
		formFields.setField("Text2", getMMddyyyyDate(signInRequest.getEffectiveDt()));// Effective date
		List<CustomerContact> customerContacts = signInRequest.getCustomerContact();
		if (customerContacts != null && !customerContacts.isEmpty()) {
			IntStream.range(0, customerContacts.size()).forEach(i -> {
				CustomerContact customerContact = customerContacts.get(i);
				try {
					if (i == 0) {
						// customer contact 1
						formFields.setField("Text6", customerContact.getName());// name
						formFields.setField("Text7", customerContact.getTitle());// title
						formFields.setField("Text8", customerContact.getPhone());// phone
						formFields.setField("Text9", customerContact.getEmail());// email
					} else if (i == 1) {
						// customer contact 2
						formFields.setField("Text6a", customerContact.getName());// name
						formFields.setField("Text7a", customerContact.getTitle());// title
						formFields.setField("Text8a", customerContact.getPhone());// phone
						formFields.setField("Text9a", customerContact.getEmail());// email
					} else {
						log.error("customer contact size expected 2 but " + i);
						throw new AsgwyGlobalException(400, "Unexpected request size of customer contact is ::" + i);
					}
				} catch (DocumentException | IOException e) {
					log.error("Exception raised in setFormDataForTwoContact ::: " + e.getMessage());
					throw new AsgwyGlobalException(400,
							"Exception catched in setFormDataForTwoContact ::" + e.getLocalizedMessage());
				}
			});
		}
		log.info("setFormDataForTwoContact() end");
	}

	private String getMMddyyyyDate(Date date) {
		return new SimpleDateFormat("MM/dd/yyyy").format(date);
	}
}

Write a complete JUnit testcase for above class code coverage shold be 100%
