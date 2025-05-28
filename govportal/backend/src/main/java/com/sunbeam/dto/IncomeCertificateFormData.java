package com.sunbeam.dto;

import lombok.Data;

@Data
public class IncomeCertificateFormData {
    private String applicantFullName;
    private String fatherOrHusbandName;
    private String annualIncome;
    private String occupation;
    private String numberOfFamilyMembers;
    private String residentialAddress;
}