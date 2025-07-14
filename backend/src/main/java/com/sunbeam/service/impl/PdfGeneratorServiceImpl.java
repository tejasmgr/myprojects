package com.sunbeam.service.impl;

import com.sunbeam.dto.BirthCertificateFormData;
import com.sunbeam.dto.CasteCertificateFormData;
import com.sunbeam.dto.DomicileCertificateFormData;
import com.sunbeam.dto.IncomeCertificateFormData;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import com.sunbeam.service.PdfGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
	@Autowired
    private ObjectMapper objectMapper;
	
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

        // Applicant Details Table (common information)
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(80))
            .setMarginTop(20);

        User citizen = application.getApplicant();
        addTableRow(table, "Certificate Number:", application.getId().toString());
        addTableRow(table, "Full Name:", citizen.getFullName());
        addTableRow(table, "Aadhar Number:", citizen.getAadharNumber());
        addTableRow(table, "Certificate Type:", application.getDocumentType().name());
        addTableRow(table, "Issue Date:", LocalDate.now().format(DATE_FORMATTER));

        String formDataJson = application.getFormData();

        try {
            switch (application.getDocumentType()) {
                case INCOME: {
                    if (formDataJson != null && !formDataJson.isEmpty()) {
                        IncomeCertificateFormData incomeData = objectMapper.readValue(formDataJson, IncomeCertificateFormData.class);
                        addTableRow(table, "Father/Husband Name:", incomeData.getFatherOrHusbandName());
                        addTableRow(table, "Annual Income:", incomeData.getAnnualIncome());
                        addTableRow(table, "Occupation:", incomeData.getOccupation());
                        addTableRow(table, "Number of Family Members:", incomeData.getNumberOfFamilyMembers());
                        addTableRow(table, "Residential Address:", incomeData.getResidentialAddress());
                    }
                    break;
                }
                case DOMICILE: {
                    if (formDataJson != null && !formDataJson.isEmpty()) {
                        DomicileCertificateFormData domicileData = objectMapper.readValue(formDataJson, DomicileCertificateFormData.class);
                        addTableRow(table, "Father's Name:", domicileData.getFatherName());
                        addTableRow(table, "Date of Birth:", domicileData.getDateOfBirth());
                        addTableRow(table, "Place of Birth:", domicileData.getPlaceOfBirth());
                        addTableRow(table, "Residential Address:", domicileData.getResidentialAddress());
                        addTableRow(table, "Years of Residence:", domicileData.getYearsOfResidence());
                        addTableRow(table, "Reason for Domicile:", domicileData.getReasonForDomicile());
                    }
                    break;
                }
                case CASTE: {
                    if (formDataJson != null && !formDataJson.isEmpty()) {
                        CasteCertificateFormData casteData = objectMapper.readValue(formDataJson, CasteCertificateFormData.class);
                        addTableRow(table, "Father's Name:", casteData.getFatherName());
                        addTableRow(table, "Mother's Name:", casteData.getMotherName());
                        addTableRow(table, "Date of Birth:", casteData.getDateOfBirth());
                        addTableRow(table, "Place of Birth:", casteData.getPlaceOfBirth());
                        addTableRow(table, "Caste Name:", casteData.getCasteName());
                        addTableRow(table, "Residential Address:", casteData.getResidentialAddress());
                    }
                    break;
                }
                case BIRTH: {
                    if (formDataJson != null && !formDataJson.isEmpty()) {
                        BirthCertificateFormData birthData = objectMapper.readValue(formDataJson, BirthCertificateFormData.class);
                        addTableRow(table, "Date of Birth:", birthData.getDateOfBirth());
                        addTableRow(table, "Place of Birth:", birthData.getPlaceOfBirth());
                        addTableRow(table, "Gender:", birthData.getGender());
                        addTableRow(table, "Father's Full Name:", birthData.getFatherFullName());
                        addTableRow(table, "Mother's Full Name:", birthData.getMotherFullName());
                        addTableRow(table, "Hospital Name:", birthData.getHospitalName());
                        addTableRow(table, "Child's Full Name:", birthData.getChildFullName());
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unexpected document type: " + application.getDocumentType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            addTableRow(table, "Error:", "Could not read form data for " + application.getDocumentType());
        }

        document.add(table);

        // Approval Section
        document.add(new Paragraph("\n\nAPPROVED BY:").setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("\n\n\nSignature: ________________________").setTextAlignment(TextAlignment.RIGHT));

        document.close();
        return baos.toByteArray();
    }

	@Override
	public byte[] generateDailyReport(LocalDate date) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
		Document document = new Document(pdf);

		document.add(new Paragraph("DAILY APPLICATION REPORT").setTextAlignment(TextAlignment.CENTER).setBold()

				.setFontSize(16));

		document.add(new Paragraph("Date: " + date.format(DATE_FORMATTER)).setMarginBottom(20));

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