package com.sunbeam.controller;

import com.sunbeam.dto.request.SystemConfigUpdateRequest;
import com.sunbeam.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService configService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @PutMapping
    public ResponseEntity<String> updateConfig(
            @Valid @RequestBody SystemConfigUpdateRequest request) {
        configService.updateConfig(request.getKey(), request.getValue());
        return ResponseEntity.ok("Config updated successfully");
    }
}