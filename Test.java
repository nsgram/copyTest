private Mono<byte[]> downloadAVScanFile(String fileReference) {
    return avProxyWebClient.get()
        .uri(uri + fileReference)
        .header(XAPIKEY, apiKey)
        .header(AUTHORIZATION, getJwtToken(fileReference))
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error("Exception raised in downloadAVScanFile API: {}", errorBody);
                    return Mono.error(new AsgwyGlobalException("Exception in downloadAVScanFile API"));
                })
        )
        .bodyToMono(byte[].class)
        .doOnSuccess(bytes -> log.info("File downloaded successfully from AVScan API, size: {} bytes", bytes.length));
}

private Mono<FileUploadResponse> handleFileProcessing(
        String avFileRef,
        Map<String, Object> decodeJson,
        AVScanFileRequest avScanFileRequest,
        ServerWebExchange exchange) {

    return downloadAVScanFile(avFileRef).flatMap(fileBytes -> {
        String avOriginalFileName = (String) decodeJson.get(CVSAVORIGINALFILENAME);
        String avOriginalFileType = (String) decodeJson.get(CVSAVORIGINALFILETYPE);

        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);

        return Mono.fromCallable(() -> {
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
                log.info("Temporary file created at: {}", tempFile.getAbsolutePath());
            }
            return tempFile;
        }).flatMap(temp -> uploadFileOnGateway(temp, avScanFileRequest, exchange)
            .doFinally(signalType -> cleanupTempFile(tempFile)));
    });
}





-------------------


Perfect, Jagan! Below is the complete Java source file based on everything you’ve shared and the latest logic you asked for (download -> convert to Base64 string -> write to file as UTF-8).

I’ve included:
	•	The downloadAVScanFile method.
	•	The handleFileProcessing method.
	•	Helper methods: getJwtToken(), cleanupTempFile(), and a placeholder uploadFileOnGateway().

Let’s assume the class is named AVFileProcessorService.

⸻



package com.example.avscan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AVFileProcessorService {

    private final WebClient avProxyWebClient;

    @Value("${avproxy.base.url}")
    private String baseUrl;

    @Value("${avproxy.api.key}")
    private String apiKey;

    private static final String X_API_KEY = "X-API-KEY";
    private static final String AUTHORIZATION = HttpHeaders.AUTHORIZATION;

    // Main method to handle file processing
    public Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
                                                         AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {

        return downloadAVScanFile(avFileRef).flatMap(fileBytes -> {
            String avOriginalFileName = (String) decodeJson.get("CVSAVORIGINALFILENAME");
            String avOriginalFileType = (String) decodeJson.get("CVSAVORIGINALFILETYPE");

            return Mono.fromCallable(() -> {
                // Convert byte[] to Base64 string
                String base64Content = Base64.getEncoder().encodeToString(fileBytes);

                String tempDir = System.getProperty("java.io.tmpdir");
                File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);

                // Write Base64 string to file as UTF-8 (like: fs.writeFileSync(filePath, base64, 'utf8'))
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(base64Content.getBytes(StandardCharsets.UTF_8));
                }
                log.info("Base64 file written at: {}", tempFile.getAbsolutePath());

                return tempFile;
            }).flatMap(tempFile -> uploadFileOnGateway(tempFile, avScanFileRequest, exchange)
                    .doFinally(signalType -> cleanupTempFile(tempFile)));
        });
    }

    // Downloads the file as byte[]
    private Mono<byte[]> downloadAVScanFile(String fileReference) {
        return avProxyWebClient.get()
                .uri(baseUrl + fileReference)
                .header(X_API_KEY, apiKey)
                .header(AUTHORIZATION, getJwtToken(fileReference))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Exception raised in downloadAVScanFile API: {}", errorBody);
                            return Mono.error(new RuntimeException("Error in downloadAVScanFile API"));
                        }))
                .bodyToMono(byte[].class)
                .doOnSuccess(response -> log.info("File downloaded as byte[], size: {}", response.length));
    }

    // Dummy JWT token generator (you can replace this logic as needed)
    private String getJwtToken(String fileReference) {
        // TODO: Implement your token logic here
        return "Bearer dummy-jwt-token";
    }

    // Dummy upload method (replace with your real logic)
    private Mono<FileUploadResponse> uploadFileOnGateway(File file, AVScanFileRequest request, ServerWebExchange exchange) {
        // TODO: Implement your upload logic here
        log.info("Uploading file {} to Gateway", file.getName());
        return Mono.just(new FileUploadResponse("success", file.getName()));
    }

    // Clean up temporary file
    private void cleanupTempFile(File file) {
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("Temporary file deleted: {}", file.getAbsolutePath());
            } else {
                log.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
            }
        }
    }

    // Placeholder classes for your request and response (replace with real ones)
    public static class AVScanFileRequest {
        // Add fields as needed
    }

    public static class FileUploadResponse {
        private String status;
        private String fileName;

        public FileUploadResponse(String status, String fileName) {
            this.status = status;
            this.fileName = fileName;
        }

        // Getters and setters if needed
    }
}



⸻

✅ What’s included:
	•	downloadAVScanFile: Downloads file as byte[].
	•	Convert & Write Logic: Converts byte[] -> Base64 string -> writes to a file as UTF-8 text (exactly like your Node.js example with fs.writeFileSync(filePath, base64, 'utf8')).
	•	Cleanup: Deletes the temporary file after upload (good practice).
	•	Stubs:
	•	AVScanFileRequest and FileUploadResponse are placeholders. You can swap them with your real models.
	•	uploadFileOnGateway is a stub—just add your logic for uploading.

⸻

Let me know if you want to wire this into a REST controller next, or if you need unit tests!
