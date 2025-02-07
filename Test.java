

	public PdfFormGenerationResponse generatePdfForm(PdfFormGenerationRequest pdfFormGenerationRequest) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Files.createDirectories(Paths.get(OUTPUT_DIR));
			String fileName = "Employer_Application_" + System.currentTimeMillis() + ".pdf";
			String filePath = OUTPUT_DIR + fileName;
			byte[] pdfBytes = Files.readAllBytes(Paths.get(TEMPLATE_PATH));
			PdfReader reader = new PdfReader(pdfBytes);
			PdfStamper stamper = new PdfStamper(reader, outputStream);
			// Fill form fields
			AcroFields fields = stamper.getAcroFields();
			fields.setField("companyName", pdfFormGenerationRequest.getCompanyName());
			fields.setField("contactName", pdfFormGenerationRequest.getContactName());
			fields.setField("phone", pdfFormGenerationRequest.getPhone());
			fields.setField("email", pdfFormGenerationRequest.getEmail());
			fields.setField("planOption", pdfFormGenerationRequest.getPlanOption());
			fields.setField("date", pdfFormGenerationRequest.getDate());
			stamper.setFormFlattening(true);

			stamper.close();
			reader.close();
			Files.write(Paths.get(filePath), outputStream.toByteArray());
			PdfFormGenerationResponse p = PdfFormGenerationResponse.builder().conversationID(filePath)
					.statusCode(String.valueOf(HttpStatus.CREATED.value())).statusDescription(HttpStatus.CREATED.name())
					.file(Base64.getEncoder().encodeToString(outputStream.toByteArray())).build();
			return p;
		} catch (IOException e) {
			throw new AsgwyGlobalException(HttpStatus.EXPECTATION_FAILED.value(), e.getLocalizedMessage());
		}
	}
	
i have used below dependancy

<dependency>
			<groupId>com.github.librepdf</groupId>
			<artifactId>openpdf</artifactId>
			<version>2.0.3</version>
		</dependency>

getting below exception

c.a.a.e.GlobalExceptionHandler - handleException : 
jakarta.servlet.ServletException: Handler dispatch failed: 
java.lang.NoClassDefFoundError: org/bouncycastle/asn1/ASN1Encodable
