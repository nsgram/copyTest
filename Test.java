import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Base64;

public static void getProxyResponse(String fileReference, String token) {
    try {
        // Proxy setup
        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("proxy.aetna.com", 9119)))
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("N993527", "Java#Java@09".toCharArray());
                    }
                })
                .build();

        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://sit1-api.cvshealth.com/file/scan/download/v1/files/" + fileReference))
                .timeout(Duration.ofSeconds(30))
                .header("x-api-key", "T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w")
                .header("Authorization", token)
                .GET()
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Process the response
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());

        // Parse JSON response if necessary (Optional)
        if (response.statusCode() == 200) {
            parseResponseToPojo(response.body());
        } else {
            System.err.println("Error: Unable to fetch the file. Response Code: " + response.statusCode());
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
