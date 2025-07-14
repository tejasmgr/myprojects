package com.sunbeam.service.impl;

import com.sunbeam.exception.FileStorageException;
import com.sunbeam.exception.InvalidFileTypeException;
import com.sunbeam.service.FileStorageService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.allowed-file-types}")
    private List<String> allowedTypes;

    @Value("${app.max-file-size}")
    private long maxFileSize;

    private final Tika tika = new Tika();

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        // Validate file size
        if(file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum limit");
        }

        // Validate file type
        String mimeType = tika.detect(file.getInputStream());
        if(!allowedTypes.contains(mimeType)) {
            throw new InvalidFileTypeException("Invalid file type: " + mimeType);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        return fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName+ ex);
        }
    }
}