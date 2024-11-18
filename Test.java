import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class DecryptAndSign {

    public static void main(String[] args) {
        try {
            decrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decrypt() throws Exception {
        // Inputs
        String client = "DMR";
        String jweToken = "Ag"; // Example JWE token, replace with actual
        String clientPrivateKeyPath = "keys_old/decrypted/private_dmr.pem";

        // Read private key from file
        String privateKeyPem = new String(Files.readAllBytes(Paths.get(clientPrivateKeyPath)));
        PrivateKey privateKey = PemUtils.getPrivateKeyFromPem(privateKeyPem);

        // Decrypt the JWE token
        JWEObject jweObject = JWEObject.parse(jweToken);
        RSADecrypter decrypter = new RSADecrypter(privateKey);
        jweObject.decrypt(decrypter);
        String payload = jweObject.getPayload().toString();

        System.out.println("Decrypted payload: " + payload);

        // Decode JWT from the decrypted payload
        String[] jwtParts = payload.split("\\.");
        String base64UrlPayload = jwtParts[1];
        String decodedPayload = new String(Base64.getUrlDecoder().decode(base64UrlPayload), StandardCharsets.UTF_8);

        System.out.println("PAYLOAD PLAINTEXT: " + decodedPayload);

        // Create header based on client
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");

        switch (client) {
            case "CLAIMS":
                header.put("kid", "abc-e264-4028-9881-8c8cba20eb7c");
                break;
            case "DMR":
                header.put("kid", "abc-49d3-4463-bd28-70efba817c1e");
                break;
            case "VM":
                header.put("kid", "abc-fMuT8N188cHHbE");
                break;
            case "AQE":
                header.put("kid", "abc-DMgpbbSDKV_0KTg");
                break;
            case "CHAT":
                header.put("kid", "");
                break;
        }

        // Create payload for download token
        Map<String, Object> scannedPayload = new HashMap<>();
        scannedPayload.put("cvs_av_file_ref", "example_file_ref"); // Replace with actual value
        scannedPayload.put("x-lob", "security-engineering");
        scannedPayload.put("scope", "openid email");
        scannedPayload.put("jti", java.util.UUID.randomUUID().toString());
        scannedPayload.put("aud", "CVS-AVScan");
        scannedPayload.put("iss", "Visit-Manager");
        scannedPayload.put("sub", "download_bearer_token");

        // Sign the token
        String jwtToken = Jwts.builder()
                .setHeader(header)
                .setClaims(scannedPayload)
                .signWith(Keys.hmacShaKeyFor(privateKeyPem.getBytes()), SignatureAlgorithm.RS256)
                .compact();

        System.out.println("BEARER TOKEN FOR DOWNLOAD: " + jwtToken);
    }
}


<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.31</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk15on</artifactId>
    <version>1.70</version>
</dependency>
