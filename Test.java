import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;

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

        // Use the reactive dataBuffer stream from the FilePart object and transfer it to the blob output stream
        return filePart.content()
                .flatMap(dataBuffer -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    dataBuffer.release();
                    byteArrayOutputStream.write(bytes, 0, bytes.length);

                    // Now upload this chunk to Azure Blob
                    return Mono.fromCallable(() -> {
                        blobClient.getBlockBlobClient().upload(BinaryData.fromBytes(byteArrayOutputStream.toByteArray()), true);
                        return true;
                    }).then();
                }).then();
    }
}
