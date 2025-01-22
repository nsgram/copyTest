package com.example.reports.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.reports.dto.ReportsRequest;
import com.example.reports.entity.QuotesReport;
import com.example.reports.entity.ScheduleReports;
import com.example.reports.exception.AsgwyGlobalException;
import com.example.reports.repository.AsgwyLkupRepository;
import com.example.reports.repository.QuotesReportRepository;
import com.example.reports.repository.ScheduleReportsRepository;
import com.example.reports.service.impl.ReportsServiceImpl;
import com.example.reports.util.ReportsEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class ReportsServiceImplTest {

    @Mock
    private QuotesReportRepository quotesReportRepository;

    @Mock
    private AsgwyLkupRepository asgwyLkupRepository;

    @Mock
    private ScheduleReportsRepository scheduleReportsRepository;

    @Mock
    private AzureFileService azureFileService;

    @InjectMocks
    private ReportsServiceImpl reportsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDownloadOrScheduleQuotesReport_EmptyResult() {
        // Arrange
        ReportsRequest request = new ReportsRequest();
        when(quotesReportRepository.findAll(any())).thenReturn(Collections.emptyList());

        // Act & Assert
        Exception exception = assertThrows(AsgwyGlobalException.class, 
            () -> reportsService.downloadOrScheduleQuotesReport("user1", request));
        assertEquals("No Data found", exception.getMessage());
    }

    @Test
    void testDownloadOrScheduleQuotesReport_GenerateCsv() {
        // Arrange
        ReportsRequest request = new ReportsRequest();
        List<QuotesReport> reports = List.of(new QuotesReport());
        when(quotesReportRepository.findAll(any())).thenReturn(reports);
        when(asgwyLkupRepository.findValueByLabel(anyString())).thenReturn("10");

        // Act
        ResponseEntity<byte[]> response = reportsService.downloadOrScheduleQuotesReport("user1", request);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("text/csv; charset=UTF-8", response.getHeaders().getContentType().toString());
    }

    @Test
    void testDownloadOrScheduleQuotesReport_ScheduleReport() {
        // Arrange
        ReportsRequest request = new ReportsRequest();
        List<QuotesReport> reports = List.of(new QuotesReport(), new QuotesReport(), new QuotesReport());
        when(quotesReportRepository.findAll(any())).thenReturn(reports);
        when(asgwyLkupRepository.findValueByLabel(anyString())).thenReturn("2");

        // Act & Assert
        Exception exception = assertThrows(AsgwyGlobalException.class, 
            () -> reportsService.downloadOrScheduleQuotesReport("user1", request));
        assertEquals("Report Got Scheduled", exception.getMessage());
    }

    @Test
    void testDownloadReportByName_Success() throws IOException {
        // Arrange
        String fileName = "test.csv";
        byte[] mockData = "dummy content".getBytes();
        when(azureFileService.downloadReportByName(fileName)).thenReturn(mockData);

        // Act
        ResponseEntity<byte[]> response = reportsService.downloadReportByName(fileName);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("attachment; filename=" + fileName, response.getHeaders().getContentDisposition().toString());
    }

    @Test
    void testDownloadReportByName_Failure() throws IOException {
        // Arrange
        String fileName = "nonexistent.csv";
        when(azureFileService.downloadReportByName(fileName)).thenThrow(IOException.class);

        // Act & Assert
        Exception exception = assertThrows(AsgwyGlobalException.class, 
            () -> reportsService.downloadReportByName(fileName));
        assertEquals("Failed to download file", exception.getMessage());
    }

    @Test
    void testGetThreshold_ValidValue() {
        // Arrange
        String reportType = ReportsEnum.QUOTE_REPORT.name();
        when(asgwyLkupRepository.findValueByLabel(reportType)).thenReturn("100");

        // Act
        int threshold = reportsService.getThreshold(reportType);

        // Assert
        assertEquals(100, threshold);
    }

    @Test
    void testGetThreshold_InvalidValue() {
        // Arrange
        String reportType = ReportsEnum.QUOTE_REPORT.name();
        when(asgwyLkupRepository.findValueByLabel(reportType)).thenReturn("invalid");

        // Act & Assert
        Exception exception = assertThrows(AsgwyGlobalException.class, 
            () -> reportsService.getThreshold(reportType));
        assertEquals("Invalid threshold value", exception.getMessage());
    }
}



import org.springframework.data.jpa.domain.Specification;

...

@Test
void downloadOrScheduleQuotesReport_NoDataFound_WithSpecification() {
    // Mock an empty specification query result
    Specification<QuotesReport> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    when(quotesReportRepository.findAll(eq(specification))).thenReturn(Collections.emptyList());

    // Simulate the ReportsRequest to build the specification
    Exception exception = assertThrows(AsgwyGlobalException.class, 
        () -> reportsService.downloadOrScheduleQuotesReport("userId", reportsRequest));

    assertEquals(HttpStatus.NO_CONTENT.value(), ((AsgwyGlobalException) exception).getStatusCode());
    assertEquals("No Data found", exception.getMessage());

    verify(quotesReportRepository, times(1)).findAll(any());
}

@Test
void downloadOrScheduleQuotesReport_ThresholdExceeded_WithSpecification() {
    // Mock a result where threshold is exceeded
    int threshold = 1;
    Specification<QuotesReport> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    List<QuotesReport> quotesReports = List.of(new QuotesReport(), new QuotesReport());
    when(quotesReportRepository.findAll(eq(specification))).thenReturn(quotesReports);
    when(asgwyLkupRepository.findValueByLabel(anyString())).thenReturn(String.valueOf(threshold));

    Exception exception = assertThrows(AsgwyGlobalException.class, 
        () -> reportsService.downloadOrScheduleQuotesReport("userId", reportsRequest));

    assertEquals(HttpStatus.OK.value(), ((AsgwyGlobalException) exception).getStatusCode());
    assertEquals("Report Got Scheduled", exception.getMessage());

    verify(scheduleReportsRepository, times(1)).save(any());
}

@Test
void downloadOrScheduleQuotesReport_ThresholdNotExceeded_WithSpecification() {
    // Mock a result where threshold is not exceeded
    int threshold = 5;
    Specification<QuotesReport> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    List<QuotesReport> quotesReports = List.of(new QuotesReport(), new QuotesReport());
    when(quotesReportRepository.findAll(eq(specification))).thenReturn(quotesReports);
    when(asgwyLkupRepository.findValueByLabel(anyString())).thenReturn(String.valueOf(threshold));

    ResponseEntity<byte[]> response = reportsService.downloadOrScheduleQuotesReport("userId", reportsRequest);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(new String(response.getBody()).contains("Quotes_Report"));

    verify(quotesReportRepository, times(1)).findAll(any());
}
