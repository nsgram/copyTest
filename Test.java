import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Component
public class WebClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient webClient() throws Exception {
        // Load KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = this.getClass().getResourceAsStream("/your-keystore.jks")) {
            keyStore.load(keyStoreStream, "your-keystore-password".toCharArray());
        }

        // Log certificate details
        logCertificateDetails(keyStore);

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

    private void logCertificateDetails(KeyStore keyStore) {
        try {
            for (String alias : keyStore.aliases()) {
                if (keyStore.isCertificateEntry(alias) || keyStore.isKeyEntry(alias)) {
                    Certificate certificate = keyStore.getCertificate(alias);
                    if (certificate instanceof X509Certificate) {
                        X509Certificate x509Certificate = (X509Certificate) certificate;
                        logger.info("Certificate Alias: {}", alias);
                        logger.info("Subject: {}", x509Certificate.getSubjectDN());
                        logger.info("Issuer: {}", x509Certificate.getIssuerDN());
                        logger.info("Valid From: {}", x509Certificate.getNotBefore());
                        logger.info("Valid To: {}", x509Certificate.getNotAfter());
                        logger.info("Certificate Serial Number: {}", x509Certificate.getSerialNumber());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while logging certificate details", e);
        }
    }
}
