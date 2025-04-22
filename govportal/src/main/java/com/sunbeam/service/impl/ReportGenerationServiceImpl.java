package com.sunbeam.service.impl;

import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.service.PdfGeneratorService;
import com.sunbeam.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final DocumentApplicationRepository appRepository;
    private final PdfGeneratorService pdfGenerator;

    @Override
    public byte[] generateDailyReport(LocalDate date) {
        // Implement PDF report generation
        try {
			return pdfGenerator.generateDailyReport(date);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    @Override
    public Map<String, Object> getVerificationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalApplications", appRepository.count());
        metrics.put("avgProcessingTime", appRepository.getAverageProcessingTime());
        return metrics;
    }
}