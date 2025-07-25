package com.sunbeam.config;


import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OciConfig {

	@Value("${oci.config.file}")
    private String configFilePath;

    @Value("${oci.config.profile}")
    private String configProfile;

    @Bean
    public ObjectStorageClient objectStorageClient() throws Exception {
    	String normalizedPath = Paths.get(configFilePath).normalize().toString();
        ConfigFileAuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(normalizedPath, configProfile);
        return ObjectStorageClient.builder().build(provider);
    }
}