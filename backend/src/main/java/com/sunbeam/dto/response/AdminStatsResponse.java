package com.sunbeam.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalCitizens;
    private long activeVerifiers;
    private long pendingApplications;
    private long approvedApplications;
    private long blockedAccounts;
}