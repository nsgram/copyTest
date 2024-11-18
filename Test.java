import java.util.Base64;
import java.nio.charset.StandardCharsets;

public static void main(String[] args) throws Exception {
    PrivateKey privateKey = readPrivateKey();
    String encryptedToken = "vvvvvv"; // Replace with actual JWE token
    String decryptedPayload = decryptJWE(encryptedToken, privateKey);

    System.out.println("Decrypted Payload: " + decryptedPayload);

    // Check if decryptedPayload is Base64 encoded
    if (isBase64(decryptedPayload)) {
        // Decode the Base64 encoded decrypted payload
        byte[] decodedBytes = Base64.getDecoder().decode(decryptedPayload);
        
        // Convert decoded bytes to a string (try both UTF-8 and default encoding)
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        
        // Print out the decoded string
        System.out.println("Decoded String: " + decodedString);
    } else {
        System.out.println("Decrypted payload is not Base64 encoded.");
    }
}

public static boolean isBase64(String str) {
    try {
        // Try decoding the string to check if it's valid Base64
        Base64.getDecoder().decode(str);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}
