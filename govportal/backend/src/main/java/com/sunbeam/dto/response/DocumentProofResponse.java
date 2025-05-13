package com.sunbeam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProofResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private String fileUrl;
    // You might include a link to download the file or other relevant metadata
}