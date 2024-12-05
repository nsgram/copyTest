import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class AVScanFileUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Private key as a string
    private static final String PRIVATE_KEY_PEM = 
        "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIEpQIBAAKCAQEAzhka94Qao5AX90If8sdYSUXCUtrA4sszSGq/w8JbxLS5om4o\n" +
        "4QOYQbCvEP239RCxpi9C0KoR0D1/kBReuDvoev2+zEKmKz2tJQWVN3T5C/bQdGjx\n" +
        "MWoBiZaUMJKbmb5tvsognYZbpAQGpOQT8HYL+dGgcJTX6msuV79CZiy66avFAi9i\n" +
        "WM22KqWGaHmI0GLmiWBO9VOJMTjugxh/AfdA8BT7XW5BoTRcdMZDEiggW/GwPCkW\n" +
        "XigNupO8QzB/s7TMIAo2PdWfO/qNTlgfd/AAzbpyslm7HKHER82dQpdmvHWTzx7M\n" +
        "+kM4MP9TJfTmesisB36qrmPkbvVp14HDaVCu5QIDAQABAoIBAFpGzvQqroyE2qqZ\n" +
        "EWuD/F0kLGmknqb5QT5wXA2Avjfcvg+zVz6xF2l23kiACqtTI63at9a1GFZyCcVv\n" +
        "1sm2nC/7pC7O5wC75HL6DhGbsFeYRlknercdawz8o27fWT8wFdxjp4O4zb17cfma\n" +
        "Wk2TN823ECMgX6+0QTl4crpUz474wDR4fwWT7gygdzOyWcw6HgBqSWSMn1a8Wx5b\n" +
        "Lk4ttN83Rm3VFDUdzb/J/U5stT2C6L6/G2/LWTlatdHjovdeW+hCz8mGZPQSxAZh\n" +
        "4ciEZMMV9+s5IPDLm+vmGIHB7fw/gDMsUr1P+YF+JLmdRaycuuAG6eZGr8ZetWUd\n" +
        "RDfEOcUCgYEA8ds8a3CxhKlG0Ho7pjZOE5d+8zCfVgGwOTh2aKWZnEGAvUU0Vhry\n" +
        "VIcnj3vxVI87hNg+OvEIoxKw0L9i0gqAkMfzWXcORYdxU47SgjgeMJ6s3BBSYSko\n" +
        "XPQ8XfU3kvNWXGBSRDgF9J9vf97MQvXLsBz2IqO4iLvsXTCemxoaNSMCgYEA2iaK\n" +
        "odA/MJBkRJXcz86iT1nJXyG98q5NODby+5GhisbaCRiK8hzlN4aIp/jlo6QwNKry\n" +
        "tkRjfnuDOQWkD5F0DaBjMoakHwyneTbHapfhUY6rwJ/ln4udLMuN/2P3xOQ6NNTt\n" +
        "tXLSKgYwwH2VWgelg9nAi0bwU76I7BYKVrwO4FcCgYEAv5UVFV6doCKfT8GjwpQ9\n" +
        "4NMe4vFfJjgMKieJ1HdpaReSLVvGi3apA0BUnBpoHr2fPio1kWchmlvR6GnSk+Nx\n" +
        "2D4rjHD8wyL5KGF4a2Kct5LaBzB+sSxzJr7pmtwGe8d6X46FXDisrwwkvLv2PgyK\n" +
        "nyUx7FqYcOe74k4DNYXFwNUCgYEArj+y5GVu09/B93atFeA7jZ5uVuqgysOnMovR\n" +
        "slg07KDBcBqGgbXK8XsolOMeqoNvbeJOVIBz6IfH3ThAP58zT98Y3TbmyVZWP2Ae\n" +
        "EWqXs4l+bzJJPBNMzIcbOTAkrMSQKbsa1KCOdrrTHi4xqeQJtTOttJJJTNsYi1tf\n" +
        "jsBQMakCgYEAyOQauRfM2UZAVMb0ohS25Xa6OcSQQfFSiH4rZ1IFklWP7c/Qoc+2\n" +
        "Vf4oi/gAzZyDf0/4aX31f6kVoUh74RF4x6nVhKRCQW6lAsx7oJ0hdRSS+FO4blRd\n" +
        "/riKn5Rp2RHo+lFl1ujS4abnAkLAhoii4M4Uo1GxYW0S7jj/PAud6RE=\n" +
        "-----END RSA PRIVATE KEY-----";

    public static PrivateKey readPrivateKey() throws Exception {
        if (PRIVATE_KEY_PEM.contains("BEGIN RSA PRIVATE KEY")) {
            return readPKCS1PrivateKey(PRIVATE_KEY_PEM);
        } else {
            return readPKCS8PrivateKey(PRIVATE_KEY_PEM);
        }
    }

    private static PrivateKey readPKCS1PrivateKey(String privateKeyPEM) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyPEM))) {
            Object object = pemParser.readObject();

            if (object instanceof PEMKeyPair) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IOException("Unsupported key format.");
            }
        }
    }

    private static PrivateKey readPKCS8PrivateKey(String privateKeyPEM) throws Exception {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
