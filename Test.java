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
        String encryptedToken = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.oVoxiyb1kjvyUlqqNFf0w_ZdSp1GlmWTETiozyhqHL-y3z2hkaQNOde1TH-yh2XHrbfLtKs8u8RRSIaC2JeqQSaFLw4u7ULuEBUzvZxuNkkknxsyC2k3GtAzrL0uC2aSHUwwsb2K_lCrw3DFiW_kt58O6JuUbwKN0AtsEHxmwvDqtsFqrAVXw0pXTSq0YdCi-FOYM_7zGwrDDMEXDg4yKoOYw2Xi16qpbTSIWql7uQ-ItFcBvn4xBay2SKjJ58J4l-gwK-RsU2aCF-ZyZBugdzY6AZBHlGXPGxlmAeji3rDd_sVvkfuWd8pQ-T1qkygmQz5ByiDyX96IJMiThREvgg.IDFEwuXUpch7wj1k.K-mEO-sMirkf8cobHFkOdc2LJS_3IVZKKyM7bdrmi5pJkkVga0j92R8JfB-NKlKPPx4b5_YPSO4kpnrIbECeh_5SoCpwLe1P--No3AvFSjTOZ9HT66QwHonU6dICbSpBKVGwqTpw2JxLa-gStmq-CGfjI-WFmz23M17rKwHAG-Qdm9JPPQO9eb3q_7YlTb8fBWjcsuZd7OQ4_o2sfEu8wsW8kfAEKQRuUsre8KR4LoHxsxRF6tP2Tm-E9IOXKt_nnmR_z68mg9FMuZITu5stPtOHNz37iFRw2x0Xp1d-INSPbh463tSfKYUHtMM2Zdg-ZlEPF1zSv9S4xkRlJImbWRLQp4JlRR7jOhwg17oWj2RA2w5FMDjOIu-w1Y2u1fxR6-vWJLkrcZ5s9cf8iosy6DXwfzOfgp-f_sf-n7rGy6XqfxuX_GBn8yOkjqg_hRmKUNnkZ4Flympfsq2CqXtamutNgTQrCN5JNq8BwH0DyaC4QIe8LREOrEwIqaVdrDy6t9mS_kJGIZOcYBP5w0jGKe2gKIl8OYi0W1jdgWlvYdE5GMmU8CZPP0APh5ofgc6qkvCcfqsKKTC-TMqLW8dugND2fZcmpMK1blkddaYyVkDdSdDTvoIxfrkyA0AX-rvBvm2E37TuS-rCb-qB95dFJ6CV3qTQs9yCXdEvZFoOt7YrDeQlCGjPpqW4z3ggPj46D1cZu1klcdHLeYYTj635GKVHGWG0B97oqPz9gp23rTR6yi4I6d4Wlll62XZYdD0-ySYCWig2zY7hQC56f5PgmJKTWXHvEIiDeNHDRtEd75NOWVx4Icq7v6cKDiGXhnU3IWuLuECIDcDVkEYRYBSz3DS8yIg2R3MxvmtIH0Z9dsNiSgC4unvkFBmnLzhuq9GRt5QLGBykyw.6hKYIr58SWC_lhqiM5RuuA";
        String decryptedPayload = decryptJWE(encryptedToken, privateKey);
        System.out.println("Decrypted Payload: " + decryptedPayload);
    }
