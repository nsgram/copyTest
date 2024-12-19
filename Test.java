import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collections;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpResponse;

public class SecureDownloadAPI {

    public static void main(String[] args) {
        try {
            String certAlias = "rg-hcb-asgwy-qa-AVscan-privatecert";
            String passkey = "asgwydev";

            // Load the keystore
            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);

            PrivateKey privateKey = null;
            Certificate certificate = null;

            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                if (alias.equalsIgnoreCase(certAlias)) {
                    privateKey = (PrivateKey) keyStore.getKey(alias, passkey.toCharArray());
                    certificate = keyStore.getCertificate(alias);
                    break;
                }
            }

            if (privateKey == null || certificate == null) {
                throw new RuntimeException("Certificate or private key not found for alias: " + certAlias);
            }

            // Build the SSL Context
            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, passkey.toCharArray())  // Load key material
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

            // Build HTTP client
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            // Make the API call
            HttpGet request = new HttpGet("https://example.com/api/v4/avscan/download");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println("Response: " + response.getStatusLine());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error setting up secure connection: " + e.getMessage(), e);
        }
    }
}





import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class AzureCertificateLoader {

    public static void main(String[] args) {
        try {
            String certAlias = "rg-hcb-asgwy-qa-AVscan-privatecert";

            // Load the Windows-MY keystore
            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);

            // Find certificate by alias
            PrivateKey privateKey = null;
            Certificate certificate = null;

            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                if (alias.equalsIgnoreCase(certAlias)) {
                    privateKey = (PrivateKey) keyStore.getKey(alias, "asgwydev".toCharArray());
                    certificate = keyStore.getCertificate(alias);
                    break;
                }
            }

            if (privateKey == null || certificate == null) {
                throw new RuntimeException("Certificate or private key not found for alias: " + certAlias);
            }

            System.out.println("Successfully loaded certificate: " + certificate.toString());

        } catch (Exception e) {
            throw new RuntimeException("Error accessing certificate: " + e.getMessage(), e);
        }
    }
}
