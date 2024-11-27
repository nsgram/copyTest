import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class FileUploadClient {

    public static void main(String[] args) {
        try {
            // Example: Temporary file to simulate the upload
            File tempFile = new File("path/to/temp/file.pdf");

            // Initialize WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://devquote-svc.aetna.com/asgwy-api/v1/quote")
                    .build();

            // Build and send the POST request
            Mono<FileUploadResponse> response = webClient.post()
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
                    .bodyToMono(FileUploadResponse.class);

            // Process the response
            response.subscribe(resp -> {
                System.out.println("File uploaded successfully:");
                System.out.println(resp);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FileUploadResponse is the DTO class to map the server response.
    public static class FileUploadResponse {
        private String statusCode;
        private String statusDescription;
        private String conversationID;

        // Getters and Setters
        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusDescription() {
            return statusDescription;
        }

        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }

        public String getConversationID() {
            return conversationID;
        }

        public void setConversationID(String conversationID) {
            this.conversationID = conversationID;
        }

        @Override
        public String toString() {
            return "FileUploadResponse{" +
                    "statusCode='" + statusCode + '\'' +
                    ", statusDescription='" + statusDescription + '\'' +
                    ", conversationID='" + conversationID + '\'' +
                    '}';
        }
    }
}
