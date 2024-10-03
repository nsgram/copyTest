import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
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
        BlobContainerAsyncClient blobContainerAsyncClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(filePart.filename()).getBlockBlobAsyncClient();

        // Upload the file using Azure's reactive method
        return blockBlobAsyncClient.upload(filePart.content(), filePart.headers().getContentLength(), true)
            .then(Mono.defer(() -> {
                BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(filePart.headers().getContentType().toString());
                return blockBlobAsyncClient.setHttpHeaders(headers)
                    .then(Mono.just(blockBlobAsyncClient.getBlobUrl()));
            }))
            .onErrorResume(e -> Mono.just("Upload failed: " + e.getMessage()));
    }
}
