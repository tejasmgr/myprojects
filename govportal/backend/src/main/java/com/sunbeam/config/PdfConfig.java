package com.sunbeam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdfConfig {

    @Value("${app.pdf.templates.path}")
    private String templatesPath;

    @Value("${app.pdf.output.dir}")
    private String outputDirectory;

    // Getters
    public String getTemplatesPath() {
        return templatesPath;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
}