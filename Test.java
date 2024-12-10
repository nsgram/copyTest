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
                // Parse request body
                String body = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString();
                AVScanFileRequest avScanFileRequest = new ObjectMapper().readValue(body, AVScanFileRequest.class);

                // Process AVScan file
                Map<String, Object> decodeJson = decodeEncryptedToken(avScanFileRequest.getAvToken());
                String avFileRef = (String) decodeJson.get("cvs_av_file_ref");
                String avFileClean = (String) decodeJson.get("cvs_av_is_file_clean");

                if ("Y".equalsIgnoreCase(avFileClean) && !StringUtils.isEmpty(avFileRef)) {
                    log.info("File is clean, proceeding with download and upload.");
                    return handleFileProcessing(avFileRef, decodeJson, avScanFileRequest, exchange)
                        .flatMap(response -> {
                            exchange.getAttributes().put("fileUploadResponse", response);
                            return chain.filter(exchange);
                        });
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
        try {
            PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(new RSADecrypter(privateKey));
            String plaintext = jweObject.getPayload().toString();
            SignedJWT signedJWT = SignedJWT.parse(plaintext);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            log.info("decodeEncryptedToken() end...");
            return claimsSet.toJSONObject();
        } catch (Exception e) {
            log.error("Error decoding token: {}", e.getLocalizedMessage(), e);
            throw new AsgwyGlobalException("Error decoding token: " + e.getLocalizedMessage());
        }
    }

    private Mono<FileUploadResponse> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson,
                                                          AVScanFileRequest avScanFileRequest,
                                                          ServerWebExchange exchange) {
        return Mono.using(
            // Resource Supplier
            () -> createTempFile(decodeJson, avFileRef),
            // Mono using Resource
            tempFile -> uploadFileOnGateway(tempFile, avScanFileRequest.getUploadData(), exchange),
            // Cleanup Logic
            this::cleanupTempFile
        );
    }

    private File createTempFile(Map<String, Object> decodeJson, String avFileRef) {
        try {
            String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
            String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
            File tempFile = File.createTempFile(avOriginalFileName, "." + avOriginalFileType);

            AVScanFileResponse avDownloadResponse = uploadAVScanFile(avFileRef);
            String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
                byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
                fos.write(decodedBytes);
            }
            return tempFile;
        } catch (IOException e) {
            log.error("Error creating temporary file: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error creating temporary file: " + e.getMessage());
        }
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            log.info("Temporary file cleanup status: {}", deleted ? "SUCCESS" : "FAILED");
        }
    }

    private AVScanFileResponse uploadAVScanFile(String fileReference) {
        log.info("Uploading file from AVScan...");
        try {
            return webClientBuilder.build().get()
                .uri(uploadUrl2, uriBuilder -> uriBuilder.pathSegment(fileReference).build())
                .header("x-api-key", apiKey)
                .retrieve()
                .bodyToMono(AVScanFileResponse.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .block();
        } catch (Exception e) {
            log.error("Error uploading AVScan file: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error uploading AVScan file: " + e.getMessage());
        }
    }

    private Mono<FileUploadResponse> uploadFileOnGateway(File tempFile, FileUploadDatail uploadDatail,
                                                         ServerWebExchange exchange) {
        log.info("Uploading file to gateway...");
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(tempFile));

        return webClientBuilder.build().post()
            .uri(gatewayapiUri + gatewayapiBasePath + fileURL)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(FileUploadResponse.class)
            .doOnError(e -> log.error("Error in gateway upload API: {}", e.getMessage(), e));
    }
}
