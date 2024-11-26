import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class FileUploadClient {

    public static void main(String[] args) {
        try {
            // Encrypted file string from the response
            String encryptedFile = "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc";

            // Your RSA private key in Base64-encoded format
            String privateKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcw..."; // Replace with your actual private key
            PrivateKey privateKey = loadPrivateKey(privateKeyString);

            // Decrypt the file content
            byte[] fileBytes = decryptFileWithPrivateKey(encryptedFile, privateKey);

            // Create a temporary file from the decrypted content
            File tempFile = Files.createTempFile("upload_", ".pdf").toFile();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            // Initialize WebClient for the file upload
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://devquote-svc.aetna.com/asgwy-api/v1/quote")
                    .build();

            // Build and send the POST request
            FileUploadDto response = webClient.post()
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
                    .bodyToMono(FileUploadDto.class)
                    .block(); // Convert Mono to FileUploadDto synchronously

            // Print the response
            System.out.println("File uploaded successfully:");
            System.out.println(response);

            // Cleanup temporary file
            tempFile.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decrypts the file using the provided private key.
     */
    private static byte[] decryptFileWithPrivateKey(String encryptedData, PrivateKey privateKey) throws Exception {
        // Decode Base64-encoded encrypted data
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // Initialize RSA cipher for decryption
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Decrypt the data
        return cipher.doFinal(encryptedBytes);
    }

    /**
     * Loads a PrivateKey object from a Base64-encoded string.
     */
    private static PrivateKey loadPrivateKey(String privateKeyString) throws Exception {
        // Decode Base64 private key
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);

        // Generate PrivateKey object
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * DTO class to map the response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileUploadDto {
        private long quoteId;
        private String docTyp;
        private String docCategory;
        private String docSubcategory;
        private Long docSize;
        private String docQuoteStage;
        private Character quoteSubmitDocInd;
        private Character quoteConcessDocInd;
        private Long quoteConcessionId;
        private Character quoteMissinfoDocInd;
        private String uploadedUsrId;
        private String uploadedUsrNm;

        // Getters and Setters
        public long getQuoteId() {
            return quoteId;
        }

        public void setQuoteId(long quoteId) {
            this.quoteId = quoteId;
        }

        public String getDocTyp() {
            return docTyp;
        }

        public void setDocTyp(String docTyp) {
            this.docTyp = docTyp;
        }

        public String getDocCategory() {
            return docCategory;
        }

        public void setDocCategory(String docCategory) {
            this.docCategory = docCategory;
        }

        public String getDocSubcategory() {
            return docSubcategory;
        }

        public void setDocSubcategory(String docSubcategory) {
            this.docSubcategory = docSubcategory;
        }

        public Long getDocSize() {
            return docSize;
        }

        public void setDocSize(Long docSize) {
            this.docSize = docSize;
        }

        public String getDocQuoteStage() {
            return docQuoteStage;
        }

        public void setDocQuoteStage(String docQuoteStage) {
            this.docQuoteStage = docQuoteStage;
        }

        public Character getQuoteSubmitDocInd() {
            return quoteSubmitDocInd;
        }

        public void setQuoteSubmitDocInd(Character quoteSubmitDocInd) {
            this.quoteSubmitDocInd = quoteSubmitDocInd;
        }

        public Character getQuoteConcessDocInd() {
            return quoteConcessDocInd;
        }

        public void setQuoteConcessDocInd(Character quoteConcessDocInd) {
            this.quoteConcessDocInd = quoteConcessDocInd;
        }

        public Long getQuoteConcessionId() {
            return quoteConcessionId;
        }

        public void setQuoteConcessionId(Long quoteConcessionId) {
            this.quoteConcessionId = quoteConcessionId;
        }

        public Character getQuoteMissinfoDocInd() {
            return quoteMissinfoDocInd;
        }

        public void setQuoteMissinfoDocInd(Character quoteMissinfoDocInd) {
            this.quoteMissinfoDocInd = quoteMissinfoDocInd;
        }

        public String getUploadedUsrId() {
            return uploadedUsrId;
        }

        public void setUploadedUsrId(String uploadedUsrId) {
            this.uploadedUsrId = uploadedUsrId;
        }

        public String getUploadedUsrNm() {
            return uploadedUsrNm;
        }

        public void setUploadedUsrNm(String uploadedUsrNm) {
            this.uploadedUsrNm = uploadedUsrNm;
        }

        @Override
        public String toString() {
            return "FileUploadDto{" +
                    "quoteId=" + quoteId +
                    ", docTyp='" + docTyp + '\'' +
                    ", docCategory='" + docCategory + '\'' +
                    ", docSubcategory='" + docSubcategory + '\'' +
                    ", docSize=" + docSize +
                    ", docQuoteStage='" + docQuoteStage + '\'' +
                    ", quoteSubmitDocInd=" + quoteSubmitDocInd +
                    ", quoteConcessDocInd=" + quoteConcessDocInd +
                    ", quoteConcessionId=" + quoteConcessionId +
                    ", quoteMissinfoDocInd=" + quoteMissinfoDocInd +
                    ", uploadedUsrId='" + uploadedUsrId + '\'' +
                    ", uploadedUsrNm='" + uploadedUsrNm + '\'' +
                    '}';
        }
    }
}
