<dependency>
    <groupId>io.projectreactor.netty</groupId>
    <artifactId>reactor-netty-http</artifactId>
    <version>1.0.34</version>
</dependency>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative-boringssl-static</artifactId>
    <version>2.0.59.Final</version>
</dependency>


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ssl.SslProvider;
import reactor.netty.transport.ssl.SslContextSpec;

import java.io.InputStream;
import java.security.KeyStore;

@Component
public class WebClientConfig {

    @Bean
    public WebClient webClient() throws Exception {
        // Load your keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = this.getClass().getResourceAsStream("/your-keystore.jks")) {
            keyStore.load(keyStoreStream, "your-keystore-password".toCharArray());
        }

        // Build SSL context
        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(keyStore, "your-keystore-password".toCharArray())
                .trustManager(keyStore)
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
