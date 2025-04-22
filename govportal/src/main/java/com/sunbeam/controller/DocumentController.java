package com.sunbeam.controller;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentApplicationResponse> submitApplication(
            @Valid @ModelAttribute DocumentApplicationRequest request) {
        return ResponseEntity.ok(documentService.submitApplication(request));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<DocumentApplicationResponse>> getUserApplications() {
        return ResponseEntity.ok(documentService.getUserApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getApplicationById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        byte[] pdfBytes = documentService.generateCertificatePdf(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"certificate.pdf\"")
                .body(pdfBytes);
    }
}