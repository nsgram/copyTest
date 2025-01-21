Optimized ReportsServiceImpl Class

Here’s the optimized version of your class with potential SonarQube fixes, improved security, and no code smells:

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

    private final QuotesReportRepository quotesReportRepository;
    private final AsgwyLkupRepository asgwyLkupRepository;
    private final ScheduleReportsRepository scheduleReportsRepository;
    private final AzureFileService azureFileService;

    @Autowired
    public ReportsServiceImpl(QuotesReportRepository quotesReportRepository, 
                              AsgwyLkupRepository asgwyLkupRepository,
                              ScheduleReportsRepository scheduleReportsRepository, 
                              AzureFileService azureFileService) {
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
            return generateCsvResponse(quotesReportList);
        } else {
            return scheduleReport(userId, reportsRequest);
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

    private ResponseEntity<byte[]> generateCsvResponse(List<QuotesReport> quotesReportList) {
        log.info("Generating CSV file...");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintWriter csvWriter = new PrintWriter(byteArrayOutputStream, true, StandardCharsets.UTF_8)) {

            String filename = "Quotes_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")) + ".csv";
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
            ScheduleReports report = new ScheduleReports();
            report.setReportType(ReportsEnum.QUOTE_REPORT.name());
            report.setRequestDetails(jsonRequest);
            report.setReportStatus(ReportsEnum.INPROGRESS.getValue());
            report.setRequestedId(userId);
            report.setRequestedDts(currentTime);
            report.setUpdateUsrId(userId);
            report.setUpdateDts(currentTime);

            scheduleReportsRepository.save(report);
            return ResponseEntity.ok("Report Scheduled Successfully".getBytes(StandardCharsets.UTF_8));

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON request", e);
            throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error scheduling report");
        }
    }

    private String convertToCsvRow(QuotesReport report) {
        return Arrays.stream(new String[]{
                safeString(report.getAgentFirstName()), safeString(report.getAgentLastName()),
                // Add all fields here...
                safeString(report.getConcessReqReasonTxt())
        }).map(this::escapeCsvField).collect(Collectors.joining(","));
    }

    private String safeString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    @Override
    public ResponseEntity<byte[]> downloadReportByName(String reportName) {
        try {
            byte[] reportData = azureFileService.downloadReportByName(reportName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(reportData.length)
                    .body(reportData);

        } catch (IOException e) {
            log.error("Error downloading file {}", reportName, e);
            throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to download file");
        }
    }
}

JUnit Test Cases

You can use the following JUnit test cases. Mock the dependencies and test all edge cases:

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class ReportsServiceImplTest {

    @InjectMocks
    private ReportsServiceImpl reportsService;

    @Mock
    private QuotesReportRepository quotesReportRepository;

    @Mock
    private AsgwyLkupRepository asgwyLkupRepository;

    @Mock
    private ScheduleReportsRepository scheduleReportsRepository;

    @Mock
    private AzureFileService azureFileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDownloadOrScheduleQuotesReport_NoData() {
        // Mock behavior
        when(quotesReportRepository.findAll(any())).thenReturn(Collections.emptyList());

        // Assert exception
        assertThrows(AsgwyGlobalException.class, () ->
                reportsService.downloadOrScheduleQuotesReport("user1", new ReportsRequest()));
    }

    @Test
    void testDownloadReportByName_FileFound() throws Exception {
        // Mock behavior
        byte[] mockData = "Sample Data".getBytes();
        when(azureFileService.downloadReportByName("test.csv")).thenReturn(mockData);

        ResponseEntity<byte[]> response = reportsService.downloadReportByName("test.csv");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(mockData, response.getBody());
    }

    @Test
    void testDownloadReportByName_FileNotFound() throws Exception {
        // Mock behavior
        when(azureFileService.downloadReportByName(any())).thenThrow(new IOException("File not found"));

        // Assert exception
        assertThrows(AsgwyGlobalException.class, () ->
                reportsService.downloadReportByName("nonexistent.csv"));
    }

    // Add more tests for CSV generation, scheduling, and exception handling...
}

The above example illustrates both mocked behaviors and edge cases in the ReportsServiceImpl.
