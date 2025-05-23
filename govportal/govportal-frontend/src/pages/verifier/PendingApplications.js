// --- src/pages/verifier/PendingApplications.js ---
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';
import { useNavigate } from 'react-router-dom'; // Ensure useNavigate is imported

const PendingApplications = () => {
    const { user, token } = useAuth();
    const navigate = useNavigate();
    const [pendingApplications, setPendingApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Pagination states
    const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-indexed pages
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0); // Total number of applications

    // Fetch applications based on current page
    useEffect(() => {
        // Redirect if not a verifier or not logged in
        if (!user || user.role !== 'VERIFIER') {
            navigate('/dashboard'); // Redirect to dashboard if not authorized
            return;
        }

        const fetchPendingApplications = async () => {
            try {
                setLoading(true);
                setError('');
                const data = await authenticatedFetch(`/verifier/pending?page=${currentPage}`, {
                    method: 'GET',
                });
                setPendingApplications(data.content);
                setTotalPages(data.totalPages);
                setCurrentPage(data.number);
                setTotalElements(data.totalElements);
            } catch (err) {
                console.error('Failed to fetch pending applications:', err);
                setError(err.message || 'Failed to load pending applications. Please try again.');
            } finally {
                setLoading(false);
            }
        };

        if (token) {
            fetchPendingApplications();
        }
    }, [user, token, navigate, currentPage]);

    // Handlers for pagination
    const handlePreviousPage = () => {
        if (currentPage > 0) {
            setCurrentPage(currentPage - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) {
            setCurrentPage(currentPage + 1);
        }
    };

    // Function to navigate to application details
    const handleReviewClick = (appId) => {
        navigate(`/verifier/applications/${appId}`);
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading pending applications...</span>
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
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Go to Dashboard
                </button>
            </div>
        );
    }

    if (pendingApplications.length === 0 && totalElements === 0) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-info rounded-3" role="alert">
                    No pending applications to review at the moment.
                </div>
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Go to Dashboard
                </button>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <h2 className="text-center mb-4 text-success fw-bold">Pending Applications for Review</h2>
            <div className="table-responsive shadow-lg rounded-4 overflow-hidden">
                <table className="table table-hover table-striped mb-0">
                    <thead className="bg-success text-white">
                        <tr>
                            <th scope="col" className="py-3">ID</th>
                            <th scope="col" className="py-3">Applicant Email</th>
                            <th scope="col" className="py-3">Document Type</th>
                            <th scope="col" className="py-3">Purpose</th>
                            <th scope="col" className="py-3">Submitted On</th>
                            <th scope="col" className="py-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingApplications.map((app) => (
                            <tr key={app.id}>
                                <td>{app.id}</td>
                                <td>{app.applicantEmail}</td>
                                <td>{app.documentType}</td>
                                <td>{app.purpose}</td>
                                <td>{new Date(app.submissionDate).toLocaleDateString()}</td>
                                <td>
                                    <button
                                        className="btn btn-sm btn-outline-success rounded-pill"
                                        onClick={() => handleReviewClick(app.id)} // Changed to navigate
                                    >
                                        Review
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
                <nav aria-label="Page navigation" className="mt-4">
                    <ul className="pagination justify-content-center">
                        <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
                            <button className="page-link" onClick={handlePreviousPage} disabled={currentPage === 0}>
                                Previous
                            </button>
                        </li>
                        {[...Array(totalPages)].map((_, index) => (
                            <li key={index} className={`page-item ${currentPage === index ? 'active' : ''}`}>
                                <button className="page-link" onClick={() => setCurrentPage(index)}>
                                    {index + 1}
                                </button>
                            </li>
                        ))}
                        <li className={`page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`}>
                            <button className="page-link" onClick={handleNextPage} disabled={currentPage === totalPages - 1}>
                                Next
                            </button>
                        </li>
                    </ul>
                </nav>
            )}
            {totalElements > 0 && (
                <p className="text-center mt-2 text-muted">
                    Showing {pendingApplications.length} of {totalElements} applications.
                </p>
            )}
        </div>
    );
};

export default PendingApplications;
