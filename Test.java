import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;

@Configuration
public class RestTemplateConfig {

    private static final String JKS_PATH = "classpath:certificate.jks"; // Path to the JKS certificate
    private static final String JKS_PASSWORD = "changeit";  // The password for the JKS

    @Bean
    public RestTemplate restTemplate() throws Exception {

        // Load the JKS file from resources
        File jksFile = new File(getClass().getClassLoader().getResource("certificate.jks").getFile());

        // Initialize the KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(jksFile.toURI().toURL().openStream(), JKS_PASSWORD.toCharArray());

        // Initialize the SSLContext using the KeyStore
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, JKS_PASSWORD.toCharArray()) // Optionally load the private key
                .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()) // Trust all certificates in the JKS
                .build();

        // Create a custom HttpClient with SSLContext
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        // Create HttpComponentsClientHttpRequestFactory with custom HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        // Return the RestTemplate
        return new RestTemplate(factory);
    }
}
