@Slf4j
@Component
public class AVScanFileFilter implements GatewayFilter {
	@Value("${avscan.header.kid}")
	private String kid;

	@Value("${avscan.header.x-api-key}")
	private String apiKey;

	@Value("${avscan.upload.url}")
	private String uploadUrl;

	@Value("${gatewayapi.uri}")
	private String gatewayapiUri;

	@Value("${gatewayapi.basePath}")
	private String gatewayapiBasePath;

	@Value("${gatewayapi.fileURL}")
	private String fileURL;

	@Value("${avscan.upload.url2}")
	private String uploadUrl2;

	private final WebClient.Builder webClientBuilder;

	public AVScanFileFilter(WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;

	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("AVScanFileFilter.filter() Start...");

		return exchange.getRequest().getBody().next().flatMap(dataBuffer -> {
			try {
				// Parse request body into AVScanFileRequest
				String body = dataBuffer.toString(StandardCharsets.UTF_8);
				AVScanFileRequest avScanFileRequest = new ObjectMapper().readValue(body, AVScanFileRequest.class);

				// Process the AVScan file
				Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
				String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
				String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");

				if ("Y".equalsIgnoreCase(avFileClean) && !StringUtils.isEmpty(avFileRef)) {
					log.info("File is clean, proceeding with download and upload.");

					return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
							.then(chain.filter(exchange));

				} else {
					log.error("The uploaded file appears to be unsafe.");
					return Mono.error(new AsgwyGlobalException("The uploaded file appears to be unsafe."));
				}
			} catch (Exception e) {
				log.error("Error processing AVScanFileFilter: {}", e.getMessage(), e);
				return Mono.error(new AsgwyGlobalException("Error processing request: " + e.getMessage()));
			}
		});
	}

	private Map<String, Object> decodeEncryptedToken(String token) {
		log.info("decodeEncryptedToken() start...");
		Map<String, Object> jsonObject = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
			// Decrypt JWE Token
			JWEObject jweObject = JWEObject.parse(token);
			jweObject.decrypt(new RSADecrypter(privateKey));
			String plaintext = jweObject.getPayload().toString();
			// Parse JWT
			SignedJWT signedJWT = SignedJWT.parse(plaintext);
			JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
			jsonObject = claimsSet.toJSONObject();
			log.info("decodeEncryptedToken() end...");
			return jsonObject;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | ParseException | JOSEException e) {
			log.error("error in decode token ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("error in decode token ::" + e.getLocalizedMessage());
		}
	}

