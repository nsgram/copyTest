package com.example.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import com.example.dto.PdfFormGenerationRequest;
import com.example.dto.PdfFormGenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Service
public class PdfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfService.class);

    private static final String TEMPLATE_PATH = "templates/Employer_Application.pdf"; // Ensure this path exists
    private static final String OUTPUT_DIR = "generated_pdfs/";

    public PdfFormGenerationResponse generatePdf(PdfFormGenerationRequest request) {
        LOGGER.info("Generating PDF with provided form values");

        try {
            // Create output directory if not exists
            Files.createDirectories(Paths.get(OUTPUT_DIR));

            String fileName = "Modified_PDF_" + System.currentTimeMillis() + ".pdf";
            String filePath = OUTPUT_DIR + fileName;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream templateStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);

            if (templateStream == null) {
                LOGGER.error("Template PDF not found in resources.");
                throw new FileNotFoundException("Template PDF not found.");
            }

            PdfReader reader = new PdfReader(templateStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            AcroFields formFields = stamper.getAcroFields();

            // 🔥 Ensure form fields are editable
            stamper.setFormFlattening(false);

            // Populate form fields dynamically
            for (Map.Entry<String, String> entry : request.getFieldValues().entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                if (formFields.getField(fieldName) != null) {
                    formFields.setField(fieldName, fieldValue);
                    LOGGER.info("Filled field '{}' with value '{}'", fieldName, fieldValue);
                } else {
                    LOGGER.warn("Field '{}' not found in the PDF template.", fieldName);
                }
            }

            stamper.close();
            reader.close();

            // Save the modified PDF
            Files.write(Paths.get(filePath), outputStream.toByteArray());

            // Encode the PDF to Base64
            String base64Pdf = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            return PdfFormGenerationResponse.builder()
                    .fileName(fileName)
                    .base64Pdf(base64Pdf)
                    .status(HttpStatus.CREATED.name())
                    .build();
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error generating PDF: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
