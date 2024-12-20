import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ssl.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Component
public class WebClientConfig {

    @Bean
    public WebClient webClient() throws Exception {
        // Load KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = this.getClass().getResourceAsStream("/your-keystore.jks")) {
            keyStore.load(keyStoreStream, "your-keystore-password".toCharArray());
        }

        // Initialize KeyManagerFactory with KeyStore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "your-keystore-password".toCharArray());

        // Initialize TrustManagerFactory with KeyStore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Build SSL context using KeyManagerFactory and TrustManagerFactory
        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .build();

        // Configure HttpClient with SSL
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        // Build WebClient
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
