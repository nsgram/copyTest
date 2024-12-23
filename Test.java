import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.security.KeyStore;

public class MyHttpClientConfig {

    public RestTemplate restTemplate() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("src/main/resources/malware.qa.jks"), "malware".toCharArray());

        // Create SSL context
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, "malware".toCharArray())
                .build();

        // Create HttpClient for HttpClient5
        org.apache.hc.client5.http.impl.classic.HttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        // Create request factory with the HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Create RestTemplate
        return new RestTemplate(factory);
    }
}
