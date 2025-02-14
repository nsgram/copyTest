
	private String uploadOnAFSFile(InputStream contentInputStream, String docTitle) throws IOException {
		//log.info("AzureStorageService.uploadFile :: File upload started on AFS " + file.getOriginalFilename());
		BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
		BlobClient blobClient = blobContainerClient.getBlobClient(docTitle);
		try {
			blobClient.upload(contentInputStream, contentInputStream.available(), true);
			BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("application/octet-stream");
			blobClient.setHttpHeaders(headers);
			log.info("CSV getBlobUrl:{}",blobClient.getBlobUrl());
		} catch (BlobStorageException e) {
			log.error("Error uploading file e :: {}", e.getMessage());
		}
		log.info("AzureStorageService.uploadFile ::File uploaded AFS:: " + docTitle);
		return blobClient.getBlobName();

	}


Getting below exception in above method

[2025-02-14 10:24:53.632] asgwy-webmw-oci-dev [WARN ] parallel-1 [N892902]-[]-[1e8391d1]  reactor.core.Exceptions - throwIfFatal detected a jvm fatal exception, which is thrown and logged below:
java.lang.NoSuchMethodError: 'reactor.core.publisher.Mono reactor.core.publisher.Mono.subscriberContext(reactor.util.context.Context)'
	at com.azure.storage.blob.BlobClient.uploadWithResponse(BlobClient.java:303)
