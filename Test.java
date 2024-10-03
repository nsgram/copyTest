import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

@Service
public class AzureStorageService {

    @Autowired
    private BlobServiceClient blobServiceClient;

    private final String containerName = "testcontainer";

    public Mono<String> uploadFile(FilePart filePart) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = blobContainerClient.getBlobClient(filePart.filename());

        // Convert Flux<DataBuffer> to byte[]
        return filePart.content()
            .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                try {
                    Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return baos;
            })
            .flatMap(baos -> {
                byte[] bytes = baos.toByteArray();
                blobClient.upload(BinaryData.fromBytes(bytes), true);

                // Set the content type
                BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(filePart.headers().getContentType().toString());
                blobClient.setHttpHeaders(headers);

                return Mono.just(blobClient.getBlobUrl());
            })
            .onErrorResume(e -> Mono.just("Upload failed: " + e.getMessage()));
    }
}
