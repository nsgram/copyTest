import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESDecryptionTest {

    public static void main(String[] args) {
        try {
            // Encrypted data and decryption key
            String encryptedData = "U2FsdGVkX1+ejNriRQZVMfS7T0VwNMtedvibJU+rwc";
            String decryptionKey = "x1Pq1RWnxzo9D0Xkgy12mwsbrVCSNkD"; // 32 chars for AES-256

            // Decode Base64 string
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // Initialize cipher for AES decryption
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(decryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Decrypt and print the result
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            System.out.println("Decrypted data: " + new String(decryptedBytes, "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
