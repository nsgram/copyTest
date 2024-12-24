import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class RestTemplateJksExample {

    public static void main(String[] args) throws Exception {
        // Path to your JKS file
        String jksFilePath = "src/main/resources/keystore.jks";
        String jksPassword = "password"; // Keystore password

        // Load the JKS file
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(new File(jksFilePath)), jksPassword.toCharArray());

        // Create the SSLContext
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, jksPassword.toCharArray()) // Load client certificates
                .loadTrustMaterial(keyStore, (TrustStrategy) (chain, authType) -> true) // Trust all for testing
                .build();

        // Create SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        // Configure the connection manager with the SSL factory
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(sslSocketFactory.getSocketConfig());
        connectionManager.setDefaultSocketConfig(sslSocketFactory.getSocketConfig());

        // Build the CloseableHttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // Configure RestTemplate with the HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // Test the configuration
        String url = "https://your-secure-url.com/api/test"; // Replace with your secure URL
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Print the response
        System.out.println("Response: " + response.getBody());
    }
}
