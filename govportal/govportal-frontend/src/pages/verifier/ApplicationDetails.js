// --- src/pages/verifier/ApplicationDetails.js ---
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';
import DocumentProofModal from './DocumentProofModal'; // Import the new modal component
import './ApplicationDetails.css'; // Import CSS for modal (if you create a separate file)

const ApplicationDetails = () => {
    const { id } = useParams(); // Get application ID from URL
    const navigate = useNavigate();
    const { user, token } = useAuth();

    const [application, setApplication] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [remarks, setRemarks] = useState(''); // For reject action
    const [actionLoading, setActionLoading] = useState(false);
    const [actionMessage, setActionMessage] = useState('');
    const [isProofModalOpen, setIsProofModalOpen] = useState(false);
    const [selectedProofId, setSelectedProofId] = useState(null);
    const [showApproveModal, setShowApproveModal] = useState(false);
    const [approveRemarks, setApproveRemarks] = useState('');
    const [approvalLoading, setApprovalLoading] = useState(false);
    const [approvalError, setApprovalError] = useState('');
    

    useEffect(() => {
        // Redirect if not a verifier or not logged in
        if (!user || user.role !== 'VERIFIER') {
            navigate('/dashboard');
            return;
        }

        const fetchApplicationDetails = async () => {
            try {
                setLoading(true);
                setError('');
                // Fetch application details using the ID
                const data = await authenticatedFetch(`/verifier/application/details/${id}`, {
                    method: 'GET',
                });
                console.log("Received application data from backend:", data);
                setApplication(data);
            } catch (err) {
                console.error('Failed to fetch application details:', err);
                setError(err.message || 'Failed to load application details. Please try again.');
            } finally {
                setLoading(false);
            }
        };

        if (token && id) {
            fetchApplicationDetails();
        }
    }, [user, token, id, navigate]);

    const handleViewDocument = (documentProofId) => {
        setSelectedProofId(documentProofId);
        setIsProofModalOpen(true);
    };

    const handleCloseProofModal = () => {
        setIsProofModalOpen(false);
        setSelectedProofId(null);
    };

    const handleOpenApproveModal = () => {
        setShowApproveModal(true);
        setApproveRemarks('');
        setApprovalError('');
    };

    const handleCloseApproveModal = () => {
        setShowApproveModal(false);
        setApproveRemarks('');
        setApprovalError('');
    };

    const handleApprove = async () => {
        if (!approveRemarks.trim()) {
            setApprovalError('Remarks are required for approval.');
            return;
        }

        setApprovalLoading(true);
        setApprovalError('');

        try {
            const response = await authenticatedFetch(
                `/verifier/approve/${id}?remarks=${encodeURIComponent(approveRemarks)}`,
                {
                    method: 'POST',
                }
            );

            // If authenticatedFetch returns the parsed JSON directly on success,
            // then 'response' here is already the JSON object (DocumentApplicationResponse).
            const data = response; // No need to call response.json() again

            if (!data) {
                throw new Error('Failed to approve application: Empty response received.');
            }

            setApplication(data); // Update the application state with the approved status
            setActionMessage('Application approved successfully!');
            setShowApproveModal(false);
            setTimeout(() => navigate('/verifier/pending-applications'), 2000);
        } catch (err) {
            console.error('Error approving application:', err);
            setApprovalError(err.message);
        } finally {
            setApprovalLoading(false);
        }
    };

    // Function to handle rejecting an application (using the existing endpoint)
    const handleReject = async () => {
        setActionLoading(true);
        setActionMessage('');
        setError('');

        if (!remarks.trim()) {
            setError('Remarks are required for rejection.');
            setActionLoading(false);
            return;
        }

        try {
            const endpoint = `/verifier/reject/${id}`;
            const response = await authenticatedFetch(endpoint, {
                method: 'POST',
                body: JSON.stringify({ remarks }), // Assuming backend expects remarks in JSON body for rejection
            });
            setActionMessage(response || `Application rejected successfully.`); // Backend might return a message
            setApplication(prev => ({ ...prev, status: 'REJECTED', rejectionReason: remarks })); // Optimistic update
            setTimeout(() => navigate('/verifier/pending-applications'), 2000);
        } catch (err) {
            console.error(`Failed to reject application:`, err);
            setError(err.message || `Failed to reject application. Please try again.`);
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading application details...</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-danger rounded-3" role="alert">
                    {error}
                </div>
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/verifier/pending-applications')}>
                    Back to Pending Applications
                </button>
            </div>
        );
    }

    if (!application) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-warning rounded-3" role="alert">
                    Application not found or you do not have access.
                </div>
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/verifier/pending-applications')}>
                    Back to Pending Applications
                </button>
            </div>
        );
    }

    // Parse formData string back into an object for display
    let parsedFormData = {};
    try {
        if (application.formData && typeof application.formData === 'string') {
            parsedFormData = JSON.parse(application.formData);
        }
    } catch (e) {
        console.error("Failed to parse formData JSON:", e);
        parsedFormData = { __parseError: "Invalid JSON format in form data." }; // Use a unique key for error
    }

    // Determine which field holds the documents (documents, documentProofs, or documentPaths)
    const documentsToDisplay = application.documents || application.documentProofs || application.documentPaths;

    return (
        <div className="container mt-5">
            <h2 className="text-center mb-4 text-success fw-bold">Application Details (ID: {application.id})</h2>
            <div className="card shadow-lg p-4 rounded-4 mb-4">
                <div className="card-body">
                    <h4 className="card-title text-primary mb-3">Applicant Information</h4>
                    <p><strong>Applicant Name:</strong> {application.applicantName || 'N/A'}</p>
                    <p><strong>Aadhar Number:</strong> {application.adhaarNumber || 'N/A'}</p>
                    <hr />

                    <h4 className="card-title text-primary mb-3">Application Details</h4>
                    <p><strong>Document Type:</strong> {application.documentType || 'N/A'}</p>
                    <p><strong>Purpose:</strong> {application.purpose || 'N/A'}</p>
                    <p><strong>Status:</strong>
                        <span className={`badge ms-2 ${
                            application.status === 'PENDING' ? 'bg-warning text-dark' :
                            application.status === 'APPROVED' ? 'bg-success' :
                            application.status === 'REJECTED' ? 'bg-danger' :
                            'bg-secondary'
                        } rounded-pill px-3 py-2`}>
                            {application.status || 'N/A'}
                        </span>
                    </p>
                    <p><strong>Submitted On:</strong> {application.submissionDate ? new Date(application.submissionDate).toLocaleString() : 'N/A'}</p>
                    {application.resolvedDate && (
                        <p><strong>Resolved On:</strong> {new Date(application.resolvedDate).toLocaleString()}</p>
                    )}
                    {application.rejectionReason && application.status === 'REJECTED' && (
                        <p className="text-danger"><strong>Rejection Reason:</strong> {application.rejectionReason}</p>
                    )}
                    <hr />

                    <h4 className="card-title text-primary mb-3">Additional Form Data</h4>
                    {parsedFormData.__parseError ? (
                        <p className="text-danger">Error parsing form data: {parsedFormData.__parseError}</p>
                    ) : Object.keys(parsedFormData).length > 0 ? (
                        <ul className="list-group list-group-flush">
                            {Object.entries(parsedFormData).map(([key, value]) => (
                                <li key={key} className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>{key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}:</strong> {/* Format key to readable text */}
                                    <span>{String(value)}</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-muted">No additional form data provided.</p>
                    )}
                    <hr />

                    <h4 className="card-title text-primary mb-3">Supporting Documents</h4>
                    {documentsToDisplay && documentsToDisplay.length > 0 ? (
                        <ul className="list-group">
                            {documentsToDisplay.map((doc) => (
                                <li key={doc.id} className="list-group-item d-flex justify-content-between align-items-center rounded-3 mb-2">
                                    {doc.fileName}
                                    <button
                                        className="btn btn-sm btn-outline-primary rounded-pill"
                                        onClick={() => handleViewDocument(doc.id)} // Use the new handler
                                    >
                                        View
                                    </button>
                                    {/* Optionally add a separate download button if needed */}
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-muted">No supporting documents uploaded.</p>
                    )}
                </div>
            </div>

            {isProofModalOpen && (
                <DocumentProofModal
                    documentProofId={selectedProofId}
                    onClose={handleCloseProofModal}
                />
            )}

            {/* Verifier Actions Section */}
            {application.status === 'PENDING' && (
                <div className="card shadow-lg p-4 rounded-4 mt-4">
                    <h4 className="card-title text-secondary mb-3">Verifier Actions</h4>
                    {actionMessage && <div className="alert alert-success rounded-3">{actionMessage}</div>}
                    {approvalError && <div className="alert alert-danger rounded-3">{approvalError}</div>}
                    <div className="mb-3">
                        <button className="btn btn-success btn-lg rounded-pill px-5" onClick={handleOpenApproveModal} disabled={approvalLoading}>
                            {approvalLoading ? <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> : 'Approve'}
                        </button>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="remarks" className="form-label">Remarks (Required for Reject)</label>
                        <textarea
                            className="form-control rounded-3"
                            id="remarks"
                            rows="3"
                            value={remarks}
                            onChange={(e) => setRemarks(e.target.value)}
                            placeholder="Enter your remarks for rejection here..."
                        ></textarea>
                    </div>
                    <div className="d-flex justify-content-around mt-3">
                        <button
                            className="btn btn-danger btn-lg rounded-pill px-5"
                            onClick={handleReject}
                            disabled={actionLoading}
                        >
                            {actionLoading ? (
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                            ) : (
                                'Reject'
                            )}
                        </button>
                    </div>
                </div>
            )}

            {/* Approve Modal */}
            {showApproveModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button className="btn btn-sm btn-secondary float-end" onClick={handleCloseApproveModal}>
                            Cancel
                        </button>
                        <h5 className="mb-3">Approve Application</h5>
                        {approvalError && <div className="alert alert-danger">{approvalError}</div>}

                        <div className="mb-3">
                            <label htmlFor="approveRemarks" className="form-label">Remarks for Approval</label>
                            <textarea
                                id="approveRemarks"
                                className="form-control"
                                value={approveRemarks}
                                onChange={(e) => setApproveRemarks(e.target.value)}
                                rows="3"
                            />
                        </div>

                        <button
                            className="btn btn-primary"
                            onClick={handleApprove}
                            disabled={approvalLoading}
                        >
                            {approvalLoading ? 'Approving...' : 'Approve'}
                        </button>
                    </div>
                </div>
            )}

            <div className="text-center mt-4">
                <button className="btn btn-secondary rounded-pill px-4 py-2" onClick={() => navigate('/verifier/pending-applications')}>
                    Back to Pending Applications
                </button>
            </div>
        </div>
    );
};

export default ApplicationDetails;