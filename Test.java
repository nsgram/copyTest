@Component
public class AVScanFileFilter implements GatewayFilter {
	@Value("${avscan.header.kid}")
	private String kid;

	@Value("${avscan.header.x-api-key}")
	private String apiKey;

	@Value("${avscan.download.uri}")
	private String uri;

	@Value("${gatewayapi.uri}")
	private String gatewayapiUri;

	@Value("${gatewayapi.basePath}")
	private String gatewayapiBasePath;

	@Value("${gatewayapi.fileURL}")
	private String fileURL;

	private final WebClient.Builder webClientBuilder;

	private final WebClient avProxyWebClient;

	private final ProfileUtil profileUtil;

	public AVScanFileFilter(WebClient.Builder webClientBuilder, WebClient avProxyWebClient, ProfileUtil profileUtil) {
		this.webClientBuilder = webClientBuilder;
		this.avProxyWebClient = avProxyWebClient;
		this.profileUtil = profileUtil;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("AVScanFileFilter.filter() Start...");
		return exchange.getRequest().getBody().next().flatMap(dataBuffer -> {
			try {
				String body = dataBuffer.toString(StandardCharsets.UTF_8);
				AVScanFileRequest avScanFileRequest = new ObjectMapper().readValue(body, AVScanFileRequest.class);
				// Process the AVScan file
				Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
				String avFileRef = (String) decodeJson.get(CVSAVFILEREF);
				String avFileClean = (String) decodeJson.get(CVSAVISFILECLEAN);
				if ("Y".equalsIgnoreCase(avFileClean) && !StringUtils.isEmpty(avFileRef)) {
					log.info("File is clean, proceeding with download and upload.");
					return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
							.flatMap(response -> {
								exchange.getAttributes().put("fileUploadResponse", response);
								exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
								exchange.getResponse().setStatusCode(HttpStatus.CREATED);
								return chain.filter(exchange.mutate().build()).then(Mono.defer(() -> {
									try {
										return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
												.bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(response))));
									} catch (JsonProcessingException e) {
										return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED,
												"File is clean but not able to proccess it ::"
														+ e.getLocalizedMessage());
									}
								}));

							}).onErrorResume(e -> exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED,
									e.getMessage()));

				} else {
					log.error("The uploaded file appears to be unsafe...");
					return exceptionResponse(exchange, chain, HttpStatus.UNPROCESSABLE_ENTITY,
							"The uploaded file appears to be unsafe.");
				}
			} catch (Exception e) {
				log.error("Exception raised in processing AVScanFileFilter :::" + e.getMessage(), e);
				return exceptionResponse(exchange, chain, HttpStatus.EXPECTATION_FAILED,
						"Exception in processing request: " + e.getMessage());
			}
		});
	}

	private Map<String, Object> decodeEncryptedToken(String token) {
		log.info("decodeEncryptedToken() start...");
		Map<String, Object> jsonObject = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey(profileUtil.isActiveProdProfile());
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
			log.error("Exception raised in decode token :::" + e.getLocalizedMessage());
			throw new AsgwyGlobalException("Exception in decoding token :" + e.getLocalizedMessage());
		}
	}

	private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
			AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {

		return downloadAVScanFile(avFileRef).flatMap(avDownloadResponse -> {
			String avFileDownloadKey = (String) decodeJson.get(CVSAVFILEDOWNLOADKEY);
			return Mono.fromCallable(() -> {
				String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
				byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);

				String avOriginalFileName = (String) decodeJson.get(CVSAVORIGINALFILENAME);
				String avOriginalFileType = (String) decodeJson.get(CVSAVORIGINALFILETYPE);

				String tempDir = System.getProperty("java.io.tmpdir");
				// Write to a temporary file
				File tempFile = new File(tempDir, avOriginalFileName + "." + avOriginalFileType);

				try (FileOutputStream fos = new FileOutputStream(tempFile)) {
					fos.write(decodedBytes);
				}
				return tempFile;
			}).flatMap(tempFile -> uploadFileOnGateway(tempFile, avScanFileRequest, exchange)
					.doFinally(signalType -> cleanupTempFile(tempFile)));
		});

	}

	private Mono<AVScanFileResponse> downloadAVScanFile(String fileReference) {
		return avProxyWebClient.get().uri(uri + fileReference).header(XAPIKEY, apiKey)
				.header(AUTHORIZATION, getJwtToken(fileReference)).retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							log.error(" Exception raised in downloadAVScanFile api ");
							throw new AsgwyGlobalException("Exception in downloadAVScanFile api");
						}))
				.bodyToMono(AVScanFileResponse.class).doOnSuccess(response -> log.info(
						"File Downloaded from avscan api, statusDescription: " + response.getStatusDescription()));

	}

	private Mono<FileUploadResponse> uploadFileOnGateway(File tempFile, AVScanFileRequest avScanFileRequest,
			ServerWebExchange exchange) {
		log.info("uploadFileOnGateway() Start...");
		MultiValueMap<String, Object> formData = getGatewayFileUploadRequest(tempFile, avScanFileRequest);
		List<String> requestHeader = exchange.getRequest().getHeaders().get(WebConstants.TOKENVALUES);

		JavaCrypto jc = new JavaCrypto();
		String token = "";
		HttpHeaders headers = new HttpHeaders();
		if (requestHeader != null) {
			token = requestHeader.get(0);
			headers.add(TOKENVALS, jc.decrypt(token));
		}
		return webClientBuilder.build().post().uri(gatewayapiUri + gatewayapiBasePath + fileURL)
				.contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(formData).headers(h -> h.addAll(headers))
				.retrieve().onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							log.error(" Exception raised in gateway file upload api at gateway end "+errorBody);
							return Mono.error(new AsgwyGlobalException(errorBody));
						}))
				.bodyToMono(FileUploadResponse.class)
				.doOnSuccess(response -> log.info("Upload successful, document ID :::" + response.getQuoteDocumentId()))
				.doOnError(AsgwyGlobalException.class,
						ex -> log.error(" Exception raised in gateway application " + ex.getLocalizedMessage()));
	}

	private MultiValueMap<String, Object> getGatewayFileUploadRequest(File tempFile,
			AVScanFileRequest avScanFileRequest) {
		FileUploadDatail uploadDatail = avScanFileRequest.getUploadData();
		MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
		formData.add("quoteId", uploadDatail.getQuoteId());
		formData.add("groupId", uploadDatail.getGroupId());
		formData.add("docSubcategory", uploadDatail.getDocSubcategory());
		formData.add("quoteMissinfoDocInd", uploadDatail.getQuoteMissinfoDocInd());
		formData.add("quoteConcessDocInd", uploadDatail.getQuoteConcessDocInd());
		formData.add("uploadedUsrId", uploadDatail.getUploadedUsrId());
		formData.add("docTyp", uploadDatail.getDocTyp());
		formData.add("uploadedUsrNm", uploadDatail.getUploadedUsrNm());
		formData.add("docCategory", uploadDatail.getDocCategory());
		formData.add("docSize", uploadDatail.getDocSize());
		formData.add("docQuoteStage", uploadDatail.getDocQuoteStage());
		formData.add("quoteSubmitDocInd", uploadDatail.getQuoteSubmitDocInd());
		formData.add("docImqIndicator", uploadDatail.getDocImqIndicator());
		formData.add("docRcIndicator", uploadDatail.getDocRcIndicator());
		formData.add("docclaimsexpIndicator", uploadDatail.getDocclaimsexpIndicator());
		formData.add("file", new FileSystemResource(tempFile));
		if (uploadDatail.getQuoteConcessionId() != null) {
			formData.add("quoteConcessionId", uploadDatail.getQuoteConcessionId());
		}
		if (uploadDatail.getDocKey() != null) {
			formData.add("docKey", uploadDatail.getDocKey());
		}
		if (avScanFileRequest.getQuoteCommentDTO() != null) {
			formData.add("commentTxt", avScanFileRequest.getQuoteCommentDTO().getCommentTxt());
			formData.add("commentCategory", avScanFileRequest.getQuoteCommentDTO().getCommentCategory());
			formData.add("quoteSubmitCommentInd", avScanFileRequest.getQuoteCommentDTO().getQuoteSubmitCommentInd());
			formData.add("quoteConcessCommentInd", avScanFileRequest.getQuoteCommentDTO().getQuoteConcessCommentInd());
			formData.add("quoteMissinfoCommentInd",
					avScanFileRequest.getQuoteCommentDTO().getQuoteMissinfoCommentInd());
			formData.add("sentToFilenetInd", avScanFileRequest.getQuoteCommentDTO().getSentToFilenetInd());
		}
		return formData;
	}

	private String getJwtToken(String fileReference) {
		log.info("getJwtToken() Start...");
		String token = null;
		try {
			PrivateKey privateKey = AVScanFileUtil.readPrivateKey(profileUtil.isActiveProdProfile());
			// Generate Download Token
			Map<String, Object> header = getHeader();
			Map<String, Object> payload = new HashMap<>();
			payload.put(CVSAVFILEREF, fileReference);
			payload.put(XLOB, SECURITYENGINEERING);
			payload.put(SCOPE, OPENIDEMAIL);
			payload.put(JTI, UUID.randomUUID().toString().replace("-", "").substring(2));
			payload.put(AUD, CVSAVSCAN);
			payload.put(ISS, AETNASALESGATEWAY);
			payload.put(SUB, DOWNLOADBEARERTOKEN);
			token = createJwt(payload, header, privateKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			log.error("Exception raised in getJwtToken() :::" + e.getLocalizedMessage());
			throw new AsgwyGlobalException("Exception in getJwtToken(): " + e.getLocalizedMessage());
		}
		log.info(" getJwtToken() End");
		return "Bearer " + token;
	}

	private Map<String, Object> getHeader() {
		Map<String, Object> header = new HashMap<>();
		header.put(ALG, RS256);
		header.put(TYP, JWT);
		header.put(KID, kid);
		header.put(EXPIRESIN, new Date(System.currentTimeMillis() + 3600 * 1000));
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
			log.error(" Exception raised in createJwt() :::" + e.getLocalizedMessage());
			throw new AsgwyGlobalException("Exception in createJwt(): " + e.getLocalizedMessage());
		}
	}

	private Mono<? extends Void> exceptionResponse(ServerWebExchange exchange, GatewayFilterChain chain,
			HttpStatus status, String message) {
		exchange.getResponse().setStatusCode(status);
		return chain.filter(exchange.mutate().build()).then(Mono.defer(() -> {
			try {
				return exchange.getResponse().writeWith(Mono.just(
						exchange.getResponse().bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(message))));
			} catch (JsonProcessingException e) {
				return Mono.error(new AsgwyGlobalException("Exception at json processing: " + e.getLocalizedMessage()));
			}
		}));
	}

	private void cleanupTempFile(File tempFile) {
		if (tempFile.exists()) {
			try {
				java.nio.file.Files.delete(tempFile.toPath());
			} catch (IOException e) {
				log.error(" Exception in Delete Temporary file :::" + e.getLocalizedMessage());
			}
			log.info("Temporary file deleted :::" + tempFile.getAbsolutePath());
		}
	}

}
