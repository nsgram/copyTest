public class AVScanFileEncrtption {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static PrivateKey readPrivateKey() throws Exception {
        String filePath = "C:\\Data\\ASGWY\\Documents\\recipient-np-private-key-asg.pem"; // Update with your file path
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get(filePath)));

        if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
            return readPKCS1PrivateKey(filePath);
        } else {
            return readPKCS8PrivateKey(privateKeyPEM);
        }
    }

    public static PrivateKey readPKCS1PrivateKey(String filePath) throws IOException {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();

            if (object instanceof PEMKeyPair) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IOException("Unsupported key format.");
            }
        }
    }

    private static PrivateKey readPKCS8PrivateKey(String privateKeyPEM) throws Exception {
        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    public static String decryptJWE(String jweString, PrivateKey privateKey) throws JOSEException, ParseException {
        JWEObject jweObject = JWEObject.parse(jweString);
        jweObject.decrypt(new RSADecrypter(privateKey));
        return jweObject.getPayload().toString();
    }
    public static void main(String[] args) throws Exception {
        PrivateKey privateKey = readPrivateKey();
        String encryptedToken = "vvvvvv";
        String decryptedPayload = decryptJWE(encryptedToken, privateKey);
        System.out.println("Decrypted Payload: " + decryptedPayload);
    }


<dependency>   
			<groupId>org.bouncycastle</groupId> 
			<artifactId>bcpkix-jdk18on</artifactId>
			 <version>1.79</version>
		</dependency>
<dependency>   
			<groupId>org.bouncycastle</groupId> 
			<artifactId>bcprov-jdk18on</artifactId>
			 <version>1.79</version>
		</dependency>
