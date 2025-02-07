To save the generated PDF file after filling the form, I’ll modify the service to store it in a specific location (e.g., src/main/resources/generated/).

Updated Implementation: Save the Generated PDF

Modify the PdfService to save the file in addition to returning the Base64 response.

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
        // Ensure the output directory exists
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // Generate unique filename
        String fileName = "Employer_Application_" + System.currentTimeMillis() + ".pdf";
        String filePath = OUTPUT_DIR + fileName;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Read the template
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

Updated API Response

Modify the controller to return both Base64 string and saved file path.

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
            return ResponseEntity.internalServerError().body("{\"error\": \"Error generating PDF: " + e.getMessage() + "\"}");
        }
    }
}

Generated Files Location

All PDFs will be saved in:

src/main/resources/generated/

Example output file:

src/main/resources/generated/Employer_Application_1707311843.pdf

Sample API Response

{
  "message": "PDF Generated Successfully",
  "base64Pdf": "JVBERi0xLj..."
}

You can decode base64Pdf or download the saved file.

Next Steps

✅ Upload the fillable PDF template to verify field names
✅ Modify storage location if needed

Let me know if you need changes!
