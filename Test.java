public String uploadFile(MultipartFile file, String quoteId, String docTyp, String docCategory, String docTitle)
        throws IOException {
    // Ensure that the Azure Blob service client and container client are initialized
    BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

    // Define the "path" of the file inside the blob container, using docTitle as the file name
    String blobFilePath = quoteId + "/" + docTyp + "/" + docCategory + "/" + docTitle;

    // Get the BlobClient for this file (using docTitle as the name of the file in this path)
    BlobClient blobClient = blobContainerClient.getBlobClient(blobFilePath);

    try (InputStream inputStream = file.getInputStream()) {
        // Upload the file input stream to Azure Blob Storage
        blobClient.upload(inputStream, file.getSize(), true);

        // Set the content type of the blob (file)
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
        blobClient.setHttpHeaders(headers);
    } catch (IOException | BlobStorageException e) {
        // If any error occurs during upload, throw the exception to be handled by the calling method
        throw new IOException("Error uploading file to Azure Blob Storage", e);
    }

    // Return the Blob URL after successful upload
    return blobClient.getBlobUrl();
}


-------
public String uploadFile(MultipartFile file, String quoteId, String docTyp, String docCategory, String docTitle)
        throws IOException {
    // Input validation to avoid any directory traversal or unsafe paths
    validateInput(quoteId, docTyp, docCategory, docTitle);

    // Ensure that the Azure Blob service client and container client are initialized
    BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

    // Define the "path" of the file inside the blob container, using docTitle as the file name
    String blobFilePath = quoteId + "/" + docTyp + "/" + docCategory + "/" + docTitle;

    // Get the BlobClient for this file (using docTitle as the name of the file in this path)
    BlobClient blobClient = blobContainerClient.getBlobClient(blobFilePath);

    if (blobClient.exists()) {
        throw new FileAlreadyExistsException("File with the name " + docTitle + " already exists in the specified path.");
    }

    try (InputStream inputStream = file.getInputStream()) {
        // Upload the file input stream to Azure Blob Storage
        blobClient.upload(inputStream, file.getSize(), true);

        // Set the content type of the blob (file)
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
        blobClient.setHttpHeaders(headers);
    } catch (BlobStorageException e) {
        // Re-throw specific Azure Blob exceptions or handle them appropriately
        throw new IOException("Error uploading file to Azure Blob Storage: " + e.getMessage(), e);
    }

    // Return the Blob URL after successful upload
    return blobClient.getBlobUrl();
}

// Input validation method to sanitize inputs
private void validateInput(String quoteId, String docTyp, String docCategory, String docTitle) throws IllegalArgumentException {
    if (quoteId == null || quoteId.isEmpty() || docTyp == null || docTyp.isEmpty() ||
        docCategory == null || docCategory.isEmpty() || docTitle == null || docTitle.isEmpty()) {
        throw new IllegalArgumentException("Invalid input: One or more required parameters are missing or empty.");
    }
    // Add further validation as needed (e.g., only allow alphanumeric values to avoid security risks)
    if (!quoteId.matches("[a-zA-Z0-9_-]+") || !docTyp.matches("[a-zA-Z0-9_-]+") ||
        !docCategory.matches("[a-zA-Z0-9_-]+") || !docTitle.matches("[a-zA-Z0-9_.-]+")) {
        throw new IllegalArgumentException("Invalid input: Inputs contain unsafe characters.");
    }
}
