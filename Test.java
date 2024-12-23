
// Load the keystore
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(new File("src/main/resources/malware.qa.jks")), "malware".toCharArray());

			// Create SSL context
			SSLContext sslContext = SSLContextBuilder.create().loadKeyMaterial(keyStore, "malware".toCharArray())
					.build();

			// Create HTTP client with SSL context
			HttpClient httpClient = HttpClientBuilder.create().setSSLContext(sslContext).build();

			// Create request factory
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
					(org.apache.hc.client5.http.classic.HttpClient) httpClient);

			// Create RestTemplate
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			
			getting below error

I am using 
<dependency>
			<groupId>org.apache.httpcomponents.client5</groupId>
			<artifactId>httpclient5</artifactId>
			<version>5.3.1</version>
		</dependency>

java.lang.ClassCastException: class org.apache.http.impl.client.InternalHttpClient cannot
 be cast to class org.apache.hc.client5.http.classic.HttpClient (org.apache.http.impl.client.InternalHttpClient 
 and org.apache.hc.client5.http.classic.HttpClient are in unnamed module of loader 'app')
