Java Code


@PostMapping(value = "/esign/create-proposal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> createAgreement(@RequestPart("file") MultipartFile file,
			@RequestPart("data") AdobeSignRequest adobeSignRequest) {

		return null;

	}


Request
curl -X 'POST' \
  'http://localhost:9080/asgwy-api/v1/quote/esign/create-proposal' \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@document.pdf;type=application/pdf' \
  -F 'data={
  "fileInfos": [
    {
      "document": null,
      "label": null,
      "libraryDocumentId": null,
      "transientDocumentId": "CBSCTBABDUAAABACAABAAYCRsRIeWS24wk7PjadWLKZtbj7KxmJSgC9MiIXHdjLF3kRRzR2epvPdsslBowlyTwUEVs7lmaIxynTfiS-usNfT6hEa1ctLgQbNl4ku6YOW36IF4IIvAl3YGDPXh3BdSEOqEcQYs3z5Q2HsMHG4YqsQDez99P0PSKIf8X-qxdD02a7O8ihbFzlPEMNw6JuKKw3gb5ED01vZ66EyUXOhJPIMnTi6trg6LnpmjbII6yX3dhClQtICIo2OpZBmaU5iuAQSezfKYNxDkeqHzr54qMNCtu1AFAbirAg-cMPLyepDoMC1NF2OCyc0xFromxQA5Bi0bn2366kge5ZTxts49KYJouYsUmWsQtN5EkCb6AQ0*",
      "urlFileInfo": null
    }
  ],
  "name": " - Application for insurance",
  "participantSetsInfo": [
    {
      "memberInfos": [
        {
          "email": "lavater@aetna.com",
          "securityOption": {
            "phoneInfo": {
              "countryCode": "+1",
              "countryIsoCode": "00",
              "phone": "3331212121"
            }
          }
        }
      ],
      "order": 1,
      "role": "SIGNER",
      "label": null,
      "name": null,
      "privateMessage": null,
      "visiblePages": [
        null
      ]
    },
	{
      "memberInfos": [
        {
          "email": "lavater@aetna.com",
          "securityOption": {
            "phoneInfo": {
              "countryCode": "+1",
              "countryIsoCode": "22",
              "phone": "222222222"
            }
          }
        }
      ],
      "order": 2,
      "role": "SIGNER",
      "label": null,
      "name": null,
      "privateMessage": null,
      "visiblePages": [
        null
      ]
    }
  ],
  "signatureType": "ESIGN",
  "state": "IN_PROCESS",
  "ccs": [
    {
      "email": "lavater@aetna.com",
      "label": null,
      "visiblePages": [
        null
      ]
    }
  ],
  "createdDate": null,
  "deviceInfo": null,
  "documentVisibilityEnabled": null,
  "emailOption": null,
  "expirationTime": null,
  "externalId": null,
  "firstReminderDelay": null,
  "formFieldLayerTemplates": null,
  "groupId": null,
  "id": null,
  "isDocumentRetentionApplied": null,
  "locale": null,
  "mergeFieldInfo": null,
  "message": "\nThank you for applying for insurance with Accendo Insurance Company. This email contains instructions for Applicant, Owner (if applicable) to apply electronic signature on the application and related documents.\n\nInstructions for Applicant(s)\n\nBefore you sign, please review the application for accuracy and completeness. If you have any questions, contact your agent. Please do not reply to this e-mail. To get started, please click on the '\''Review and Sign'\'' button above. Your unique password is the last 4 digits of your SSN.\n\nInstructions for Owner(if applicable)\n\nBefore you sign, please review the application for accuracy and completeness. If you have any questions, contact your agent. Please do not reply to this e-mail. To get started, please click on the '\''Review and Sign'\'' button above. Your unique password is the last 4 digits of your SSN.\n\nAfter all email signatures are completed, application will be automatically sent to our home office for processing",
  "postSignOption": null,
  "reminderFrequency": null,
  "securityOption": null,
  "senderEmail": null,
  "status": null,
  "vaultingInfo": null,
  "workflowId": null
}'
response
{
  "type": "about:blank",
  "title": "Unsupported Media Type",
  "status": 415,
  "detail": "Content-Type 'application/octet-stream' is not supported.",
  "instance": "/asgwy-api/v1/quote/esign/create-proposal"
}
