import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;

public class AVScanFileUtilV2 {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Use ClassPathResource to read the file from the resources folder
        Resource resource = new ClassPathResource("encryptionKeys/recipient-np-private-key-asg.pem");
        String privateKeyPEM = new String(Files.readAllBytes(resource.getFile().toPath()));

        if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
            return readPKCS1PrivateKey(resource);
        } else {
            return readPKCS8PrivateKey(privateKeyPEM);
        }
    }

    private static PrivateKey readPKCS1PrivateKey(Resource resource) throws IOException {
        try (PEMParser pemParser = new PEMParser(new FileReader(resource.getFile()))) {
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

    private static PrivateKey readPKCS8PrivateKey(String privateKeyPEM)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // Other methods remain unchanged (decryptValue, GenerateKeyAndIV, etc.)
}
