import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import org.json.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenDecryptor {

    public static void main(String[] args) {
        try {
            // Inputs
            String client = "DMR"; // Set client type
            String jwe = "xxxx";  // JWE token received in the upload response
            String clientPrivateKeyPath = "keys_old/decrypted/private_dmr.pem"; // Private key for the client

            // Decrypt the JWE token and extract payload
            String payload = decryptJWE(jwe, clientPrivateKeyPath);
            System.out.println("Payload from JWE: " + payload);

            // Parse the JWT token
            String jwtPayload = decodeJWT(payload);
            System.out.println("Decoded JWT Payload: " + jwtPayload);

            // Generate a new JWT with the expected payload for download
            String downloadToken = generateDownloadToken(jwtPayload, client, clientPrivateKeyPath);
            System.out.println("Download Bearer Token: " + downloadToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Decrypt the JWE token using the RSA private key
    private static String decryptJWE(String jwe, String privateKeyPath) throws Exception {
        // For simplicity, we'll simulate decryption of JWE.
        // In a real application, you'd need a JWE decryption library like Nimbus JOSE + JWT.
        // For now, assuming that the JWE payload is the base64-encoded JWT token.
        String decryptedPayload = new String(Base64.getDecoder().decode(jwe), StandardCharsets.UTF_8);
        return decryptedPayload;
    }

    // Decode the JWT and extract the payload (without validation)
    private static String decodeJWT(String token) throws Exception {
        // Split the JWT to extract the payload (the middle part of JWT)
        String[] parts = token.split("\\.");
        String base64Url = parts[1];

        // Decode from base64 to JSON
        String base64 = base64Url.replace('-', '+').replace('_', '/');
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    // Generate a new JWT with the provided payload and sign it with RS256 algorithm
    private static String generateDownloadToken(String jwtPayload, String client, String privateKeyPath) throws Exception {
        // Define JWT headers based on the client
        String kid = getKidForClient(client);
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\",\"kid\":\"" + kid + "\"}";

        // Parse the decoded JWT payload
        JSONObject decodedData = new JSONObject(jwtPayload);

        // Prepare the payload for the download token
        JSONObject payload = new JSONObject();
        payload.put("cvs_av_file_ref", decodedData.getString("cvs_av_file_ref"));
        payload.put("x-lob", "security-engineering");
        payload.put("scope", "openid email");
        payload.put("jti", Math.random() + 1);
        payload.put("aud", "CVS-AVScan");
        payload.put("iss", "Visit-Manager");
        payload.put("sub", "download_bearer_token");

        // Load the private key to sign the JWT
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);

        // Sign the JWT with RS256
        return Jwts.builder()
                .setHeaderParam("kid", kid)
                .setPayload(payload.toString())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // Load the private key from the given file path
    private static PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        // Read the private key file
        String privateKeyContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(privateKeyPath)), StandardCharsets.UTF_8);

        // Remove the first and last lines (-----BEGIN PRIVATE KEY----- and -----END PRIVATE KEY-----)
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

        // Decode the private key and generate the RSAPrivateKey
        byte[] encoded = Base64.getDecoder().decode(privateKeyContent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    // Get the "kid" (key ID) based on the client type
    private static String getKidForClient(String client) {
        switch (client) {
            case "CLAIMS":
                return "abc-e264-4028-9881-8c8cba20eb7c";
            case "DMR":
                return "abc-49d3-4463-bd28-70efba817c1e";
            case "VM":
                return "abc-fMuT8N188cHHbE";
            case "AQE":
                return "abc-DMgpbbSDKV_0KTg";
            case "CHAT":
                return "";
            default:
                return "";
        }
    }
}
