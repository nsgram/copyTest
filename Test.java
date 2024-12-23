@Bean
	public WebClient proxyWebClient() throws Exception {
		// Load KeyStore
		KeyStore keyStore = KeyStore.getInstance("JKS");
		try (InputStream keyStoreStream = this.getClass().getClassLoader().getResourceAsStream("malware.qa.jks")) {
			keyStore.load(keyStoreStream, "malware".toCharArray());
		}
		logCertificateDetails(keyStore);
		
		// Initialize KeyManagerFactory with KeyStore
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, "malware".toCharArray());

		// Initialize TrustManagerFactory with KeyStore
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		// Build SSL context using KeyManagerFactory and TrustManagerFactory
		SslContext sslContext = SslContextBuilder.forClient().keyManager(keyManagerFactory)
				.trustManager(trustManagerFactory).build();

		// Configure HttpClient with SSL
		HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

		// Build WebClient
		return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}


avscan.upload.url=https://sit1-api.cvshealth.com/file/scan/download/v1/files/{fileReference}

private AVScanFileResponse downloadAVScanFile(String fileReference) {
		try {
			log.info("downloadAVScanFile() Start...");
			log.info("downloadAVScanFile() fileReference--->" + fileReference);
			return proxyWebClient.method(HttpMethod.GET).uri(uploadUrl, fileReference).header("x-api-key", apiKey)
					.header("Authorization", "Bearer " + getJwtToken(fileReference)).retrieve()
					.onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(AVScanFileResponse.class).flatMap(errorBody -> {
								log.error("Error in  avscan donload file api");
								throw new AsgwyGlobalException("Error in avscan download file api");
							}))
					.bodyToMono(AVScanFileResponse.class).toFuture().get();
		} catch (InterruptedException e) {
			log.error("Error in AV download api ::{}", e.getLocalizedMessage());
			Thread.currentThread().interrupt();
			throw new AsgwyGlobalException("Error in AV download api ::" + e.getLocalizedMessage());

		} catch (ExecutionException e) {
			log.error("Error in AV download api ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in AV download api ::" + e.getLocalizedMessage());
		}
	}
	
	
	
	I have tried with multiple things but its not working getting below error
	
	com.aetna.asgwy.webmw.exception.AsgwyGlobalException: Error in AV download api ::org.springframework.web.reactive.function.client.WebClientRequestException: Connection reset
	at com.aetna.asgwy.webmw.service.AVScanFileServiceV4.downloadAVScanFile(AVScanFileServiceV4.java:181) ~[classes/:na]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ com.aetna.asgwy.webmw.config.CorsFilterConfiguration$$Lambda/0x0000021681783620 [DefaultWebFilterChain]
	
	Same requst is working in  postman
