package com.sunbeam.service;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.DocumentProof;
import com.sunbeam.model.User;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
	DocumentApplication submitApplication(User applicant, DocumentApplicationRequest request,List<MultipartFile> files) throws IOException;

	DocumentApplicationResponse getApplicationById(Long id);
	
	DocumentApplicationDetailsResponse getDocumentApplicationDetails(long id);

//	List<DocumentApplicationResponse> getUserApplications();

	Page<DocumentApplicationResponse> getAllApplications(Pageable pageable);

	byte[] getCertificatePdf(Long applicationId);

//	DocumentApplicationResponse approveApplication(Long applicationId, String remarks);

//	DocumentApplicationResponse rejectApplication(Long applicationId, String remarks);

//	DocumentApplicationResponse reassignApplication(Long applicationId, Long newVerifierId);

//	String uploadSupportingDocument(Long applicationId, MultipartFile file);

	DocumentProof getDocumentProof(Long proofId);
	
	Resource viewDocumentProof(Long documentProofId) throws IOException;

	Page<DocumentApplicationResponse> getApprovalPassesApplicationsOfCitizen(Pageable pageable);
}