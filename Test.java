import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureKeyRetriever {

    /**
     * Reads the private key from the Azure App Service Key Store (Windows-MY) using the certificate thumbprint.
     *
     * @param thumbprint Certificate thumbprint (case-insensitive).
     * @return The corresponding private key.
     * @throws RuntimeException If the key retrieval fails.
     */
    public static PrivateKey readPrivateKey(String thumbprint) {
        try {
            // Load the Windows-MY keystore
            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);

            // Normalize thumbprint
            String normalizedThumbprint = thumbprint.replaceAll("\\s+", "").toUpperCase();

            // Search for the certificate by thumbprint
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                if (certificate != null && normalizedThumbprint.equalsIgnoreCase(getThumbprint(certificate))) {
                    log.info("Certificate found for thumbprint: {}", thumbprint);
                    return (PrivateKey) keyStore.getKey(alias, null); // Retrieve private key
                }
            }
            throw new RuntimeException("Certificate with thumbprint " + thumbprint + " not found.");
        } catch (Exception e) {
            log.error("Error reading private key from Azure Key Store: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading private key: " + e.getMessage(), e);
        }
    }

    /**
     * Computes the thumbprint (SHA-1 hash) of a given certificate.
     *
     * @param certificate The X509Certificate object.
     * @return The computed thumbprint as a String.
     * @throws Exception If any error occurs while computing the hash.
     */
    private static String getThumbprint(X509Certificate certificate) throws Exception {
        byte[] encoded = certificate.getEncoded();
        byte[] digest = java.security.MessageDigest.getInstance("SHA-1").digest(encoded);

        // Convert digest to a hexadecimal thumbprint
        StringBuilder thumbprintBuilder = new StringBuilder();
        for (byte b : digest) {
            thumbprintBuilder.append(String.format("%02X", b));
        }
        return thumbprintBuilder.toString();
    }
}
