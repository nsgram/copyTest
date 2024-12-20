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
  
  
  getting below exception but its work same api call is postman
 
 org.springframework.web.reactive.function.client.WebClientRequestException: Connection reset
	at com.aetna.asgwy.webmw.service.AVScanFileServiceV4.downloadAVScanFile(AVScanFileServiceV4.java:181) ~[classes/:na]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ com.aetna.asgwy.webmw.config.CorsFilterConfiguration$$Lambda/0x0000024431786b10 [DefaultWebFilterChain]
