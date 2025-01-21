package com.aetna.asgwy.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aetna.asgwy.constants.AsgwyConstants;
import com.aetna.asgwy.dto.QuotesReport;
import com.aetna.asgwy.enums.ReportsEnum;
import com.aetna.asgwy.exception.AsgwyGlobalException;
import com.aetna.asgwy.repository.AsgwyLkupRepository;
import com.aetna.asgwy.repository.QuotesReportRepository;
import com.aetna.asgwy.repository.ScheduleReportsRepository;
import com.aetna.asgwy.repository.entity.ScheduleReports;
import com.aetna.asgwy.request.ReportsRequest;
import com.aetna.asgwy.specification.QuotesReportSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportsServiceImpl implements ReportsService {

	@Autowired
	private QuotesReportRepository quotesReportRepository;

	@Autowired
	private AsgwyLkupRepository asgwyLkupRepository;

	@Autowired
	private ScheduleReportsRepository scheduleReportsRepository;

	@Autowired
	private AzureFileService azureFileService;

	@Override
	public ResponseEntity<byte[]> downloadOrScheduleQuotesReport(String userId, ReportsRequest reportsRequest) {
		log.info("downloadOrScheduleQuotesReport() start...");
		int threshold = Integer.parseInt(asgwyLkupRepository.findValueByLabel(ReportsEnum.QUOTE_REPORT.name()));
		List<QuotesReport> quotesReportList = quotesReportRepository
				.findAll(QuotesReportSpecification.buildDynamicQuery(reportsRequest));

		if (quotesReportList.size() > 0 && quotesReportList.size() <= threshold) {
			log.info("CSV File generating....");
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					PrintWriter csvWriter = new PrintWriter(byteArrayOutputStream, true, StandardCharsets.UTF_8)) {

				String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"));
				String filename = "Quotes_Report_" + currentTime + ".csv";
				csvWriter.println(AsgwyConstants.QuotesReportHeader());

				quotesReportList.stream().map(this::convertToCsvRow).forEach(csvWriter::println);

				byte[] csvBytes = byteArrayOutputStream.toByteArray();

				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
				headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
				headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvBytes.length));
				return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(csvBytes);
			} catch (Exception e) {
				log.error("Exception at generating CSV file ::{}", e);
				throw new RuntimeException("Error generating CSV file", e);
			}
		} else if (quotesReportList.size() > threshold) {
			log.info("Report scheduning start....");
			try {
				ObjectMapper mapper = new ObjectMapper();
				String jsonRequest = mapper.writeValueAsString(reportsRequest);

				Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("America/New_York")));
				ScheduleReports reports = ScheduleReports.builder().reportType(ReportsEnum.QUOTE_REPORT.name())
						.requestDetails(jsonRequest).reportStatus(ReportsEnum.INPROGRESS.getValue()).requestedId(userId)
						.requestedDts(currentTime).updateUsrId(userId).updateDts(currentTime).build();
				scheduleReportsRepository.save(reports);
				log.info("Report scheduled....");
				throw new AsgwyGlobalException(200, "Report Got Scheduled");
			} catch (JsonProcessingException e) {
				log.error("Exeption in JsonProcessing ::{} " + e.getLocalizedMessage());
				throw new AsgwyGlobalException(200, "Exeption in JsonProcessing " + e.getLocalizedMessage());
			}
		} else {
			log.info("No Data found to gererate Report");
			throw new AsgwyGlobalException(200, "No Data found");
		}
	}

	private String convertToCsvRow(QuotesReport report) {
		return Arrays.asList(getStringValue(report.getAgentFirstName()), getStringValue(report.getAgentLastName()),
				getStringValue(report.getAgentNPN()), getStringValue(report.getFirmName()),
				getStringValue(report.getGaName()), getStringValue(report.getGroupNm()),
				getStringValue(report.getEmployerIdNbr()), getStringValue(report.getGroupZipCd()),
				getStringValue(report.getStateNm()), getStringValue(report.getGroupLocAddrLine1Txt()),
				getStringValue(report.getGroupLocAddrLine2Txt()), getStringValue(report.getGroupLocCityNm()),
				getStringValue(report.getCurrCarrierTypDesc()), getStringValue(report.getCurrMedCarrierNm()),
				getStringValue(report.getGrpMewaAssoc()), getStringValue(report.getAetnaPeoInd()),
				getStringValue(report.getEffectiveDt()), getStringValue(report.getContractPeriodMoNbr()),
				getStringValue(report.getEligibleEntrdCnt()), getStringValue(report.getTotAvgEmpCnt()),
				getStringValue(report.getSicCd()), getStringValue(report.getSicNm()),
				getStringValue(report.getDefBrokerFeeAmt()), getStringValue(report.getUnionEmpInd()),
				getStringValue(report.getUnionEmpCnt()), getStringValue(report.getParticipationCnt()),
				getStringValue(report.getEligibleDervdCnt()), getStringValue(report.getWaiverCnt()),
				getStringValue(report.getEligibleRetCnt()), getStringValue(report.getCobraEmpCnt()),
				getStringValue(report.getParticipationPct()), getStringValue(report.getFtEqvlntCnt()),
				getStringValue(report.getErisaCd()), getStringValue(report.getCurrTpaNm()),
				getStringValue(report.getAetnaSalesExe()), getStringValue(report.getTestGroupInd()),
				getStringValue(report.getConcessReqQuoteId()), getStringValue(report.getConcessReqStatusCd()),
				getStringValue(report.getConcessReqPct()), getStringValue(report.getConcessReqReasonTxt())

		).stream().map(this::escapeCsvField).collect(Collectors.joining(","));
	}

	private String getStringValue(Object obj) {
		// Handle null values
		return obj != null ? obj.toString() : "";
	}

	private String escapeCsvField(String field) {
		if (field == null) {
			return "";
		}
		if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
			// Wrap in quotes and escape existing quotes
			return "\"" + field.replace("\"", "\"\"") + "\"";
		}
		return field;
	}

	public List<QuotesReport> getFilteredQuotes(ReportsRequest reportsRequest) {
		return quotesReportRepository.findAll(QuotesReportSpecification.buildDynamicQuery(reportsRequest));
	}

	@Override
	public ResponseEntity<byte[]> downloadReportByName(String reportName) {

		try {
			byte[] bytesFromStorage = azureFileService.downloadReportByName(reportName);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + reportName)

					.contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(bytesFromStorage.length)
					.body(bytesFromStorage);

		} catch (IOException e) {
			log.error("failed to download file:: {}", e.getMessage());
			throw new AsgwyGlobalException(200, "Failed to Download file ::" + reportName);
		}
	}
}
