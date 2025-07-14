package com.sunbeam.dto;

import lombok.Data;

@Data
public class DomicileCertificateFormData {
    private String applicantFullName;
    private String fatherName;
    private String dateOfBirth;
    private String placeOfBirth;
    private String residentialAddress;
    private String yearsOfResidence;
    private String reasonForDomicile;
}