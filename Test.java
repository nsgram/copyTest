import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyStore;

public class RestTemplateJksExample {

    public static void main(String[] args) throws Exception {
        // Path to your JKS file
        String jksFilePath = "src/main/resources/keystore.jks";
        String jksPassword = "password"; // Keystore password
        
        // Load the JKS file
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new java.io.FileInputStream(new File(jksFilePath)), jksPassword.toCharArray());

        // Create the SSLContext
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, jksPassword.toCharArray())
                .loadTrustMaterial(keyStore, (TrustStrategy) (chain, authType) -> true) // Trust all certificates for demo purposes
                .build();

        // Create HttpClient with the SSLContext
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        // Configure RestTemplate with the HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // Test the configuration with a request
        String url = "https://your-secure-url.com/api/test"; // Replace with your HTTPS URL
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Print the response
        System.out.println("Response: " + response.getBody());
    }
}


<dependencies>
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>6.1.12</version>
    </dependency>

    <!-- Apache HttpClient 5 -->
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <version>5.4</version>
    </dependency>
</dependencies>
