import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

public class AESFileDecryptor {

    public static void main(String[] args) throws Exception {
        // Input data
        String base64File = "U2FsdGVXXXXXXXXXXXXXXXXXXXX";
        String downloadKey = "x1Pq1RWnxzo9D0Xkqy12mwsbrvcsNkD";

        // Decrypt the file and save to temp directory
        File tempFile = saveDecryptedFile(base64File, downloadKey);
        System.out.println("Decrypted file saved at: " + tempFile.getAbsolutePath());
    }

    public static File saveDecryptedFile(String base64File, String downloadKey) throws Exception {
        // Step 1: Decode the Base64 string
        byte[] encryptedBytes = Base64Utils.decodeFromString(base64File);

        // Step 2: Decrypt using AES-256
        byte[] decryptedBytes = decryptAES(encryptedBytes, downloadKey);

        // Step 3: Write to a temporary file
        File tempFile = Files.createTempFile("decrypted-file-", ".tmp").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decryptedBytes);
        }

        return tempFile;
    }

    public static byte[] decryptAES(byte[] encryptedBytes, String key) throws Exception {
        // Generate a 256-bit key from the provided downloadKey
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes("UTF-8"));

        // Use the first 16 bytes of the key for the IV (Initialization Vector)
        IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0, 16));
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // Initialize AES Cipher in CBC mode with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        // Perform decryption
        return cipher.doFinal(encryptedBytes);
    }
}
