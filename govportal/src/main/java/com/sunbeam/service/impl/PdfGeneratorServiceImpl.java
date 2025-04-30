package com.sunbeam.service.impl;

import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import com.sunbeam.service.PdfGeneratorService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.itextpdf.layout.properties.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Override
    public byte[] generateCertificate(DocumentApplication application) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);

        // Fonts
        PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header
        document.add(new Paragraph("OFFICIAL GOVERNMENT CERTIFICATE")
            .setFont(headerFont)
            .setFontSize(18)
            .setFontColor(ColorConstants.BLUE)
            .setTextAlignment(TextAlignment.CENTER));

        // Applicant Details Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(80))
            .setMarginTop(20);

        User citizen = application.getApplicant();
        addTableRow(table, "Certificate Number:", application.getId().toString());
        addTableRow(table, "Full Name:", citizen.getFullName());
        addTableRow(table, "Aadhar Number:", citizen.getAadharNumber());
        addTableRow(table, "Certificate Type:", application.getDocumentType().name());
        addTableRow(table, "Issue Date:", LocalDate.now().format(DATE_FORMATTER));

        document.add(table);

        // Approval Section
        document.add(new Paragraph("\n\nAPPROVED BY:")
            .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("\n\n\nSignature: ________________________")
            .setTextAlignment(TextAlignment.RIGHT));

        document.close();
        return baos.toByteArray();
    }

    @Override
    public byte[] generateDailyReport(LocalDate date) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);

        document.add(new Paragraph("DAILY APPLICATION REPORT")
            .setTextAlignment(TextAlignment.CENTER)
            .setBold()
            
            .setFontSize(16));

        document.add(new Paragraph("Date: " + date.format(DATE_FORMATTER))
            .setMarginBottom(20));

        // Add report content here
        document.add(new Paragraph("Total applications processed today: [DATA]"));
        
        document.close();
        return baos.toByteArray();
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold());
        table.addCell(new Paragraph(value != null ? value : "N/A"));
    }
}