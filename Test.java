import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        // Mock data
        List<QuoteReport> quoteReports = getQuoteReports();

        // Generate CSV as a byte array
        byte[] csvBytes = generateCsv(quoteReports);

        // Prepare response with appropriate headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quote_reports.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private List<QuoteReport> getQuoteReports() {
        // Example data
        return Arrays.asList(
                new QuoteReport(new BigInteger("1001"), "Alice", "Description with, comma."),
                new QuoteReport(new BigInteger("1002"), "Bob", "Text with \"quote\"."),
                new QuoteReport(null, "Charlie", "Multiline\ntext example")
        );
    }

    private byte[] generateCsv(List<QuoteReport> quoteReports) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintWriter csvWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {

            // Write CSV header
            csvWriter.println("Agent ID,Agent Name,Agent NPN");

            // Write each row with proper escaping
            quoteReports.stream()
                    .map(this::convertToCsvRow)
                    .forEach(csvWriter::println);

            csvWriter.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }
    }

    private String convertToCsvRow(QuoteReport report) {
        return Arrays.asList(
                getStringValue(report.getAgentId()),
                getStringValue(report.getAgentName()),
                getStringValue(report.getAgentNpn())
        ).stream()
         .map(this::escapeCsvField)
         .collect(Collectors.joining(","));
    }

    private String getStringValue(Object obj) {
        // Handle null values
        return obj != null ? obj.toString() : "";
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return ""; // Null field converted to empty string
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Wrap in quotes and escape existing quotes
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field; // Return as is if no escaping is needed
    }
}
