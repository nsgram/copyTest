import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DecryptJWE {

    public static void main(String[] args) throws Exception {
        // Inputs
        String client = "DMR"; 
        String jwe = "eyJhbGciOiJSU0EtT0FFU...";
        String clientPrivateKeyPath = "keys_old/decrypted/private_dmr.pem";

        // Load Private Key
        String privateKeyPem = new String(Files.readAllBytes(Paths.get(clientPrivateKeyPath)));
        PrivateKey privateKey = PemUtils.getPrivateKeyFromPem(privateKeyPem);

        // Decrypt JWE Token
        JWEObject jweObject = JWEObject.parse(jwe);
        jweObject.decrypt(new RSADecrypter(privateKey));
        String plaintext = jweObject.getPayload().toString();

        // Parse JWT
        SignedJWT signedJWT = SignedJWT.parse(plaintext);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        System.out.println("Payload: " + claimsSet.toJSONObject());

        // Generate Download Token
        Map<String, Object> header = getHeader(client);
        Map<String, Object> payload = new HashMap<>();
        payload.put("cvs_av_file_ref", claimsSet.getStringClaim("cvs_av_file_ref"));
        payload.put("x-lob", "security-engineering");
        payload.put("scope", "openid email");
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("aud", "CVS-AVScan");
        payload.put("iss", "Visit-Manager");
        payload.put("sub", "download_bearer_token");

        String token = JwtUtils.createJwt(payload, header, privateKey);
        System.out.println("Generated Download Token: " + token);
    }

    private static Map<String, Object> getHeader(String client) {
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

        return header;
    }
}
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PemUtils {

    public static PrivateKey getPrivateKeyFromPem(String pemContent) throws Exception {
        PemReader pemReader = new PemReader(new StringReader(pemContent));
        PemObject pemObject = pemReader.readPemObject();
        byte[] content = pemObject.getContent();
        pemReader.close();

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}


import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSASigner;

import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    public static String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey) throws Exception {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse(header)).build();
        JWSObject jwsObject = new JWSObject(jwsHeader, new com.nimbusds.jose.Payload(payload));

        jwsObject.sign(new RSASSASigner(privateKey));
        return jwsObject.serialize();
    }
}








