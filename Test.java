import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class FileProcessor {

    // AES Decryption method
    public static byte[] decryptFile(String encryptedData, String downloadKey) throws Exception {
        // Decode the Base64 strings
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = downloadKey.getBytes();

        // Ensure the key is 256-bit
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid key size. Key must be 32 bytes (256 bits).");
        }

        // Create AES SecretKey
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // Use a zero IV for simplicity (adjust as per your encryption method)
        byte[] iv = new byte[16]; // IV is 16 bytes for AES (default to zeros)
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize the Cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // Decrypt the data
        return cipher.doFinal(encryptedBytes);
    }

    // Write the decrypted bytes to a temporary file
    public static Path writeToTempFile(byte[] data, String fileName) throws Exception {
        Path tempDir = Files.createTempDirectory("tempFiles");
        Path tempFile = tempDir.resolve(fileName);

        // Write the bytes to the file
        Files.write(tempFile, data, StandardOpenOption.CREATE);

        return tempFile;
    }

    public static void main(String[] args) {
        try {
            // Sample input
            String encryptedFile = "U2FsdGVXXXXXXXXXXXXXXXXXXXX"; // Example Base64 encrypted string
            String downloadKey = "x1Pq1RWnxzo9D0Xkqy12mwsbrvcsNkD";

            // Decrypt the file
            byte[] decryptedBytes = decryptFile(encryptedFile, downloadKey);

            // Write the decrypted content to a temporary file
            Path tempFilePath = writeToTempFile(decryptedBytes, "decryptedFile.txt");

            System.out.println("File created at: " + tempFilePath.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
