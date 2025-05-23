// --- src/pages/verifier/ApplicationDetails.js ---
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';

const ApplicationDetails = () => {
    const { id } = useParams(); // Get application ID from URL
    const navigate = useNavigate();
    const { user, token } = useAuth();

    const [application, setApplication] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [remarks, setRemarks] = useState('');
    const [actionLoading, setActionLoading] = useState(false);
    const [actionMessage, setActionMessage] = useState('');

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
                const data = await authenticatedFetch(`/documents/${id}`, {
                    method: 'GET',
                });
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

    // Function to handle approving or rejecting an application
    const handleAction = async (actionType) => { // 'approve' or 'reject'
        setActionLoading(true);
        setActionMessage('');
        setError('');

        if (!remarks.trim()) {
            setError('Remarks are required for this action.');
            setActionLoading(false);
            return;
        }

        try {
            const endpoint = `/verifier/${actionType}/${id}`;
            const response = await authenticatedFetch(endpoint, {
                method: 'POST',
                body: JSON.stringify({ remarks }), // Assuming backend expects remarks in JSON body
            });
            setActionMessage(response || `Application ${actionType}d successfully.`); // Backend might return a message
            // Optionally, update application status in state or refetch
            setApplication(prev => ({ ...prev, status: actionType.toUpperCase() })); // Optimistic update
            // Redirect back to pending applications or dashboard after a delay
            setTimeout(() => navigate('/verifier/pending-applications'), 2000);
        } catch (err) {
            console.error(`Failed to ${actionType} application:`, err);
            setError(err.message || `Failed to ${actionType} application. Please try again.`);
        } finally {
            setActionLoading(false);
        }
    };

    // Function to download a document
    const handleDownloadDocument = (documentId) => {
        // Construct the download URL. Backend should handle file streaming.
        const downloadUrl = `http://localhost:8080/api/documents/${id}/download/${documentId}`;
        // Open in a new tab to trigger download
        window.open(downloadUrl, '_blank');
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
        if (application.formData) {
            parsedFormData = JSON.parse(application.formData);
        }
    } catch (e) {
        console.error("Failed to parse formData JSON:", e);
        parsedFormData = { error: "Invalid JSON format in form data." };
    }

    return (
        <div className="container mt-5">
            <h2 className="text-center mb-4 text-success fw-bold">Application Details (ID: {application.id})</h2>
            <div className="card shadow-lg p-4 rounded-4 mb-4">
                <div className="card-body">
                    <h4 className="card-title text-primary mb-3">Applicant Information</h4>
                    <p><strong>Applicant Name:</strong> {application.applicantFullName}</p>
                    <p><strong>Applicant Email:</strong> {application.applicantEmail}</p>
                    <p><strong>Aadhar Number:</strong> {application.applicantAadharNumber}</p>
                    <p><strong>Address:</strong> {application.applicantAddress}</p>
                    <hr />

                    <h4 className="card-title text-primary mb-3">Application Details</h4>
                    <p><strong>Document Type:</strong> {application.documentType}</p>
                    <p><strong>Purpose:</strong> {application.purpose}</p>
                    <p><strong>Status:</strong>
                        <span className={`badge ms-2 ${
                            application.status === 'PENDING' ? 'bg-warning text-dark' :
                            application.status === 'APPROVED' ? 'bg-success' :
                            application.status === 'REJECTED' ? 'bg-danger' :
                            'bg-secondary'
                        } rounded-pill px-3 py-2`}>
                            {application.status}
                        </span>
                    </p>
                    <p><strong>Submitted On:</strong> {new Date(application.submissionDate).toLocaleString()}</p>
                    {application.resolvedDate && (
                        <p><strong>Resolved On:</strong> {new Date(application.resolvedDate).toLocaleString()}</p>
                    )}
                    {application.rejectionReason && application.status === 'REJECTED' && (
                        <p className="text-danger"><strong>Rejection Reason:</strong> {application.rejectionReason}</p>
                    )}
                    <hr />

                    <h4 className="card-title text-primary mb-3">Additional Form Data</h4>
                    {Object.keys(parsedFormData).length > 0 ? (
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
                    {application.documents && application.documents.length > 0 ? (
                        <ul className="list-group">
                            {application.documents.map((doc) => (
                                <li key={doc.id} className="list-group-item d-flex justify-content-between align-items-center rounded-3 mb-2">
                                    {doc.fileName}
                                    <button
                                        className="btn btn-sm btn-outline-primary rounded-pill"
                                        onClick={() => handleDownloadDocument(doc.id)}
                                    >
                                        Download
                                    </button>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-muted">No supporting documents uploaded.</p>
                    )}
                </div>
            </div>

            {/* Verifier Actions Section */}
            {application.status === 'PENDING' && (
                <div className="card shadow-lg p-4 rounded-4 mt-4">
                    <h4 className="card-title text-secondary mb-3">Verifier Actions</h4>
                    {actionMessage && <div className="alert alert-success rounded-3">{actionMessage}</div>}
                    {error && <div className="alert alert-danger rounded-3">{error}</div>}
                    <div className="mb-3">
                        <label htmlFor="remarks" className="form-label">Remarks (Required for Approve/Reject)</label>
                        <textarea
                            className="form-control rounded-3"
                            id="remarks"
                            rows="3"
                            value={remarks}
                            onChange={(e) => setRemarks(e.target.value)}
                            placeholder="Enter your remarks here..."
                            required
                        ></textarea>
                    </div>
                    <div className="d-flex justify-content-around mt-3">
                        <button
                            className="btn btn-success btn-lg rounded-pill px-5"
                            onClick={() => handleAction('approve')}
                            disabled={actionLoading}
                        >
                            {actionLoading ? (
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                            ) : (
                                'Approve'
                            )}
                        </button>
                        <button
                            className="btn btn-danger btn-lg rounded-pill px-5"
                            onClick={() => handleAction('reject')}
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
            <div className="text-center mt-4">
                <button className="btn btn-secondary rounded-pill px-4 py-2" onClick={() => navigate('/verifier/pending-applications')}>
                    Back to Pending Applications
                </button>
            </div>
        </div>
    );
};

export default ApplicationDetails;
