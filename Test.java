import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.MessageDigest;

public class AES256Decryption {

    public static void main(String[] args) {
        try {
            // Encrypted data
            String encryptedData = "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc";

            // Download key (31 characters)
            String downloadKey = "x1Pq1RWnxzo9D0Xkgy12mwsbrVCSNkD";

            // Hash the key to create a 32-byte AES-256 key
            byte[] aesKey = hashKey(downloadKey);

            // Decode Base64-encoded encrypted data
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // Initialize AES-256 cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Perform decryption
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, "UTF-8");

            // Output the decrypted data
            System.out.println("Decrypted data: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hashes the given key using SHA-256 to generate a 32-byte AES-256 key.
     *
     * @param key The input key (any length).
     * @return A 32-byte key derived from the input key.
     */
    private static byte[] hashKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(key.getBytes("UTF-8")); // Generate a 32-byte key
    }
}
