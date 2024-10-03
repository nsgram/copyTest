import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.core.util.FluxUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        return blobClient.getBlockBlobAsyncClient()
            .upload(FluxUtil.toFluxByteBuffer(filePart.content()), null, true)
            .then();
    }
}


BlobHttpHeaders headers = new BlobHttpHeaders()
    .setContentType(filePart.headers().getContentType().toString());
blobClient.setHttpHeaders(headers);
