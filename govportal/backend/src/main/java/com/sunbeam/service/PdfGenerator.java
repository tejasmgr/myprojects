package com.sunbeam.service;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    
    public byte[] generateCertificate(DocumentApplication application) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);
        
        // Load fonts
        PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        // Add document header
        Paragraph header = new Paragraph("GOVERNMENT OF INDIA")
                .setFont(headerFont)
                .setFontSize(16)
                .setFontColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
                
        document.add(header);
        
        // Add document title based on type
        String docType = application.getDocumentType().toString().toUpperCase() + " CERTIFICATE";
        Paragraph title = new Paragraph(docType)
                .setFont(headerFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(title);
        
        // Add certificate number and date
        Paragraph details = new Paragraph()
                .setFont(normalFont)
                .setFontSize(10)
                .add("Certificate No: " + application.getId())
                .add("\nDate: " + application.getSubmissionDate().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(details);
        
        // Create applicant details table
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(80))
                .setMarginTop(30)
                .setMarginBottom(30);
        
        User citizen = application.getApplicant();
        addTableRow(table, "Full Name:", citizen.getFullName());
        addTableRow(table, "Father's Name:", citizen.getFatherName());
        addTableRow(table, "Address:", citizen.getAddress());
        addTableRow(table, "Date of Birth:", citizen.getDateOfBirth().format(DATE_FORMATTER));
        addTableRow(table, "Purpose:", application.getPurpose());
        
        document.add(table);
        
        // Add approval section
        Paragraph approval = new Paragraph()
                .setFont(normalFont)
                .setFontSize(10)
                .add("\n\nVerified and approved by:")
                .add("\n\n\nSignature: ________________________")
                .add("\nName: " + application.getApprovedBy())
                .add("\nDesignation: Government Verifier")
                .add("\nDate: " + application.getResolvedDate().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(approval);
        
        // Add watermark if needed
        // document.add(new Paragraph("OFFICIAL COPY").setRotationAngle(Math.PI/4).setFontColor(ColorConstants.LIGHT_GRAY));
        
        document.close();
        return baos.toByteArray();
    }
    
    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold());
        table.addCell(new Paragraph(value != null ? value : ""));
    }
}