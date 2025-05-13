package com.sunbeam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationMetricsResponse {
    private long totalApplications;
    private double avgProcessingTimeMinutes;
    private Map<String, Long> statusDistribution;
}