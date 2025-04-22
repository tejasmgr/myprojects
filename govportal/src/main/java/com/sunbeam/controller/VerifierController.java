//package com.sunbeam.controller;
//import com.sunbeam.dto.request.ApplicationActionRequest;
//import com.sunbeam.dto.response.DocumentApplicationResponse;
//import com.sunbeam.dto.response.VerificationStatsResponse;
//import com.sunbeam.service.VerificationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/verifier")
//@RequiredArgsConstructor
//public class VerifierController {
//
//    private final VerificationService verificationService;
//
//    @GetMapping("/pending")
//    public ResponseEntity<List<DocumentApplicationResponse>> getPendingApplications() {
//        return ResponseEntity.ok(verificationService.getPendingApplications());
//    }
//
//    @PostMapping("/approve")
//    public ResponseEntity<DocumentApplicationResponse> approveApplication(
//            @Valid @RequestBody ApplicationActionRequest request) {
//        return ResponseEntity.ok(verificationService.approveApplication(request));
//    }
//
//    @PostMapping("/reject")
//    public ResponseEntity<DocumentApplicationResponse> rejectApplication(
//            @Valid @RequestBody ApplicationActionRequest request) {
//        return ResponseEntity.ok(verificationService.rejectApplication(request));
//    }
//
//    @PostMapping("/move-desk")
//    public ResponseEntity<DocumentApplicationResponse> moveToNextDesk(
//            @Valid @RequestBody ApplicationActionRequest request) {
//        return ResponseEntity.ok(verificationService.moveToNextDesk(request));
//    }
//    
//    @PostMapping("/{applicationId}/approve")
//    public ResponseEntity<DocumentApplicationResponse> approveApplication(
//            @PathVariable Long applicationId,
//            @RequestParam String remarks) {
//        return ResponseEntity.ok(
//            verificationService.approveApplication(applicationId, remarks)
//        );
//    }
//
//    @GetMapping("/stats")
//    public ResponseEntity<VerificationStatsResponse> getVerificationStats() {
//        return ResponseEntity.ok(verificationService.getVerificationStats());
//    }
//}