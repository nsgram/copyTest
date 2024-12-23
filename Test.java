import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContexts;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.function.Consumer;

public class WebClientConfig {

    public WebClient webClientWithJks() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(new File("src/main/resources/your-keystore.jks"))) {
            keyStore.load(fis, "your-keystore-password".toCharArray());
        }

        // Set up the SSLContext
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, "your-key-password".toCharArray()) // Password for private key
                .build();

        // Set up the HTTP client with SSL support and default SSL hostname verifier (i.e., skip hostname verification)
        HttpClient httpClient = HttpClient.create()
                .secure(ssl -> ssl.sslContext(sslContext).hostnameVerifier(NoopHostnameVerifier.INSTANCE));

        // Create WebClient with reactive HTTP connector
        WebClient.Builder webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        return webClientBuilder.build();
    }
}
