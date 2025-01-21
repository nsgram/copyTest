@Slf4j
@Service
public class ReportsServiceImpl implements ReportsService {

	private final QuotesReportRepository quotesReportRepository;
	private final AsgwyLkupRepository asgwyLkupRepository;
	private final ScheduleReportsRepository scheduleReportsRepository;
	private final AzureFileService azureFileService;

	@Autowired
	public ReportsServiceImpl(QuotesReportRepository quotesReportRepository, AsgwyLkupRepository asgwyLkupRepository,
			ScheduleReportsRepository scheduleReportsRepository, AzureFileService azureFileService) {
		this.quotesReportRepository = quotesReportRepository;
		this.asgwyLkupRepository = asgwyLkupRepository;
		this.scheduleReportsRepository = scheduleReportsRepository;
		this.azureFileService = azureFileService;
	}

	@Override
	public ResponseEntity<byte[]> downloadOrScheduleQuotesReport(String userId, ReportsRequest reportsRequest) {
		log.info("downloadOrScheduleQuotesReport() start...");
		int threshold = getThreshold(ReportsEnum.QUOTE_REPORT.name());
		List<QuotesReport> quotesReportList = quotesReportRepository
				.findAll(QuotesReportSpecification.buildDynamicQuery(reportsRequest));

		if (quotesReportList.isEmpty()) {
			log.info("No Data found to generate Report");
			throw new AsgwyGlobalException(HttpStatus.NO_CONTENT.value(), "No Data found");
		}

		if (quotesReportList.size() <= threshold) {
			return generateCsvResponse(quotesReportList, reportsRequest.getStateCd());
		} else {
			return scheduleReport(userId, reportsRequest);
		}
	}

	@Override
	public ResponseEntity<byte[]> downloadReportByName(String reportFileName) {
		try {
			byte[] reportData = azureFileService.downloadReportByName(reportFileName);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportFileName)
					.contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(reportData.length).body(reportData);

		} catch (IOException e) {
			log.error("Error downloading file {}", reportFileName, e);
			throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to download file");
		}
	}

	private int getThreshold(String reportType) {
		try {
			return Integer.parseInt(asgwyLkupRepository.findValueByLabel(reportType));
		} catch (NumberFormatException e) {
			log.error("Invalid threshold value for report type {}", reportType, e);
			throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid threshold value");
		}
	}

	private ResponseEntity<byte[]> generateCsvResponse(List<QuotesReport> quotesReportList, String stateCd) {
		log.info("Generating CSV file...");
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				PrintWriter csvWriter = new PrintWriter(byteArrayOutputStream, true, StandardCharsets.UTF_8)) {
			String stateCode = stateCd != null && StringUtils.isNotBlank(stateCd) ? stateCd + "_" : "";

			String filename = "Quotes_Report_" + stateCode + new Random().nextInt(99) + 1 + "_"
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMDD")) + ".csv";
			csvWriter.println(AsgwyConstants.QuotesReportHeader());
			quotesReportList.stream().map(this::convertToCsvRow).forEach(csvWriter::println);

			byte[] csvBytes = byteArrayOutputStream.toByteArray();
			HttpHeaders headers = createCsvHeaders(filename, csvBytes.length);
			return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(csvBytes);

		} catch (IOException e) {
			log.error("Error generating CSV file", e);
			throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating CSV file");
		}
	}

	private HttpHeaders createCsvHeaders(String filename, int fileLength) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));
		return headers;
	}

	private ResponseEntity<byte[]> scheduleReport(String userId, ReportsRequest reportsRequest) {
		log.info("Scheduling report...");
		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonRequest = mapper.writeValueAsString(reportsRequest);

			Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("America/New_York")));
			ScheduleReports report = ScheduleReports.builder().reportType(ReportsEnum.QUOTE_REPORT.name())
					.requestDetails(jsonRequest).reportStatus(ReportsEnum.INPROGRESS.getValue()).requestedId(userId)
					.requestedDts(currentTime).updateUsrId(userId).updateDts(currentTime).build();

			scheduleReportsRepository.save(report);
			log.info("Report scheduled....");
			throw new AsgwyGlobalException(HttpStatus.OK.value(), "Report Got Scheduled");
		} catch (JsonProcessingException e) {
			log.error("Error processing JSON request", e);
			throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error scheduling report");
		}
	}

	private String convertToCsvRow(QuotesReport report) {
		return Arrays.stream(new String[] { report.getAgentFirstName(), report.getAgentLastName(), report.getAgentNPN(),
				report.getFirmName(), report.getGaName(), report.getGroupNm(), report.getEmployerIdNbr(),
				report.getGroupZipCd(), report.getStateNm(), report.getGroupLocAddrLine1Txt(),
				report.getGroupLocAddrLine2Txt(), report.getGroupLocCityNm(), report.getCurrCarrierTypDesc(),
				report.getCurrMedCarrierNm(), report.getGrpMewaAssoc(), report.getAetnaPeoInd(),
				safeString(report.getEffectiveDt()), safeString(report.getContractPeriodMoNbr()),
				safeString(report.getEligibleEntrdCnt()), safeString(report.getTotAvgEmpCnt()), report.getSicCd(),
				report.getSicNm(), safeString(report.getDefBrokerFeeAmt()), report.getUnionEmpInd(),
				safeString(report.getUnionEmpCnt()), report.getProductCd(), safeString(report.getParticipationCnt()),
				safeString(report.getEligibleDervdCnt()), safeString(report.getWaiverCnt()),
				safeString(report.getEligibleRetCnt()), safeString(report.getCobraEmpCnt()),
				safeString(report.getParticipationPct()), safeString(report.getFtEqvlntCnt()), report.getErisaCd(),
				report.getCurrTpaNm(), report.getAetnaSalesExe(), safeString(report.getTestGroupInd()),
				safeString(report.getConcessReqQuoteId()), report.getConcessReqStatusCd(),
				safeString(report.getConcessReqPct()), report.getConcessReqReasonTxt() }).map(this::escapeCsvField)
				.collect(Collectors.joining(","));
	}

	private String safeString(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	private String escapeCsvField(String field) {
		if (field == null)
			return "";
		if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
			return "\"" + field.replace("\"", "\"\"") + "\"";
		}
		return field;
	}

}
