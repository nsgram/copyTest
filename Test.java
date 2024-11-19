public class AVScanFileDecryption {

	public static void main(String[] args) throws Exception {
		// Inputs
		String jwe = "QQ";

		PrivateKey privateKey = AVScanFileEncrtption.readPrivateKey();

		// Decrypt JWE Token
		JWEObject jweObject = JWEObject.parse(jwe);
		jweObject.decrypt(new RSADecrypter(privateKey));
		String plaintext = jweObject.getPayload().toString();

		// Parse JWT
		SignedJWT signedJWT = SignedJWT.parse(plaintext);
		JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
		System.out.println("Payload: " + claimsSet.toJSONObject());

		// Generate Download Token
		Map<String, Object> header = getHeader();
		Map<String, Object> payload = new HashMap<>();
		payload.put("cvs_av_file_ref", claimsSet.getStringClaim("cvs_av_file_ref"));
		payload.put("x-lob", "security-engineering");
		payload.put("scope", "openid email");
		// payload.put("jti", UUID.randomUUID().toString());
		payload.put("jti", Long.toString((long) ((Math.random() + 1) * 1_000_000), 36).substring(2));
		payload.put("aud", "CVS-AVScan");
		payload.put("iss", "Aetna-Sales-Gateway");
		payload.put("sub", "download_bearer_token");

		String token = createJwt(payload, header, privateKey);
		System.out.println("Generated Download Token: " + token);
	}

	private static Map<String, Object> getHeader() {
		Map<String, Object> header = new HashMap<>();
		header.put("alg", "RS256");
		header.put("typ", "JWT");
		header.put("kid", "dd");
		header.put("expiresIn", new Date(System.currentTimeMillis() + 3600 * 1000));

		return header;
	}

	public static String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey)
			throws Exception {
		JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse(header)).build();
		JWSObject jwsObject = new JWSObject(jwsHeader, new com.nimbusds.jose.Payload(payload));

		jwsObject.sign(new RSASSASigner(privateKey));
		return jwsObject.serialize();
	}
}