	private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
			AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
		return Mono.using(
				// Resource Supplier
				() -> {
					String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
					String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
					String tempDir = System.getProperty("java.io.tmpdir");
					// Write to a temporary file
					File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);
					AVScanFileResponse avDownloadResponse = uploadAVScanFile(avFileRef);
					String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

					try (FileOutputStream fos = new FileOutputStream(tempFile)) {
						String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(),
								avFileDownloadKey);
						byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
						fos.write(decodedBytes);
					}
					return tempFile;
				},
				// Mono using Resource
				tempFile -> {
					log.info("Uploading file to gateway...");
					FileUploadResponse response = uploadFileOnGateway(tempFile, avScanFileRequest.getUploadData(),
							exchange);
					log.info("Uploading file to gateway document id..." + response.getQuoteDocumentId());
					return Mono.just(response);
				},
				// Cleanup Logic
				tempFile -> {
					if (tempFile != null && tempFile.exists()) {
						boolean deleted = tempFile.delete();
						log.info("Temporary file cleanup status: {}", deleted ? "SUCCESS" : "FAILED");
					}
				});
	}

	private AVScanFileResponse uploadAVScanFile(String fileReference) {
		log.info("Uploading file from AVScan...");
		try {
			return webClientBuilder.build().get()
					.uri(uploadUrl2, uriBuilder -> uriBuilder.pathSegment(fileReference).build())
					.header("x-api-key", apiKey).header("Authorization", "Bearer " + getJwtToken(fileReference))
					.retrieve().bodyToMono(AVScanFileResponse.class)
					.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))).toFuture().get();
		} catch (Exception e) {
			log.error("Error uploading AVScan file: {}", e.getMessage(), e);
			throw new AsgwyGlobalException("Error uploading AVScan file: " + e.getMessage());
		}
	}

	private FileUploadResponse uploadFileOnGateway(File tempFile, FileUploadDatail uploadDatail,
			ServerWebExchange exchange) {
		log.info("uploadFileOnGateway() Start...");
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("quoteId", uploadDatail.getQuoteId());
		formData.add("docSubcategory", uploadDatail.getDocSubcategory());
		formData.add("quoteMissinfoDocInd", uploadDatail.getQuoteMissinfoDocInd());
		formData.add("quoteConcessDocInd", uploadDatail.getQuoteConcessDocInd());
		formData.add("uploadedUsrId", uploadDatail.getUploadedUsrId());
		formData.add("docTyp", uploadDatail.getDocTyp());
		formData.add("uploadedUsrNm", uploadDatail.getUploadedUsrNm());
		formData.add("docCategory", uploadDatail.getDocCategory());
		formData.add("quoteId", uploadDatail.getQuoteConcessionId());
		formData.add("docSize", uploadDatail.getDocSize());
		formData.add("docQuoteStage", uploadDatail.getDocQuoteStage());
		formData.add("quoteSubmitDocInd", uploadDatail.getQuoteSubmitDocInd());
		formData.add("quoteConcessionId", uploadDatail.getQuoteConcessionId());
		formData.add("file", new FileSystemResource(tempFile));
		formData.add("docKey", uploadDatail.getDocKey());
		List<String> requestHeader = exchange.getRequest().getHeaders().get("tokenvalues");

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
//			headers.add("tokenvals", token);
			log.info("--decrypted tokenvals -- " + jc.decrypt(token));
			headers.add("tokenvals", jc.decrypt(token));
		}

		try {
			FileUploadResponse response = webClientBuilder.build().method(HttpMethod.POST)
					.uri(gatewayapiUri + gatewayapiBasePath + fileURL).contentType(MediaType.MULTIPART_FORM_DATA)
					.bodyValue(formData).headers(header -> header.addAll(headers)).retrieve()
					.onStatus(status -> !status.is2xxSuccessful(),
							clientResponse -> clientResponse.bodyToMono(FileUploadResponse.class).flatMap(errorBody -> {
								log.error("Exception in gateway upload file api");
								throw new AsgwyGlobalException("Exception in  gateway upload file api");
							}))
					.bodyToMono(FileUploadResponse.class)// .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
					.toFuture().get();
			if (response != null) {
				log.info("uploadFileOnGateway() End...");
				return response;
			} else {
				throw new AsgwyGlobalException("Null Response from gateway upload api");
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error("Error in gateway upload api   ::{}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in gateway upload api ::" + e.getLocalizedMessage());
		}

	}

	private String getJwtToken(String fileReference) {
		log.info("getJwtToken() Start...");
		String token = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
			// Generate Download Token
			Map<String, Object> header = getHeader();
			Map<String, Object> payload = new HashMap<>();
			payload.put("cvs_av_file_ref", fileReference);
			payload.put("x-lob", "security-engineering");
			payload.put("scope", "openid email");
			payload.put("jti", Long.toString((long) ((Math.random() + 1) * 1_000_000), 36).substring(2));
			payload.put("aud", "CVS-AVScan");
			payload.put("iss", "Aetna-Sales-Gateway");
			payload.put("sub", "download_bearer_token");
			token = createJwt(payload, header, privateKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			log.error("Error in getJwtToken() :: {}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in getJwtToken() ::" + e.getLocalizedMessage());
		}
		log.info(" getJwtToken() End");
		return token;
	}

	private Map<String, Object> getHeader() {
		Map<String, Object> header = new HashMap<>();
		header.put("alg", "RS256");
		header.put("typ", "JWT");
		header.put("kid", kid);
		header.put("expiresIn", new Date(System.currentTimeMillis() + 3600 * 1000));
		return header;
	}

	private String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey) {
		try {
			log.info("createJwt() Start...");
			JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse(header)).build();
			JWSObject jwsObject = new JWSObject(jwsHeader, new com.nimbusds.jose.Payload(payload));
			jwsObject.sign(new RSASSASigner(privateKey));
			log.info("createJwt() end...");
			return jwsObject.serialize();
		} catch (ParseException | JOSEException e) {
			log.error("Error in createJwt() :: {}", e.getLocalizedMessage());
			throw new AsgwyGlobalException("Error in createJwt() ::" + e.getLocalizedMessage());
		}
	}
}

@Slf4j
@Configuration
public class AVScanFileConfig {
	@Bean
	public RouteLocator aVScanFileRouting(RouteLocatorBuilder builder, AVScanFileFilter avScanFileFilter) {
		log.info(" ******* AVScan API RouteLocator ******* ");

		return builder.routes()
				.route("avscan-file-route", route -> route.path("/asgwy-webmw/api/v1/avscan/uploadx").and()
						.method(HttpMethod.POST).filters(filter -> filter.filter(avScanFileFilter)).uri("no://op"))
				.build();
	}

}


My all code is working fine I am able get response 200 ok
but I want FileUploadResponse as bodu from able filter
remove sonar and security issue
