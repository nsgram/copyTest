import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.time.Duration;

public class AVScanService {

    private static final String BASE_URL = "https://sit1-api.cvshealth.com";
    private static final String API_KEY = "your-api-key";

    public AVScanFileResponse downloadAVScanFile(String fileReference) {
        try {
            // Configure HttpClient with optional proxy and timeout
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(30))  // Configure response timeout
                    .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                            .host("your-proxy-host")
                            .port(8080)); // Replace with proxy host/port if required
            
            // Create WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(BASE_URL)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + getJwtToken(fileReference));
            headers.add("x-api-key", API_KEY);

            // Make the API call
            return webClient.method(HttpMethod.GET)
                    .uri("/file/scan/download/v1/files/" + fileReference)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> {
                                logError(clientResponse);
                                return Mono.error(new RuntimeException("Error in AV scan download API"));
                            })
                    .bodyToMono(AVScanFileResponse.class)
                    .retry(3) // Retry up to 3 times
                    .block(Duration.ofSeconds(60)); // Block and wait for response

        } catch (Exception e) {
            // Log and throw custom exception
            Log.error("Error in AV scan download API: {}", e.getMessage(), e);
            throw new RuntimeException("Error in AV scan download API: " + e.getMessage(), e);
        }
    }

    private void logError(ClientResponse clientResponse) {
        clientResponse.bodyToMono(String.class).subscribe(body -> {
            Log.error("Error response from API: Status = {}, Body = {}", clientResponse.statusCode(), body);
        });
    }

    private String getJwtToken(String fileReference) {
        // Implement JWT token generation logic here
        return "generated-token";
    }
}
