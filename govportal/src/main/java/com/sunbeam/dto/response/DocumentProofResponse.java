package com.sunbeam.dto.response;

import lombok.Data;

@Data
public class DocumentProofResponse {
    private Long id;
    private String fileName;
    private String contentType;
    // You might include a link to download the file or other relevant metadata
}