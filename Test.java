import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public String uploadToAdobe(MultipartFile file, String authorizationKey) throws Exception {
    String url = "https://api.na2.documents.adobe.com/api/rest/v6/transientDocuments";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + authorizationKey);
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Convert file to ByteArrayResource
    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
        @Override
        public String getFilename() {
            return file.getOriginalFilename(); // Important: Adobe API expects a proper filename
        }
    };

    // Create multipart body
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("File-Name", file.getOriginalFilename());
    body.add("File", fileResource);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return (String) response.getBody().get("transientDocumentId");
    } else {
        throw new RuntimeException("Failed to upload file: " + response.getBody());
    }
}
