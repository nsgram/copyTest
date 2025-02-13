	private Mono<FileUploadResponse> handleFileProcessing2(String avFileRef, Map<String, Object> decodeJson,
			AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
		return Mono.using(
				// Resource Supplier
				() -> {
					String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
					String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
					String tempDir = System.getProperty("java.io.tmpdir");
					// Write to a temporary file
					File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);
					AVScanFileResponse avDownloadResponse = downloadAVScanFile(avFileRef);

					log.info("File Downloaded from AV malware statusDescription::{} ",
							avDownloadResponse.getStatusDescription());
					String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

					String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(),
							avFileDownloadKey);
					byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
					String docTitle = docTitle(avScanFileRequest.getUploadData().getQuoteId(),
							avScanFileRequest.getUploadData().getDocTyp(),
							avScanFileRequest.getUploadData().getDocSubcategory(), avOriginalFileType);
					docTitle = uploadFile(decodedBytes, docTitle);

					return docTitle;
				},
				// Mono using Resource
				docTitle -> {
					log.info("Uploading file to gateway...");
					FileUploadResponse response = uploadFileOnGateway(docTitle, avScanFileRequest, exchange);
					log.info("Uploading file to gateway document id..." + response.getQuoteDocumentId());
					return Mono.just(response);
				});
	}
