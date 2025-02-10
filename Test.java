package com.example.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Service
public class PdfService {

    // The PDF template should be placed under src/main/resources/templates
    private static final String TEMPLATE_PATH = "templates/Employer_Application.pdf";
    // Output directory for generated PDFs
    private static final String OUTPUT_DIR = "generated_pdfs/";

    /**
     * Populates matching fields in the PDF template with provided values and returns a new editable PDF as a Base64 string.
     *
     * @param fieldValues Map of field names to values
     * @return Base64 encoded PDF string
     */
    public String generateEditablePdf(Map<String, String> fieldValues) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String fileName = "Modified_PDF_" + System.currentTimeMillis() + ".pdf";
            String filePath = OUTPUT_DIR + fileName;

            InputStream templateStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);
            if (templateStream == null) {
                throw new FileNotFoundException("Template PDF not found.");
            }

            PdfReader reader = new PdfReader(templateStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            AcroFields formFields = stamper.getAcroFields();

            // Populate matching fields from the request
            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                if (formFields.getField(fieldName) != null) {
                    formFields.setField(fieldName, fieldValue);
                }
            }

            // Do not flatten the form to keep it editable
            stamper.setFormFlattening(false);
            stamper.close();
            reader.close();

            // Save the modified PDF file to disk
            Files.write(Paths.get(filePath), outputStream.toByteArray());

            // Return the PDF as a Base64-encoded string
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
