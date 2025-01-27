import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;

public class KeyReader {

    public static PrivateKey readPKCS1PrivateKey(String filePath) throws IOException {
        // Load the file from the classpath
        ClassPathResource resource = new ClassPathResource(filePath);

        // Use a try-with-resources block to safely handle resource closing
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             PEMParser pemParser = new PEMParser(reader)) {

            Object object = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof PEMKeyPair) {
                // Handle PEMKeyPair
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                // Handle PrivateKeyInfo
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IOException("Unsupported key format.");
            }
        }
    }
}
