Request 1
curl -X 'GET' \
  'https://aetna.na1.adobesign.com/api/rest/v6/baseUris' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer 3AAABLblqZhAGDFfvYVDORbzoUnKCkbG7PeSsy3wlPkAWvORfmQbE5OQZxpEzbJAB7BRA3_3qYeUqkWyvNkITfabRokoDZElC'
Response
{
  "apiAccessPoint": "https://api.na2.documents.adobe.com/",
  "webAccessPoint": "https://na2.documents.adobe.com/"
}

Request 2
curl -X 'POST' \
  'https://api.na2.documents.adobe.com/api/rest/v6/transientDocuments' \
  -H 'accept: application/json' \
  -H 'Content-Type: multipart/form-data' \
  -H 'Authorization: Bearer 3AAABLblqZhAGDFfvYVDORbzoUnKCkbG7PeSsy3wlPkAWvORfmQbE5OQZxpEzbJAB7BRA3_3qYeUqkWyvNkITfabRokoDZElC' \
  -F 'File-Name=Proposal' \
  -F 'File=@Proposal.pdf;type=application/pdf'
Response
{
  "transientDocumentId": "CBSCTBABDUAAABACAABAAeZ4mh3uIRO6nLuRJLKl5fpVy6oLf_OPbZvBd4mTYBXzIT7rNs5I-VaNYUzEBZ39mKO4qlZziB7XF3wLWEGq5l2OmlS8BWYfw4LqJuULDhlcgHtvVQOSot2OHxVGwRcfAEJmcqCZ4Qx-N3E-mDPW5T3lm6QkjHpfxhuy6Ix5xFkYPtZoz_mZHfoBB9kKX_d4R98mP9xrTk1-XYDyuta0mZC-d8TxldOyOJKbvbHj9ZR1AnS-phmKBGRS0YdbNs5AUzB57BPYXtZgSvn9DivZTwTytpyeIjriJnaiAV8DMBIJymWZLoprJK2a5RZzatj3G_jeyeCO3oUZ2i8nI8TqFo2KSyLN6FnDhzihKwOKiHfw*"
}

Request 3
curl -X 'POST' \
  'https://api.na2.documents.adobe.com/api/rest/v6/agreements' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer 3AAABLblqZhAGDFfvYVDORbzoUnKCkbG7PeSsy3wlPkAWvORfmQbE5OQZxpEzbJAB7BRA3_3qYeUqkWyvNkITfabRokoDZElC' \
  -d '{
    "fileInfos": [
        {
            "document": null,
            "label": null,
            "libraryDocumentId": null,
            "transientDocumentId": "CBSCTBABDUAAABACAABAAeZ4mh3uIRO6nLuRJLKl5fpVy6oLf_OPbZvBd4mTYBXzIT7rNs5I-VaNYUzEBZ39mKO4qlZziB7XF3wLWEGq5l2OmlS8BWYfw4LqJuULDhlcgHtvVQOSot2OHxVGwRcfAEJmcqCZ4Qx-N3E-mDPW5T3lm6QkjHpfxhuy6Ix5xFkYPtZoz_mZHfoBB9kKX_d4R98mP9xrTk1-XYDyuta0mZC-d8TxldOyOJKbvbHj9ZR1AnS-phmKBGRS0YdbNs5AUzB57BPYXtZgSvn9DivZTwTytpyeIjriJnaiAV8DMBIJymWZLoprJK2a5RZzatj3G_jeyeCO3oUZ2i8nI8TqFo2KSyLN6FnDhzihKwOKiHfw*",
            "urlFileInfo": null
        }
    ],
    "name": " - Application for insurance",
    "participantSetsInfo": [
        {
            "memberInfos": [
                {
                    "email":  "lavater@aetna.com",
                    "securityOption": {
                        "phoneInfo": {
                            "countryCode": "+1",
                            "countryIsoCode": null,
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
            "visiblePages": null
        },
        {
            "memberInfos": [
                {
                    "email": "lavater@aetna.com",
                    "securityOption": {
                        "phoneInfo": {
                            "countryCode": "+1",
                            "countryIsoCode": null,
                            "phone": "3331212121"
                        }
                    }
                }
            ],
            "order": 2,
            "role": "SIGNER",
            "label": null,
            "name": null,
            "privateMessage": null,
            "visiblePages": null
        }
    ],
    "signatureType": "ESIGN",
    "state": "IN_PROCESS",
    "ccs": [
        {
            "email": "lavater@aetna.com",
            "label": null,
            "visiblePages": null
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
}
'
Response
{
  "id": "CBJCHBCAABAA99jurZPdI2CVydkQuFrF8dCz5F8q3rXQ"
}

I want to integrate above rest api in the Spring boot applications rest api
response of first request in an input of request second
response of Second request in an input of request third

I want created new post endpoint in spring boot apllication 
Use latest java api
remove all sonar and check marx issue
