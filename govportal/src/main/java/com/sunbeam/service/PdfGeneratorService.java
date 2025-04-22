package com.sunbeam.service;

import com.sunbeam.model.DocumentApplication;
import java.io.IOException;
import java.time.LocalDate;

public interface PdfGeneratorService {
    byte[] generateCertificate(DocumentApplication application) throws IOException;
    byte[] generateDailyReport(LocalDate date) throws IOException;
}