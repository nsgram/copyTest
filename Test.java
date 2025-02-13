To convert the CSV file to an Excel file with the following formatting:
	•	Bold headers
	•	Green background for headers
	•	Borders for each cell

You can use Apache POI to create the Excel file in Spring Boot. Below is the updated ConvertToExcel utility class that takes your CSV data and converts it to an Excel file with the specified formatting:

Steps
	1.	Read the CSV data from convertListToBytes method.
	2.	Use Apache POI to write it to an Excel file with formatting.
	3.	Apply bold text and a green background to headers.
	4.	Apply borders to all cells.

Maven Dependency for Apache POI

If you haven’t already, add Apache POI dependencies to your pom.xml:

<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>

ConvertToExcel Utility

package com.aetna.asgwy.reporting.batch.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.aetna.asgwy.reporting.batch.constants.ReportHeaderConstants;
import com.aetna.asgwy.reporting.batch.repositories.entity.QuotesReportEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConvertToExcel {

    public byte[] convertListToExcel(List<QuotesReportEntity> quotesReportEntityList) {
        if (quotesReportEntityList.isEmpty()) {
            return new byte[0];
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Quotes Report");
            writeHeader(workbook, sheet);
            writeData(workbook, sheet, quotesReportEntityList);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel file", e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = ReportHeaderConstants.quotesReportHeader();

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
    }

    private void writeData(Workbook workbook, Sheet sheet, List<QuotesReportEntity> quotesReportEntityList) {
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        int rowIndex = 1;
        for (QuotesReportEntity report : quotesReportEntityList) {
            Row row = sheet.createRow(rowIndex++);
            String[] data = convertToCsvRowArray(report);

            for (int i = 0; i < data.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(data[i]);
                cell.setCellStyle(borderStyle);
            }
        }
    }

    private String[] convertToCsvRowArray(QuotesReportEntity report) {
        return new String[]{
                report.getGroupNm(), report.getEmployerIdNbr(), safeString(report.getTestGroupInd()),
                safeString(report.getQuoteId()), report.getStatusDesc(),
                safeString(report.getGroupZipCd()), report.getGroupStateCd(),
                safeString(report.getEligibleEntrdCnt()), report.getUnionEmpInd(),
                safeString(report.getUnionEmpCnt()), report.getProductCd(),
                safeString(report.getDefBrokerFeeAmt()), safeString(report.getParticipationCnt())
        };
    }

    private String safeString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}

How It Works
	1.	convertListToExcel: Creates an Excel workbook and writes data.
	2.	writeHeader: Formats the header row with bold text and a green background.
	3.	writeData: Writes the actual data and applies borders to all cells.

Usage Example

Call this method instead of convertListToBytes:

byte[] excelBytes = convertToExcel.convertListToExcel(quotesReportEntityList);

Final Output

✔ Headers will be bold and have a green background.
✔ All cells will have borders.
✔ The file will be in Excel format (.xlsx).

Let me know if you need any modifications!
