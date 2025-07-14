package com.sunbeam.service;

import com.sunbeam.model.SystemConfig;

import java.util.Map;
import java.util.Optional;

public interface SystemConfigService {
    Optional<String> getConfigValue(String key);
    void updateConfig(String key, String value);
    Map<String, String> getAllConfigs();
}