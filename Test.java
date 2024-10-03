import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.ByteBuffer;

@Service
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureBlobService(BlobServiceClient blobServiceClient, 
                            @Value("${azure.storage.container-name}") String containerName) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
    }

    public Mono<Void> uploadFile(FilePart filePart, String fileName) {
        BlobContainerAsyncClient containerClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
        BlobAsyncClient blobClient = containerClient.getBlobAsyncClient(fileName);

        // Convert FilePart content to Flux<ByteBuffer>
        Flux<ByteBuffer> byteBufferFlux = filePart.content()
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                    dataBuffer.release();  // release the buffer after use
                    return byteBuffer;
                });

        // Upload the data asynchronously using the BlobAsyncClient
        return blobClient.upload(byteBufferFlux, true).then();
    }
}
