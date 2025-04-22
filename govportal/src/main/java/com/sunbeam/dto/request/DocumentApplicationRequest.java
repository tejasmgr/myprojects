package com.sunbeam.dto.request;
import com.sunbeam.model.DocumentApplication.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class DocumentApplicationRequest {
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
    
    @NotNull(message = "Documents are required")
    private List<MultipartFile> documents;
    
    private String purpose;
}