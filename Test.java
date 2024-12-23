import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class RestTemplateConfig {

    public RestTemplate restTemplateWithJks() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(new File("src/main/resources/your-keystore.jks")); // Path to the JKS file
        keyStore.load(fis, "your-keystore-password".toCharArray());

        // Create SSLContext with the keystore
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, "your-key-password".toCharArray()) // Password for the private key
                .build();

        // Create HttpClient with the custom SSLContext
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
                .build();

        // Use HttpClient with RestTemplate
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
