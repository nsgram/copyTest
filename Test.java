public class AVScanFileUtil {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String filePath = recipient-np-private-key-asg.pem";
		String privateKeyPEM = new String(Files.readAllBytes(Paths.get(filePath)));

		if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
			return readPKCS1PrivateKey(filePath);
		} else {
			return readPKCS8PrivateKey(privateKeyPEM);
		}
	}
		private static PrivateKey readPKCS1PrivateKey(String filePath) throws IOException {
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
	private static PrivateKey readPKCS8PrivateKey(String privateKeyPEM)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");

		byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}
}

recipient-np-private-key-asg.pem
-----BEGIN RSA PRIVATE KEY-----
MIIEpQIBAAKCAQEAzhka94Qao5AX90If8sdYSUXCUtrA4sszSGq/w8JbxLS5om4o
4QOYQbCvEP239RCxpi9C0KoR0D1/kBReuDvoev2+zEKmKz2tJQWVN3T5C/bQdGjx
MWoBiZaUMJKbmb5tvsognYZbpAQGpOQT8HYL+dGgcJTX6msuV79CZiy66avFAi9i
WM22KqWGaHmI0GLmiWBO9VOJMTjugxh/AfdA8BT7XW5BoTRcdMZDEiggW/GwPCkW
XigNupO8QzB/s7TMIAo2PdWfO/qNTlgfd/AAzbpyslm7HKHER82dQpdmvHWTzx7M
+kM4MP9TJfTmesisB36qrmPkbvVp14HDaVCu5QIDAQABAoIBAFpGzvQqroyE2qqZ
EWuD/F0kLGmknqb5QT5wXA2Avjfcvg+zVz6xF2l23kiACqtTI63at9a1GFZyCcVv
1sm2nC/7pC7O5wC75HL6DhGbsFeYRlknercdawz8o27fWT8wFdxjp4O4zb17cfma
Wk2TN823ECMgX6+0QTl4crpUz474wDR4fwWT7gygdzOyWcw6HgBqSWSMn1a8Wx5b
Lk4ttN83Rm3VFDUdzb/J/U5stT2C6L6/G2/LWTlatdHjovdeW+hCz8mGZPQSxAZh
4ciEZMMV9+s5IPDLm+vmGIHB7fw/gDMsUr1P+YF+JLmdRaycuuAG6eZGr8ZetWUd
RDfEOcUCgYEA8ds8a3CxhKlG0Ho7pjZOE5d+8zCfVgGwOTh2aKWZnEGAvUU0Vhry
VIcnj3vxVI87hNg+OvEIoxKw0L9i0gqAkMfzWXcORYdxU47SgjgeMJ6s3BBSYSko
XPQ8XfU3kvNWXGBSRDgF9J9vf97MQvXLsBz2IqO4iLvsXTCemxoaNSMCgYEA2iaK
odA/MJBkRJXcz86iT1nJXyG98q5NODby+5GhisbaCRiK8hzlN4aIp/jlo6QwNKry
tkRjfnuDOQWkD5F0DaBjMoakHwyneTbHapfhUY6rwJ/ln4udLMuN/2P3xOQ6NNTt
tXLSKgYwwH2VWgelg9nAi0bwU76I7BYKVrwO4FcCgYEAv5UVFV6doCKfT8GjwpQ9
4NMe4vFfJjgMKieJ1HdpaReSLVvGi3apA0BUnBpoHr2fPio1kWchmlvR6GnSk+Nx
2D4rjHD8wyL5KGF4a2Kct5LaBzB+sSxzJr7pmtwGe8d6X46FXDisrwwkvLv2PgyK
nyUx7FqYcOe74k4DNYXFwNUCgYEArj+y5GVu09/B93atFeA7jZ5uVuqgysOnMovR
slg07KDBcBqGgbXK8XsolOMeqoNvbeJOVIBz6IfH3ThAP58zT98Y3TbmyVZWP2Ae
EWqXs4l+bzJJPBNMzIcbOTAkrMSQKbsa1KCOdrrTHi4xqeQJtTOttJJJTNsYi1tf
jsBQMakCgYEAyOQauRfM2UZAVMb0ohS25Xa6OcSQQfFSiH4rZ1IFklWP7c/Qoc+2
Vf4oi/gAzZyDf0/4aX31f6kVoUh74RF4x6nVhKRCQW6lAsx7oJ0hdRSS+FO4blRd
/riKn5Rp2RHo+lFl1ujS4abnAkLAhoii4M4Uo1GxYW0S7jj/PAud6RE=
-----END RSA PRIVATE KEY-----

cnvert recipient-np-private-key-asg.pem into String and refer it 
