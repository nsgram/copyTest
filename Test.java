curl -X 'POST' \
  'https://devquote-svc.aetna.com/asgwy-api/v1/quote/file' \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F 'docSubcategory=SBC' \
  -F 'quoteMissinfoDocInd=N' \
  -F 'quoteConcessDocInd=N' \
  -F 'uploadedUsrId=N993527' \
  -F 'docTyp=SG' \
  -F 'uploadedUsrNm=FirstName LastName' \
  -F 'docCategory=RC' \
  -F 'quoteId=369' \
  -F 'docSize=291' \
  -F 'docQuoteStage=INITIAL' \
  -F 'quoteSubmitDocInd=Y' \
  -F 'quoteConcessionId=21' \
  -F 'file=@Steps to install CheckMarx Plugin.pdf;type=application/pdf'
 
Write call for above using spring boot3 web client in request use below file reference 
Convert below file string into  -F 'file write complete java code remove sonar and security issue

{
  "statusCode": "0000",
  "statusDescription": "Success",
  "conversationID": "334020c974274ea384921d7f37767b20",
  "file": "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc"
}


