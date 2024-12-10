private Mono<Void> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson, AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
    return Mono.using(
        // Resource Supplier
        () -> {
            String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
            String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
            File tempFile = File.createTempFile(avOriginalFileName, "." + avOriginalFileType);

            AVScanFileResponse avDownloadResponse = uploadAVScanFile(avFileRef);
            String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
                byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
                fos.write(decodedBytes);
            }
            return tempFile;
        },
        // Mono using Resource
        tempFile -> {
            log.info("Uploading file to gateway...");
            uploadFileOnGateway(tempFile, avScanFileRequest.getUploadData(), exchange);
            return Mono.empty();
        },
        // Cleanup Logic
        tempFile -> {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                log.info("Temporary file cleanup status: {}", deleted ? "SUCCESS" : "FAILED");
            }
        }
    );
}
