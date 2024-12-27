com.aetna.asgwy.webmw.exception.AsgwyGlobalException: Error in AV download api ::org.springframework.web.reactive.function.client.WebClientRequestException: Connection reset
	at com.aetna.asgwy.webmw.service.AVScanFileServiceV3.downloadAVScanFile(AVScanFileServiceV3.java:177) ~[classes/:na]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ com.aetna.asgwy.webmw.config.CorsFilterConfiguration$$Lambda/0x000002885a77b000 [DefaultWebFilterChain]
