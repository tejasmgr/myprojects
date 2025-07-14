// src/utils/documentSchemas.js
// This file defines the dynamic form fields required for each document type.

export const documentSchemas = {
    // Schema for INCOME Certificate
    INCOME: [
        { name: 'applicantFullName', label: 'Applicant Full Name', type: 'text', required: true },
        { name: 'fatherOrHusbandName', label: 'Father/Husband Name', type: 'text', required: true },
        { name: 'annualIncome', label: 'Annual Income (INR)', type: 'number', required: true },
        { name: 'occupation', label: 'Occupation', type: 'text', required: true },
        { name: 'numberOfFamilyMembers', label: 'Number of Family Members', type: 'number', required: true },
        { name: 'residentialAddress', label: 'Residential Address', type: 'textarea', required: true },
    ],
    // Schema for CASTE Certificate
    CASTE: [
        { name: 'applicantFullName', label: 'Applicant Full Name', type: 'text', required: true },
        { name: 'fatherName', label: 'Father Name', type: 'text', required: true },
        { name: 'motherName', label: 'Mother Name', type: 'text', required: true },
        { name: 'dateOfBirth', label: 'Date of Birth', type: 'date', required: true },
        { name: 'placeOfBirth', label: 'Place of Birth', type: 'text', required: true },
        { name: 'casteName', label: 'Caste Name', type: 'text', required: true },
        { name: 'subCaste', label: 'Sub-Caste (if applicable)', type: 'text', required: false },
        { name: 'residentialAddress', label: 'Residential Address', type: 'textarea', required: true },
    ],
    // Schema for DOMICILE Certificate
    DOMICILE: [
        { name: 'applicantFullName', label: 'Applicant Full Name', type: 'text', required: true },
        { name: 'fatherName', label: 'Father Name', type: 'text', required: true },
        { name: 'dateOfBirth', label: 'Date of Birth', type: 'date', required: true },
        { name: 'placeOfBirth', label: 'Place of Birth', type: 'text', required: true },
        { name: 'residentialAddress', label: 'Residential Address', type: 'textarea', required: true },
        { name: 'yearsOfResidence', label: 'Years of Residence in State', type: 'number', required: true },
        { name: 'reasonForDomicile', label: 'Reason for Domicile', type: 'textarea', required: true },
    ],
    // Schema for BIRTH Certificate
    BIRTH: [
        { name: 'childFullName', label: 'Child Full Name', type: 'text', required: true },
        { name: 'dateOfBirth', label: 'Date of Birth', type: 'date', required: true },
        { name: 'placeOfBirth', label: 'Place of Birth', type: 'text', required: true },
        { name: 'gender', label: 'Gender', type: 'select', options: ['MALE', 'FEMALE', 'OTHER'], required: true },
        { name: 'fatherFullName', label: 'Father Full Name', type: 'text', required: true },
        { name: 'motherFullName', label: 'Mother Full Name', type: 'text', required: true },
        { name: 'hospitalName', label: 'Hospital Name (if applicable)', type: 'text', required: false },
    ],
};
