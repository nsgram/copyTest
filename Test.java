import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

public class RestTemplateConfig {

    public RestTemplate restTemplateWithJks() throws Exception {
        // Load the JKS file as KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream("src/main/resources/your-keystore.jks"); // Replace with your actual JKS file path
        keyStore.load(fis, "your-keystore-password".toCharArray()); // Replace with your keystore password

        // Create an SSL context and load the KeyStore
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, "your-key-password".toCharArray()) // Password for your private key in JKS
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()) // Allows trusting self-signed certificates
                .build();

        // Configure HttpClient with the custom SSL context
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Disable hostname verification
                .build();

        // Set the HttpClient in RestTemplate
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
