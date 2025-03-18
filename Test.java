{
  "groupId": "string",
  "locale": "string",
  "type": "AGREEMENT",
  "vaultingInfo": {
    "enabled": true
  },
  "securityOption": {
    "contentProtectionPreference": {
      "external": "ENABLE",
      "internal": "ENABLE"
    },
    "openPassword": "string"
  },
  "postSignOption": {
    "redirectUrl": "string",
    "redirectDelay": 0
  },
  "notaryInfo": {
    "note": "string",
    "notaryEmail": "string",
    "notaryType": "PROVIDER_NOTARY",
    "appointment": "string",
    "payment": "BY_SENDER"
  },
  "ccs": [
    {
      "visiblePages": [
        "string"
      ],
      "label": "string",
      "ccSigningOrder": [
        "string"
      ],
      "email": "string"
    }
  ],
  "senderSigns": "FIRST",
  "documentVisibilityEnabled": true,
  "isDocumentRetentionApplied": true,
  "documentRetentionAppliedDate": "string",
  "hasSignerIdentityReport": true,
  "lastEventDate": "string",
  "senderEmail": "string",
  "id": "string",
  "state": "AUTHORING",
  "mergeFieldInfo": [
    {
      "fieldName": "string",
      "defaultValue": "string"
    }
  ],
  "firstReminderDelay": 0,
  "agreementSettingsInfo": {
    "hipaaEnabled": true,
    "canEditFiles": true,
    "canEditAgreementSettings": true,
    "canEditElectronicSeals": true
  },
  "emailOption": {
    "sendOptions": {
      "initEmails": "ALL",
      "inFlightEmails": "ALL",
      "completionEmails": "ALL"
    }
  },
  "formFieldGenerators": [
    {
      "formFieldNamePrefix": "string",
      "participantSetName": "string",
      "formFieldDescription": {
        "radioCheckType": "CIRCLE",
        "borderColor": "string",
        "valueExpression": "string",
        "maskingText": "string",
        "defaultValue": "string",
        "masked": true,
        "minLength": 0,
        "origin": "AUTHORED",
        "tooltip": "string",
        "hiddenOptions": [
          "string"
        ],
        "required": true,
        "validationData": "string",
        "minValue": 0.1,
        "borderWidth": 0.1,
        "urlOverridable": true,
        "currency": "string",
        "inputType": "TEXT_FIELD",
        "borderStyle": "SOLID",
        "calculated": true,
        "contentType": "DATA",
        "validation": "NONE",
        "displayLabel": "string",
        "hyperlink": {
          "linkType": "INTERNAL",
          "documentLocation": {
            "pageNumber": 0,
            "top": 0.1,
            "left": 0.1,
            "width": 0.1,
            "height": 0.1
          },
          "url": "string"
        },
        "backgroundColor": "string",
        "visible": true,
        "displayFormatType": "DEFAULT",
        "maxValue": 0.1,
        "validationErrMsg": "string",
        "displayFormat": "string",
        "visibleOptions": [
          "string"
        ],
        "readOnly": true,
        "fontName": "string",
        "conditionalAction": {
          "predicates": [
            {
              "fieldName": "string",
              "value": "string",
              "operator": "EQUALS",
              "fieldLocationIndex": 0
            }
          ],
          "anyOrAll": "ALL",
          "action": "SHOW"
        },
        "fontSize": 0.1,
        "alignment": "LEFT",
        "fontColor": "string",
        "maxLength": 0
      },
      "anchorTextInfo": {
        "anchorText": "string",
        "anchoredFormFieldLocation": {
          "offsetX": "string",
          "offsetY": "string",
          "width": "string",
          "height": "string"
        },
        "pages": [
          "string"
        ],
        "fileInfoLabel": "string"
      },
      "generatorType": "ANCHOR_TEXT",
      "linked": true
    }
  ],
  "signatureType": "ESIGN",
  "externalId": {
    "id": "string"
  },
  "message": "string",
  "deviceInfo": {
    "deviceDescription": "string",
    "applicationDescription": "string",
    "deviceTime": "2025-03-18"
  },
  "parentId": "string",
  "reminderFrequency": "DAILY_UNTIL_SIGNED",
  "redirectOptions": [
    {
      "delay": 0,
      "action": "DECLINED",
      "url": "string"
    }
  ],
  "createdDate": "2025-03-18",
  "participantSetsInfo": [
    {
      "role": "SIGNER",
      "visiblePages": [
        "string"
      ],
      "providerParticipationInfo": {
        "participationSetId": "string",
        "label": "string",
        "participationId": "string"
      },
      "electronicSealId": "string",
      "name": "string",
      "id": "string",
      "label": "string",
      "privateMessage": "string",
      "memberInfos": [
        {
          "phoneDeliveryInfo": {
            "countryIsoCode": "string",
            "phone": "string",
            "countryCode": "string"
          },
          "formDataLastAutoSavedTime": "string",
          "name": "string",
          "deliverableEmail": true,
          "id": "string",
          "isPrivate": true,
          "email": "string",
          "securityOption": {
            "password": "string",
            "authenticationMethod": "NONE",
            "notaryAuthentication": "MULTI_FACTOR_AUTHENTICATION",
            "digAuthInfo": {
              "providerId": "string",
              "providerDesc": "string",
              "providerName": "string"
            },
            "nameInfo": {
              "firstName": "string",
              "lastName": "string"
            },
            "phoneInfo": {
              "countryIsoCode": "string",
              "phone": "string",
              "countryCode": "string"
            },
            "identityCheckInfo": {
              "emailMatch": {
                "allowCustomAlternateEmail": true,
                "allowRegisteredAlternateEmail": true,
                "alternateEmails": [
                  "string"
                ],
                "requireEmailMatching": true
              },
              "nameMatch": {
                "nameMatchCriteria": "DISABLED"
              }
            }
          }
        }
      ],
      "order": 0
    }
  ],
  "hasFormFieldData": true,
  "expirationTime": "2025-03-18",
  "formFieldLayerTemplates": [
    {
      "notarize": true,
      "transientDocumentId": "string",
      "document": {
        "numPages": 0,
        "createdDate": "string",
        "name": "string",
        "id": "string",
        "label": "string",
        "mimeType": "string"
      },
      "libraryDocumentId": "string",
      "label": "string",
      "urlFileInfo": {
        "name": "string",
        "mimeType": "string",
        "url": "string"
      }
    }
  ],
  "name": "string",
  "sendType": "FILL_SIGN",
  "fileInfos": [
    {
      "notarize": true,
      "transientDocumentId": "string",
      "document": {
        "numPages": 0,
        "createdDate": "string",
        "name": "string",
        "id": "string",
        "label": "string",
        "mimeType": "string"
      },
      "libraryDocumentId": "string",
      "label": "string",
      "urlFileInfo": {
        "name": "string",
        "mimeType": "string",
        "url": "string"
      }
    }
  ],
  "createdGroupId": "string",
  "workflowId": "string",
  "status": "OUT_FOR_SIGNATURE"
}
