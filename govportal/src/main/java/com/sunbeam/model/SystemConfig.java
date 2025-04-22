package com.sunbeam.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SystemConfig {
    public SystemConfig(String key) {
		this.configKey = key;
	}

	@Id
    private String configKey;
    
    @Lob
    private String configValue;
    
    public enum Key {
        ALLOWED_FILE_TYPES,
        MAX_FILE_SIZE_MB,
        DOCUMENT_TYPES
    }
}