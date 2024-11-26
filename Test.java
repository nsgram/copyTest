import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESDecryptionDebug {

    public static void main(String[] args) {
        try {
            // Encrypted data
            String encryptedData = "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc";

            // Decryption key (check and normalize)
            String decryptionKey = "x1Pq1RWnxzo9D0Xkgy12mwsbrVCSNkD".trim();
            decryptionKey = decryptionKey.replaceAll("[^\\x20-\\x7E]", "").trim();

            // Debugging: Print key length
            System.out.println("Key length (characters): " + decryptionKey.length());
            System.out.println("Key length (bytes): " + decryptionKey.getBytes("UTF-8").length);

            // Validate key length
            if (decryptionKey.getBytes("UTF-8").length != 32) {
                throw new IllegalArgumentException("Key length must be exactly 32 bytes for AES-256");
            }

            // Decode Base64 string
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // Initialize cipher for AES decryption
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Decrypt data
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            System.out.println("Decrypted data: " + new String(decryptedBytes, "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
