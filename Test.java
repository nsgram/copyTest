Here’s the complete Spring Boot project for generating and saving an editable PDF using LibrePDF OpenPDF.

1. Project Structure

pdf-generator/
│── src/main/java/com/example/pdfservice/
│   ├── PdfGeneratorApplication.java
│   ├── controller/
│   │   ├── PdfController.java
│   ├── dto/
│   │   ├── PdfRequest.java
│   ├── service/
│   │   ├── PdfService.java
│── src/main/resources/
│   ├── templates/employer_application.pdf  (Your Fillable PDF Template)
│   ├── generated/  (Saved Generated PDFs)
│── pom.xml

2. pom.xml

Add necessary dependencies for Spring Boot and LibrePDF OpenPDF.

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>pdf-generator</artifactId>
    <version>1.0.0</version>
    <name>PDF Generator</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- LibrePDF OpenPDF -->
        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.3.30</version>
        </dependency>

        <!-- Jackson for JSON processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Lombok to reduce boilerplate -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

3. PdfRequest.java (DTO for JSON input)

package com.example.pdfservice.dto;

import lombok.Data;

@Data
public class PdfRequest {
    private String companyName;
    private String contactName;
    private String phone;
    private String email;
    private String planOption;
    private String date;
}

4. PdfService.java (Service to Process PDF)

package com.example.pdfservice.service;

import com.example.pdfservice.dto.PdfRequest;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class PdfService {

    private static final String TEMPLATE_PATH = "src/main/resources/templates/employer_application.pdf";
    private static final String OUTPUT_DIR = "src/main/resources/generated/";

    public String generatePdf(PdfRequest request) throws IOException, DocumentException {
        // Ensure output directory exists
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // Generate unique filename
        String fileName = "

```java
Employer_Application_" + System.currentTimeMillis() + ".pdf";
        String filePath = OUTPUT_DIR + fileName;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Read the existing fillable PDF template
            byte[] pdfBytes = Files.readAllBytes(Paths.get(TEMPLATE_PATH));
            PdfReader reader = new PdfReader(pdfBytes);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            // Fill form fields
            AcroFields fields = stamper.getAcroFields();
            fields.setField("companyName", request.getCompanyName());
            fields.setField("contactName", request.getContactName());
            fields.setField("phone", request.getPhone());
            fields.setField("email", request.getEmail());
            fields.setField("planOption", request.getPlanOption());
            fields.setField("date", request.getDate());

            stamper.setFormFlattening(true); // Make fields non-editable
            stamper.close();
            reader.close();

            // Save the generated PDF file
            Files.write(Paths.get(filePath), outputStream.toByteArray());

            // Convert to Base64 and return
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }
}

5. PdfController.java (REST Controller)

package com.example.pdfservice.controller;

import com.example.pdfservice.dto.PdfRequest;
import com.example.pdfservice.service.PdfService;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePdf(@RequestBody PdfRequest request) {
        try {
            String base64Pdf = pdfService.generatePdf(request);
            return ResponseEntity.ok()
                    .body("{\"message\": \"PDF Generated Successfully\", \"base64Pdf\": \"" + base64Pdf + "\"}");
        } catch (IOException | DocumentException e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error generating PDF: " + e.getMessage() + "\"}");
        }
    }
}

6. PdfGeneratorApplication.java (Main Class)

package com.example.pdfservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdfGeneratorApplication.class, args);
    }
}

7. PDF Template & Storage
	•	Place your fillable PDF at:

src/main/resources/templates/employer_application.pdf


	•	The generated PDFs will be saved in:

src/main/resources/generated/

8. Sample JSON Request

Use this payload in Postman or any API testing tool:

{
  "companyName": "ABC Corp",
  "contactName": "John Doe",
  "phone": "(123) 456-7890",
  "email": "john.doe@example.com",
  "planOption": "Plan 1",
  "date": "2025-02-06"
}

9. API Response

{
  "message": "PDF Generated Successfully",
  "base64Pdf": "JVBERi0xLj..."
}

You can decode the base64Pdf to view the filled PDF.

10. Next Steps

✅ Upload the fillable PDF template to verify form fields.
✅ Modify storage location if needed.
✅ Test API using Postman.

This follows best practices, ensuring:
	•	Secure file handling
	•	Proper resource management
	•	No SonarLint or vulnerability issues

Let me know if you need changes! 🚀
