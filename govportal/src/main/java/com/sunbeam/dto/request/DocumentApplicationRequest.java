package com.sunbeam.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentApplicationRequest {
	@NotBlank(message = "Application type is required")
    private String applicationType;

 @NotBlank(message = "Application type is required")
 private String documentType;


 @NotBlank(message = "Form data is required")
 private String formData;
 
 @NotBlank(message = "Purpose is required") // Add validation
 private String purpose;


 //  We don't include userId here, as we'll get it from the
 //  authentication context on the server-side.
 //  We also don't include documentProofs in the DTO, as files
 //  are handled separately via MultipartFile.
}