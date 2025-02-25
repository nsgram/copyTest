package com.aetna.asgwy.webmw.util;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class AVScanFileUtil {
    
    private static final String PRIVATE_KEY_ENV = "ENCRYPTION_PRIVATE_KEY";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyPEM = System.getenv(PRIVATE_KEY_ENV);
        
        if (privateKeyPEM == null || privateKeyPEM.isEmpty()) {
            throw new IllegalStateException("Private key is not set in environment variables.");
        }

        if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
            return readPKCS1PrivateKey(privateKeyPEM);
        } else {
            return readPKCS8PrivateKey(privateKeyPEM);
        }
    }

    public static String decryptValue(String encryptedString, String key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        
        byte[] cipherData = Base64.getDecoder().decode(encryptedString);
        byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        final byte[][] keyAndIV = generateKeyAndIV(32, 16, 1, saltData, key.getBytes(StandardCharsets.UTF_8), md5);
        
        SecretKeySpec keySpec = new SecretKeySpec(keyAndIV[0], "AES");
        IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

        byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
        Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCBC.init(Cipher.DECRYPT_MODE, keySpec, iv);
        
        byte[] decryptedData = aesCBC.doFinal(encrypted);
        
        // Clear sensitive data
        Arrays.fill(encrypted, (byte) 0);
        Arrays.fill(saltData, (byte) 0);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    private static PrivateKey readPKCS1PrivateKey(String pemKey) throws IOException {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemKey))) {
            Object object = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof PEMKeyPair) {
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IOException("Unsupported key format.");
            }
        }
    }

    private static PrivateKey readPKCS8PrivateKey(String privateKeyPEM)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        // Clear sensitive data
        Arrays.fill(keyBytes, (byte) 0);

        return keyFactory.generatePrivate(keySpec);
    }

    private static byte[][] generateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password,
                                             MessageDigest md) {

        int digestLength = md.getDigestLength();
        int requiredLength = ((keyLength + ivLength + digestLength - 1) / digestLength) * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;

        try {
            md.reset();
            while (generatedLength < keyLength + ivLength) {
                if (generatedLength > 0) {
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                }
                md.update(password);
                if (salt != null) {
                    md.update(salt, 0, 8);
                }
                md.digest(generatedData, generatedLength, digestLength);

                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }

                generatedLength += digestLength;
            }

            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0) {
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);
            }

            return result;
        } catch (DigestException e) {
            throw new RuntimeException("Error generating key and IV", e);
        } finally {
            Arrays.fill(generatedData, (byte) 0);
        }
    }
}
