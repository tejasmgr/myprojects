package com.sunbeam.dto.request;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class SearchRequest {
    private String query;
    private int page = 0;
    private int size = 10;
    private Sort.Direction sortDirection = Sort.Direction.ASC;
    private String sortBy = "id";
}