import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        List<List<String>> data = getData();

        // Generate CSV as byte array
        byte[] csvBytes = generateCsv(data);

        // Build response with headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private List<List<String>> getData() {
        // Example data rows
        return Arrays.asList(
                Arrays.asList("ID", "Name", "Description"),
                Arrays.asList("1", "Alice", "A sample description with, a comma."),
                Arrays.asList("2", "Bob", "Another \"quoted\" description."),
                Arrays.asList("3", "Charlie", "Simple text")
        );
    }

    private byte[] generateCsv(List<List<String>> data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintWriter csvWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {

            data.forEach(row -> csvWriter.println(escapeCsvRow(row)));
            csvWriter.flush();

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }
    }

    private String escapeCsvRow(List<String> row) {
        return row.stream()
                .map(this::escapeCsvField)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private String escapeCsvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
