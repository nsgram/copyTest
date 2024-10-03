import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AzureStorageService {

    @Autowired
    private BlobServiceClient blobServiceClient;

    private final String containerName = "testcontainer";

    public Mono<String> uploadFile(FilePart filePart) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(filePart.filename());

        // Upload the file using Azure's reactive upload method
        return blobClient.upload(filePart.content(), true)
            .then(Mono.defer(() -> {
                BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(filePart.headers().getContentType().toString());
                blobClient.setHttpHeaders(headers);
                return Mono.just(blobClient.getBlobUrl());
            }))
            .onErrorResume(e -> {
                return Mono.just("Upload failed: " + e.getMessage());
            });
    }
}
