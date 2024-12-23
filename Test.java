import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;

public class MyHttpClientConfig {

    public RestTemplate restTemplate() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("src/main/resources/malware.qa.jks"), "malware".toCharArray());

        // Create SSL context
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, "malware".toCharArray()) // Load the private key material (optional)
                .build();

        // Create HttpClient for HttpClient4
        HttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext) // Set the SSL context
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Optional: to ignore hostname verification (use if needed)
                .build(); // Build the HttpClient

        // Create request factory with the HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Create RestTemplate
        return new RestTemplate(factory);
    }
}
