package com.sunbeam.dto;

import lombok.Data;

@Data
public class CasteCertificateFormData {
    private String applicantFullName;
    private String fatherName;
    private String motherName;
    private String dateOfBirth;
    private String placeOfBirth;
    private String casteName;
    private String residentialAddress;
}