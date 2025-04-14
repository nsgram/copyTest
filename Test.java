package com.example.pdfsignature.controller;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RestController
@RequestMapping("/pdf")
public class PdfSignatureController {

    @GetMapping("/generate-pdf-with-signature")
    public ResponseEntity<byte[]> generatePdfWithSignature() throws IOException {
        InputStream templatePdf = new ClassPathResource("templates/sample.pdf").getInputStream();

        byte[] updatedPdf = addSignatureField(templatePdf);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("fillable-with-signature.pdf").build());

        return new ResponseEntity<>(updatedPdf, headers, HttpStatus.OK);
    }

    private byte[] addSignatureField(InputStream pdfInputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(pdfInputStream);
        PdfStamper stamper = new PdfStamper(reader, outputStream);

        PdfFormField signatureField = PdfFormField.createSignature(stamper.getWriter());
        signatureField.setFieldName("signature");
        signatureField.setWidget(new Rectangle(100, 100, 300, 150), PdfAnnotation.HIGHLIGHT_INVERT);
        signatureField.setFlags(PdfAnnotation.FLAGS_PRINT);
        signatureField.setPage(1);

        stamper.addAnnotation(signatureField, 1);
        stamper.close();
        reader.close();

        return outputStream.toByteArray();
    }
}
