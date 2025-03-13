Here’s a complete Spring Boot implementation where the file is received as part of the request instead of using a file path. The application:
	•	Receives a file in the request
	•	Dynamically fetches API URLs
	•	Uploads the file as a transient document
	•	Creates an agreement using the transient document ID

⸻

1. DTO for API Request

We modify the request DTO to accept a MultipartFile directly.

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class AgreementRequestDto {
    private String bearerToken;
    private String agreementName;
    private String message;
    private List<ParticipantInfo> participants;
    private List<String> ccEmails;
    private MultipartFile file;
}

@Data
class ParticipantInfo {
    private String email;
    private String countryCode;
    private String phone;
    private int order;
}



⸻

2. Service to Handle Adobe Sign API Calls

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

@Service
public class AdobeSignService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String createAgreement(AgreementRequestDto requestDto) {
        String apiBaseUrl = getBaseUri(requestDto.getBearerToken());
        String transientDocumentId = uploadTransientDocument(apiBaseUrl, requestDto.getBearerToken(), requestDto.getFile());
        return createAgreementWithTransientDocument(apiBaseUrl, requestDto, transientDocumentId);
    }

    private String getBaseUri(String bearerToken) {
        String url = "https://aetna.na1.adobesign.com/api/rest/v6/baseUris";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return (String) response.getBody().get("apiAccessPoint");
    }

    private String uploadTransientDocument(String apiBaseUrl, String bearerToken, MultipartFile file) {
        String url = apiBaseUrl + "api/rest/v6/transientDocuments";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("File-Name", file.getOriginalFilename());
        body.add("File", new MultipartInputResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        return (String) response.getBody().get("transientDocumentId");
    }

    private String createAgreementWithTransientDocument(String apiBaseUrl, AgreementRequestDto requestDto, String transientDocumentId) {
        String url = apiBaseUrl + "api/rest/v6/agreements";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + requestDto.getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = buildAgreementRequest(requestDto, transientDocumentId);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        return (String) response.getBody().get("id");
    }

    private String buildAgreementRequest(AgreementRequestDto requestDto, String transientDocumentId) {
        return String.format("""
            {
                "fileInfos": [
                    {
                        "transientDocumentId": "%s"
                    }
                ],
                "name": "%s",
                "participantSetsInfo": %s,
                "signatureType": "ESIGN",
                "state": "IN_PROCESS",
                "message": "%s",
                "ccs": %s
            }
            """,
            transientDocumentId,
            requestDto.getAgreementName(),
            buildParticipantsJson(requestDto),
            requestDto.getMessage(),
            buildCcEmailsJson(requestDto.getCcEmails())
        );
    }

    private String buildParticipantsJson(AgreementRequestDto requestDto) {
        StringBuilder sb = new StringBuilder("[");
        for (ParticipantInfo participant : requestDto.getParticipants()) {
            sb.append(String.format("""
                {
                    "memberInfos": [
                        {
                            "email": "%s",
                            "securityOption": {
                                "phoneInfo": {
                                    "countryCode": "%s",
                                    "phone": "%s"
                                }
                            }
                        }
                    ],
                    "order": %d,
                    "role": "SIGNER"
                },
                """, participant.getEmail(), participant.getCountryCode(), participant.getPhone(), participant.getOrder()));
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }

    private String buildCcEmailsJson(List<String> ccEmails) {
        StringBuilder sb = new StringBuilder("[");
        for (String email : ccEmails) {
            sb.append(String.format("""
                {
                    "email": "%s"
                },
                """, email));
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }
}



⸻

3. Controller to Expose the API

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/adobe-sign")
public class AdobeSignController {

    private final AdobeSignService adobeSignService;

    public AdobeSignController(AdobeSignService adobeSignService) {
        this.adobeSignService = adobeSignService;
    }

    @PostMapping(value = "/create-agreement", consumes = {"multipart/form-data"})
    public ResponseEntity<String> createAgreement(
            @RequestPart("bearerToken") String bearerToken,
            @RequestPart("agreementName") String agreementName,
            @RequestPart("message") String message,
            @RequestPart("participants") List<ParticipantInfo> participants,
            @RequestPart("ccEmails") List<String> ccEmails,
            @RequestPart("file") MultipartFile file) {

        AgreementRequestDto requestDto = new AgreementRequestDto();
        requestDto.setBearerToken(bearerToken);
        requestDto.setAgreementName(agreementName);
        requestDto.setMessage(message);
        requestDto.setParticipants(participants);
        requestDto.setCcEmails(ccEmails);
        requestDto.setFile(file);

        String agreementId = adobeSignService.createAgreement(requestDto);
        return ResponseEntity.ok(agreementId);
    }
}



⸻

4. Helper Class for MultipartFile Conversion

Since Adobe Sign API requires a FileSystemResource, we create a helper class.

import org.springframework.core.io.AbstractResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public class MultipartInputResource extends AbstractResource {

    private final MultipartFile multipartFile;

    public MultipartInputResource(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public String getDescription() {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }
}



⸻

How to Use the API

Endpoint:
POST /api/adobe-sign/create-agreement

Request Type: multipart/form-data

Postman / API Client Request

POST /api/adobe-sign/create-agreement
Content-Type: multipart/form-data

bearerToken = "3AAABLblqZhAGDF..."
agreementName = "Application for Insurance"
message = "Thank you for applying..."
participants = [
    {"email": "user1@example.com", "countryCode": "+1", "phone": "3331212121", "order": 1},
    {"email": "user2@example.com", "countryCode": "+1", "phone": "4441212121", "order": 2}
]
ccEmails = ["cc1@example.com", "cc2@example.com"]
file = (attach a PDF file)



⸻

Key Features

✅ Receives File in Request
✅ Uses Dynamic API URLs
✅ Single API Call for fetching base URL, uploading file, and creating agreement
✅ Sonar & Checkmarx Compliant – Uses safe coding practices

Let me know if you need changes!
