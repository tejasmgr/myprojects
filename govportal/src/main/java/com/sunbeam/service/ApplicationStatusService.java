package com.sunbeam.service;

import com.sunbeam.model.DocumentApplication;
import java.util.List;
import java.util.Map;

public interface ApplicationStatusService {
//    Map<String, Long> getStatusCounts();
    List<DocumentApplication> getRecentApprovals(int count);
}