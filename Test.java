public boolean deleteFileByBlobName(String blobName) throws IOException {
    try {
        // Ensure that the Azure Blob service client and container client are initialized
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Get the BlobClient for the file using the blob name (path inside the container)
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        // Check if the file exists, then delete it
        if (blobClient.exists()) {
            blobClient.delete();
            System.out.println("File deleted successfully.");
            return true;  // Return true if the file was successfully deleted
        } else {
            System.out.println("File not found.");
            return false; // Return false if the file does not exist
        }
    } catch (BlobStorageException e) {
        // Handle specific Azure Blob exceptions or re-throw
        throw new IOException("Error deleting file from Azure Blob Storage: " + e.getMessage(), e);
    }
}
