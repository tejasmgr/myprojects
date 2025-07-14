package com.sunbeam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.sunbeam.model.DocumentApplication.ApplicationStatus;
import com.sunbeam.model.DocumentApplication.DocumentType;
import com.sunbeam.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentApplicationResponse {
    private Long id;
    private User citizen;
    private DocumentType documentType;
    private ApplicationStatus status;
    private List<String> documentPaths;
    private LocalDateTime submissionDate;
    private LocalDateTime resolvedDate;
    private String rejectionReason;
    private String currentDesk;
}