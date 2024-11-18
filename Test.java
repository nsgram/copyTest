import io.jsonwebtoken.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemReader;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Decrypt {

    public static void main(String[] args) {
        try {
            decrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decrypt() throws Exception {

        // ** Inputs **
        String client = "DMR";
        String jweToken = "43Ag"; // Replace with actual token
        String privateKeyPath = "keys_old/decrypted/private_dmr.pem";

        // Load private key
        RSAPrivateKey privateKey = loadPrivateKey(privateKeyPath);

        // Decrypt JWE
        Security.addProvider(new BouncyCastleProvider());
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(jweToken);
        jwe.setKey(privateKey);

        String decryptedText = jwe.getPayload();

        // Decode JWT
        String[] jwtParts = decryptedText.split("\\.");
        String payload = new String(Base64.getDecoder().decode(jwtParts[1]));
        System.out.println("PAYLOAD PLAINTEXT: " + payload);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> decodedData = mapper.readValue(payload, Map.class);

        // Generate download token
        String kid = switch (client) {
            case "CLAIMS" -> "abc-e264-4028-9881-8c8cba20eb7c";
            case "DMR" -> "abc-49d3-4463-bd28-70efba817c1e";
            case "VM" -> "abc-fMuT8N188cHHbE";
            case "AQE" -> "abc-DMgpbbSDKV_0KTg";
            default -> "";
        };

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", kid);

        Map<String, Object> scannedPayload = new HashMap<>();
        scannedPayload.put("cvs_av_file_ref", decodedData.get("cvs_av_file_ref"));
        scannedPayload.put("x-lob", "security-engineering");
        scannedPayload.put("scope", "openid email");
        scannedPayload.put("jti", generateRandomId());
        scannedPayload.put("aud", "CVS-AVScan");
        scannedPayload.put("iss", "Visit-Manager");
        scannedPayload.put("sub", "download_bearer_token");

        JwtBuilder jwtBuilder = Jwts.builder()
                .setHeader(header)
                .setClaims(scannedPayload)
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256);

        System.out.println("\nINPUTS FOR THE DOWNLOAD CALL:");
        System.out.println("FILE NAME: " + decodedData.get("cvs_av_file_ref"));
        System.out.println("BEARER TOKEN FOR DOWNLOAD:");
        System.out.println(jwtBuilder.compact());
    }

    private static RSAPrivateKey loadPrivateKey(String privateKeyPath) throws IOException {
        PemReader pemReader = new PemReader(new FileReader(privateKeyPath));
        byte[] pemContent = pemReader.readPemObject().getContent();
        pemReader.close();

        return (RSAPrivateKey) SecurityUtils.decodePrivateKey(pemContent);
    }

    private static String generateRandomId() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
