package com.sunbeam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatsResponse {
    private long totalAssigned;
    private long pending;
    private long approved;
    private long rejected;
    private double approvalRate;
}