import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import org.springframework.util.ResourceUtils;

@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {
    char[] password = "password".toCharArray();

    SSLContext sslContext = SSLContextBuilder.create()
            .loadKeyMaterial(keyStore("classpath:cert.jks", password), password)
            .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

    // Create the HttpClient using the SSLContext
    CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext).build();

    // Use HttpComponentsClientHttpRequestFactory
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);

    // Return the RestTemplate with the custom request factory
    return builder.requestFactory(() -> factory).build();
}

private KeyStore keyStore(String file, char[] password) throws Exception {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    File key = ResourceUtils.getFile(file);
    try (InputStream in = new FileInputStream(key)) {
        keyStore.load(in, password);
    }
    return keyStore;
}
