package com.sunbeam.service;

import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import java.util.List;

public interface VerificationWorkflowService {
    void assignToVerificationDesk(DocumentApplication application);
    void moveToNextDesk(DocumentApplication application);
    List<DocumentApplication> getApplicationsByDesk(String deskLevel);
    void finalizeVerification(DocumentApplication application, boolean approved, String remarks);
}