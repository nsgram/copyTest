public static PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		//String filePath = "src/main/resources/encryptionKeys/recipient-np-private-key-asg.pem";
		//String privateKeyPEM = new String(Files.readAllBytes(Paths.get(filePath)));
		String privateKeyPEM = new String(pemKey);

		if (privateKeyPEM.contains("BEGIN RSA PRIVATE KEY")) {
			return readPKCS1PrivateKey(pemKey);
		} else {
			return readPKCS8PrivateKey(privateKeyPEM);
		}
	}



private static PrivateKey readPKCS1PrivateKey(String pemKey) throws IOException {
		try (PEMParser pemParser = new PEMParser(new StringReader(pemKey))) {
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
