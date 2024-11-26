import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;

public class FileUploadClient {

    public static void main(String[] args) {
        try {
            // Encrypted file string
            String encryptedFile = "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc";

            // Decryption key (use your actual key)
            String decryptionKey = "1234567890123456"; // Must be 16 characters for AES-128
            byte[] fileBytes = decryptFile(encryptedFile, decryptionKey);

            // Create a temporary file
            File tempFile = Files.createTempFile("upload_", ".pdf").toFile();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            // Initialize WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://devquote-svc.aetna.com/asgwy-api/v1/quote")
                    .build();

            // Build and send the POST request
            Mono<String> response = webClient.post()
                    .uri("/file")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder -> {
                        builder.field("docSubcategory", "SBC");
                        builder.field("quoteMissinfoDocInd", "N");
                        builder.field("quoteConcessDocInd", "N");
                        builder.field("uploadedUsrId", "N993527");
                        builder.field("docTyp", "SG");
                        builder.field("uploadedUsrNm", "FirstName LastName");
                        builder.field("docCategory", "RC");
                        builder.field("quoteId", "369");
                        builder.field("docSize", "291");
                        builder.field("docQuoteStage", "INITIAL");
                        builder.field("quoteSubmitDocInd", "Y");
                        builder.field("quoteConcessionId", "21");
                        builder.part("file", new FileSystemResource(tempFile))
                                .header("Content-Type", "application/pdf");
                    })
                    .retrieve()
                    .bodyToMono(String.class);

            // Print the response
            response.subscribe(System.out::println);

            // Cleanup temporary file
            tempFile.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] decryptFile(String encryptedData, String decryptionKey) throws Exception {
        // Decode Base64 string
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // Initialize cipher for AES decryption
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(decryptionKey.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return cipher.doFinal(encryptedBytes);
    }
}
