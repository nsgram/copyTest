import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;

public class FileUploadClient {

    public static void main(String[] args) {
        try {
            // Example: Temporary file to simulate the upload
            File tempFile = new File("path/to/temp/file.pdf");

            // Initialize WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://devquote-svc.aetna.com/asgwy-api/v1/quote")
                    .build();

            // Prepare multipart form data
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("docSubcategory", "SBC");
            formData.add("quoteMissinfoDocInd", "N");
            formData.add("quoteConcessDocInd", "N");
            formData.add("uploadedUsrId", "N993527");
            formData.add("docTyp", "SG");
            formData.add("uploadedUsrNm", "FirstName LastName");
            formData.add("docCategory", "RC");
            formData.add("quoteId", "369");
            formData.add("docSize", "291");
            formData.add("docQuoteStage", "INITIAL");
            formData.add("quoteSubmitDocInd", "Y");
            formData.add("quoteConcessionId", "21");
            formData.add("file", new FileSystemResource(tempFile));

            // Build and send the POST request
            ResponseEntity<FileUploadResponse> response = webClient.post()
                    .uri("/file")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(formData)
                    .retrieve()
                    .toEntity(FileUploadResponse.class)
                    .block(); // For synchronous execution

            // Process the response
            System.out.println("File uploaded successfully:");
            System.out.println(response);

        } catch (WebClientResponseException e) {
            System.err.println("Error response from server: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DTO class for response mapping
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
