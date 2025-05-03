package com.sunbeam.service.impl;

import com.sunbeam.model.DocumentApplication;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.service.ApplicationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationStatusServiceImpl implements ApplicationStatusService {

    private final DocumentApplicationRepository appRepository;

//    @Override
//    public Map<String, Long> getStatusCounts() {
//        return appRepository.getCountByStatus()
//                .stream()
//                .collect(Collectors.toMap(
//                        arr -> ((DocumentApplication.ApplicationStatus) arr[0]).name(),
//                        arr -> (Long) arr[1]
//                ));
//    }

    @Override
    public List<DocumentApplication> getRecentApprovals(int count) {
        PageRequest pageRequest = PageRequest.of(0, count); // Page 0, with 'count' number of elements
        return appRepository.findTopNByStatusOrderByResolvedDateDesc(
                DocumentApplication.ApplicationStatus.APPROVED,
                pageRequest
        );
    }
}