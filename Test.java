import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;

public class AVScanFileEncrtption {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey readPrivateKey() throws Exception {
        String filePath = "C:\\Data\\ASGWY\\Documents\\recipient-np-private-key-asg.pem"; // Update with your file path
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get(filePath)));

        if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
            return readPKCS1PrivateKey(filePath);
        } else {
            return readPKCS8PrivateKey(privateKeyPEM);
        }
    }

    public static PrivateKey readPKCS1PrivateKey(String filePath) throws IOException {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
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
        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static String decryptJWE(String jweString, PrivateKey privateKey) throws JOSEException, ParseException {
        JWEObject jweObject = JWEObject.parse(jweString);
        jweObject.decrypt(new RSADecrypter(privateKey));
        return jweObject.getPayload().toString();
    }

    public static void main(String[] args) throws Exception {
        PrivateKey privateKey = readPrivateKey();
        String encryptedToken = "vvvvvv"; // Replace with actual JWE token
        String decryptedPayload = decryptJWE(encryptedToken, privateKey);

        System.out.println("Decrypted Payload: " + decryptedPayload);

        // Clean the decrypted payload to remove unwanted characters and ensure proper padding
        String cleanedPayload = cleanBase64String(decryptedPayload);

        // Add padding if necessary
        if (cleanedPayload.length() % 4 != 0) {
            cleanedPayload += "=".repeat(4 - cleanedPayload.length() % 4); // Add padding
        }

        // Check if the string is Base64 and decode it
        if (isBase64(cleanedPayload)) {
            byte[] decodedBytes = Base64.getDecoder().decode(cleanedPayload);
            
            // Try to decode it as a UTF-8 string
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            System.out.println("Decoded String: " + decodedString);
        } else {
            System.out.println("Decrypted payload is not valid Base64 encoded.");
        }
    }

    // Helper method to clean non-Base64 characters from the string
    public static String cleanBase64String(String input) {
        return input.replaceAll("[^A-Za-z0-9+/=]", "");
    }

    // Helper method to check if a string is valid Base64
    public static boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
