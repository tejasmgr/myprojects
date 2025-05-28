package com.sunbeam.dto;

import lombok.Data;

@Data
public class BirthCertificateFormData {
    private String childFullName;
    private String dateOfBirth;
    private String placeOfBirth;
    private String gender;
    private String fatherFullName;
    private String motherFullName;
    private String hospitalName;
}