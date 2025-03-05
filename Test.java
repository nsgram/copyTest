@PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload New files", description = "Save upload file Azure File Storage and insert metadata into DB")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<FileOperationResponse> uploadFile(@RequestPart("file") MultipartFile file,
			@RequestPart("quoteId") String quoteId, @Schema(example = "SG") @RequestPart("docTyp") String docTyp,
			@Schema(example = "9") @RequestPart("groupId") String groupId,
			@Schema(example = "RC") @RequestPart("docCategory") String docCategory,
			@Schema(example = "SBC") @RequestPart("docSubcategory") String docSubcategory,
			@Schema(example = "1024") @RequestPart("docSize") String docSize,
			@Schema(example = "INITIAL") @RequestPart("docQuoteStage") String docQuoteStage,
			@Schema(example = "Y") @RequestPart(value = "quoteSubmitDocInd", required = false) String quoteSubmitDocInd,
			@Schema(example = "N") @RequestPart(value = "quoteConcessDocInd", required = false) String quoteConcessDocInd,
			@Valid @Schema(example = "21") @RequestPart(value = "quoteConcessionId", required = false) String quoteConcessionId,
			@Schema(example = "N") @RequestPart(value = "quoteMissinfoDocInd", required = false) String quoteMissinfoDocInd,
			@Schema(example = "FirstName LastName") @RequestPart("uploadedUsrNm") String uploadedUsrNm,
			@Schema(example = "Document Key") @RequestPart(value = "docKey", required = false) String docKey,
			@Schema(example = "Document Comment Txt") @RequestPart(value = "commentTxt", required = false) String commentTxt,
			@Schema(example = "Document commentCategory") @RequestPart(value = "commentCategory", required = false) String commentCategory,
			@Schema(example = "Document quoteSubmitCommentInd") @RequestPart(value = "quoteSubmitCommentInd", required = false) String quoteSubmitCommentInd,
			@Schema(example = "Document quoteConcessCommentInd") @RequestPart(value = "quoteConcessCommentInd", required = false) String quoteConcessCommentInd,
			@Schema(example = "Document quoteMissinfoCommentInd") @RequestPart(value = "quoteMissinfoCommentInd", required = false) String quoteMissinfoCommentInd,
			@Schema(example = "Document sentToFilenetInd") @RequestPart(value = "sentToFilenetInd", required = false) String sentToFilenetInd,
			@Schema(example = "N") @RequestPart(value = "docImqIndicator", required = false) String docImqIndicator,
			@Schema(example = "N") @RequestPart(value = "docRcIndicator", required = false) String docRcIndicator,
			@Schema(example = "N") @RequestPart(value = "docclaimsexpIndicator", required = false) String docclaimsexpIndicator,HttpServletRequest httpServletRequest) {
		log.info("post call uploadFile :: {}", quoteId);
		log.info("Group ID:"+ groupId);
		KeyIdentifiers keyIdentifiers = (KeyIdentifiers)httpServletRequest.getAttribute(AsgwyConstants.KEY_IDENTIFIERS);
		if(!StringUtils.isEmpty(docKey)){
			docKey = docKeyEncrptUtil.encrypt(docKey);
		}
		isValidChar(quoteSubmitDocInd, "QuoteSubmitDocInd");
		isValidChar(quoteConcessDocInd, "quoteConcessDocInd");
		isValidChar(quoteMissinfoDocInd, "QuoteMissinfoDocInd");
		
		isValidChar(docImqIndicator, "DocImqIndicator");
		isValidChar(docRcIndicator, "DocRcIndicator");
		isValidChar(docclaimsexpIndicator, "DocclaimsexpIndicator");
		
		FileUploadDto fileUploadDto = new FileUploadDto();
		fileUploadDto.setQuoteId(Long.parseLong(quoteId));
		fileUploadDto.setGroupId(Long.parseLong(groupId));
		fileUploadDto.setDocTyp(docTyp);
		fileUploadDto.setDocCategory(docCategory);
		fileUploadDto.setDocSubcategory(docSubcategory);
		fileUploadDto.setDocSize(Long.parseLong(docSize));
		fileUploadDto.setDocQuoteStage(docQuoteStage);
		fileUploadDto.setQuoteSubmitDocInd(quoteSubmitDocInd != null ? quoteSubmitDocInd.charAt(0) : null);
		fileUploadDto.setQuoteConcessDocInd(quoteConcessDocInd != null ? quoteConcessDocInd.charAt(0) : null);
		fileUploadDto.setQuoteConcessionId(quoteConcessionId != null ? Long.parseLong(quoteConcessionId) : null);
		fileUploadDto.setQuoteMissinfoDocInd(quoteMissinfoDocInd != null ? quoteMissinfoDocInd.charAt(0) : null);
		
		fileUploadDto.setDocImqIndicator(docImqIndicator != null ? docImqIndicator.charAt(0) : null);
		fileUploadDto.setDocRcIndicator(docRcIndicator != null ? docRcIndicator.charAt(0) : null);
		fileUploadDto.setDocclaimsexpIndicator(docclaimsexpIndicator != null ? docclaimsexpIndicator.charAt(0) : null);
		
		if(keyIdentifiers.getUsertype().equalsIgnoreCase(AsgwyConstants.USER_TYPE_EXTERNAL)) {
			fileUploadDto.setUploadedUsrId(keyIdentifiers.getProxyid());
		}else if(keyIdentifiers.getUsertype().equalsIgnoreCase(AsgwyConstants.USER_TYPE_INTERNAL)){
			fileUploadDto.setUploadedUsrId(keyIdentifiers.getAetnaid());
		}
		fileUploadDto.setUploadedUsrNm(uploadedUsrNm);
		fileUploadDto.setDocKey(docKey);
		
		fileUploadDto.setCommentTxt(commentTxt);
		fileUploadDto.setCommentCategory(commentCategory);
		fileUploadDto.setQuoteConcessCommentInd(quoteConcessCommentInd);
		fileUploadDto.setQuoteMissinfoCommentInd(quoteMissinfoCommentInd);
		fileUploadDto.setQuoteSubmitCommentInd(quoteSubmitCommentInd);
		fileUploadDto.setSentToFilenetInd(sentToFilenetInd);
		
		return new ResponseEntity<>(fileService.uploadFile(file, fileUploadDto), HttpStatus.CREATED);
	}

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
server.tomcat.max-swallow-size=100MB

its not allowing more than 1mb file
