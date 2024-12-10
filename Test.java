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

    private final WebClient.Builder webClientBuilder;

    private final DocKeyEncrptUtil docKeyEncrptUtil;

    public AVScanFileFilter(WebClient.Builder webClientBuilder, DocKeyEncrptUtil docKeyEncrptUtil) {
        this.webClientBuilder = webClientBuilder;
        this.docKeyEncrptUtil = docKeyEncrptUtil;
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
                String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");

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
        log.info("Decoding encrypted token...");
        try {
            PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(new RSADecrypter(privateKey));
            String plaintext = jweObject.getPayload().toString();
            SignedJWT signedJWT = SignedJWT.parse(plaintext);
            return signedJWT.getJWTClaimsSet().toJSONObject();
        } catch (Exception e) {
            log.error("Error decoding token: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error decoding token: " + e.getMessage());
        }
    }

    private Mono<Void> handleFileProcessing(String avFileRef, Map<String, Object> decodeJson, AVScanFileRequest avScanFileRequest, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            AVScanFileResponse avDownloadResponse = uploadAVScanFile(avFileRef);
            String avOriginalFileName = (String) decodeJson.get("cvs_av_original_file_name");
            String avOriginalFileType = (String) decodeJson.get("cvs_av_original_file_type");
            String avFileDownloadKey = (String) decodeJson.get("cvs_av_file_download_key");

            File tempFile = File.createTempFile(avOriginalFileName, "." + avOriginalFileType);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                String decodedContent = AVScanFileUtil.decryptValue(avDownloadResponse.getFile(), avFileDownloadKey);
                byte[] decodedBytes = Base64.getDecoder().decode(decodedContent);
                fos.write(decodedBytes);
            }

            log.info("Uploading file to gateway...");
            uploadFileOnGateway(tempFile, avScanFileRequest.getUploadData(), exchange);

            return tempFile;
        }).doFinally(tempFile -> {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }).then();
    }

    private AVScanFileResponse uploadAVScanFile(String fileReference) {
        log.info("Uploading file from AVScan...");
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(uploadUrl, uriBuilder -> uriBuilder.pathSegment(fileReference).build())
                    .header("x-api-key", apiKey)
                    .header("Authorization", "Bearer " + getJwtToken(fileReference))
                    .retrieve()
                    .bodyToMono(AVScanFileResponse.class)
                    .retry(3)
                    .block();
        } catch (Exception e) {
            log.error("Error uploading AVScan file: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error uploading AVScan file: " + e.getMessage());
        }
    }

    private void uploadFileOnGateway(File tempFile, FileUploadDatail uploadDatail, ServerWebExchange exchange) {
        log.info("Uploading file to gateway...");
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(tempFile));
        // Add additional fields to formData from uploadDatail...

        try {
            webClientBuilder.build()
                    .post()
                    .uri(gatewayapiUri + gatewayapiBasePath + fileURL)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(formData)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Error uploading file to gateway: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error uploading file to gateway: " + e.getMessage());
        }
    }

    private String getJwtToken(String fileReference) {
        log.info("Generating JWT token...");
        try {
            PrivateKey privateKey = AVScanFileUtil.readPrivateKey();
            Map<String, Object> header = Map.of(
                    "alg", "RS256",
                    "typ", "JWT",
                    "kid", kid
            );
            Map<String, Object> payload = Map.of(
                    "cvs_av_file_ref", fileReference,
                    "scope", "openid email",
                    "aud", "CVS-AVScan",
                    "iss", "Aetna-Sales-Gateway"
            );
            return createJwt(payload, header, privateKey);
        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error generating JWT token: " + e.getMessage());
        }
    }

    private String createJwt(Map<String, Object> payload, Map<String, Object> header, PrivateKey privateKey) {
        try {
            JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse(header)).build();
            JWSObject jwsObject = new JWSObject(jwsHeader, new com.nimbusds.jose.Payload(payload));
            jwsObject.sign(new RSASSASigner(privateKey));
            return jwsObject.serialize();
        } catch (Exception e) {
            log.error("Error creating JWT: {}", e.getMessage(), e);
            throw new AsgwyGlobalException("Error creating JWT: " + e.getMessage());
        }
    }
}
