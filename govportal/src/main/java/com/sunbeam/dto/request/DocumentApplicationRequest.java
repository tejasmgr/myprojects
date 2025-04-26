package com.sunbeam.dto.request;
import com.sunbeam.model.DocumentApplication.DocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class DocumentApplicationRequest {
	@NotNull(message = "Document type is required")
    private DocumentType documentType;
    
	private String fullName;
	
	private String address;
	
    @NotNull(message = "Documents are required")
    @Size(min = 1, message = "At least one document must be uploaded")
    private List<MultipartFile> documents;
    
    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose must be less than 500 characters")
    private String purpose;
    
    // Additional fields if needed
//    private String additionalInfo;
}