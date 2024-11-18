import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    public static String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey) throws Exception {
        // Construct the claims set
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 hour expiry
                .jwtID((String) payload.getOrDefault("jti", UUID.randomUUID().toString()))
                .audience((String) payload.get("aud"))
                .issuer((String) payload.get("iss"))
                .subject((String) payload.get("sub"));

        // Add remaining payload claims
        payload.forEach((key, value) -> {
            if (!"jti".equals(key) && !"aud".equals(key) && !"iss".equals(key) && !"sub".equals(key)) {
                claimsSetBuilder.claim(key, value);
            }
        });

        JWTClaimsSet claimsSet = claimsSetBuilder.build();

        // Build the header
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .customParams(header)
                .build();

        // Sign the JWT
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(claimsSet.toJSONObject()));
        jwsObject.sign(new RSASSASigner(privateKey));

        return jwsObject.serialize();
    }
}
