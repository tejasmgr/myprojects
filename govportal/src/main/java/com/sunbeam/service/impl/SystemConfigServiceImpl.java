package com.sunbeam.service.impl;

import com.sunbeam.model.SystemConfig;
import com.sunbeam.repository.SystemConfigRepository;
import com.sunbeam.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository configRepository;

    @Override
    public Optional<String> getConfigValue(String key) {
        return configRepository.findById(key)
                .map(SystemConfig::getConfigValue);
    }

    @Override
    public void updateConfig(String key, String value) {
        SystemConfig config = configRepository.findById(key)
                .orElse(new SystemConfig(key));
        
        config.setConfigValue(value);
        configRepository.save(config);
    }

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = configRepository.findAll();
        
        return configs.stream()
            .collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                SystemConfig::getConfigValue
            ));
    }
}