// --- src/pages/citizen/SubmitApplication.js ---
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';
import { useNavigate, Link } from 'react-router-dom';
import { documentSchemas } from '../../utils/documentSchemas'; // Import document schemas

const SubmitApplication = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    // State for main form fields
    const [documentType, setDocumentType] = useState('');
    const [purpose, setPurpose] = useState('');
    const [selectedFiles, setSelectedFiles] = useState([]);

    // State for dynamically generated form data
    const [dynamicFormData, setDynamicFormData] = useState({});

    // State for UI feedback
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    // successMessage will now hold the full string from backend
    const [successMessage, setSuccessMessage] = useState('');

    // State for the success modal
    const [showModal, setShowModal] = useState(false);
    // submittedApplicationId is no longer needed as we display the full string
    // const [submittedApplicationId, setSubmittedApplicationId] = useState(null);

    // Reset dynamic form data when document type changes
    useEffect(() => {
        setDynamicFormData({}); // Clear previous dynamic form data
    }, [documentType]);

    // Handle changes in dynamic form fields
    const handleDynamicInputChange = (e) => {
        const { name, value } = e.target;
        setDynamicFormData(prev => ({ ...prev, [name]: value }));
    };

    // Handle file selection
    const handleFileChange = (e) => {
        setSelectedFiles(Array.from(e.target.files));
    };

    // Function to close the modal and optionally redirect
    const handleCloseModal = () => {
        setShowModal(false);
        // setSubmittedApplicationId(null); // No longer needed
        // Clear form fields after successful submission
        setDocumentType('');
        setPurpose('');
        setDynamicFormData({});
        setSelectedFiles([]);
        navigate('/dashboard'); // Changed redirection to dashboard
    };

    // Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMessage(''); // Clear previous success message
        setLoading(true);

        // Basic client-side validation for main fields
        if (!documentType || !purpose || selectedFiles.length === 0) {
            setError('Please fill in all required main fields and upload at least one document.');
            setLoading(false);
            return;
        }

        // Validate dynamic form data based on schema
        const currentSchema = documentSchemas[documentType];
        if (currentSchema) {
            for (const field of currentSchema) {
                if (field.required && (!dynamicFormData[field.name] || String(dynamicFormData[field.name]).trim() === '')) {
                    setError(`Please fill in the required field: ${field.label}`);
                    setLoading(false);
                    return;
                }
            }
        } else {
            setError('Invalid document type selected.');
            setLoading(false);
            return;
        }

        // Prepare applicationData
        const applicationData = {
            applicationType: documentType,
            documentType: documentType,
            // Convert dynamicFormData object to JSON string
            formData: JSON.stringify(dynamicFormData),
            purpose: purpose,
        };

        // Create FormData object for multipart/form-data request
        const formData = new FormData();
        formData.append('applicationData', JSON.stringify(applicationData)); // Stringify JSON object

        // Append each selected file
        selectedFiles.forEach((file) => {
            formData.append('documents', file);
        });

        try {
            // The backend returns a plain string like "Application submitted successfully with Application ID : 123"
            // authenticatedFetch will now return this plain text directly
            const responseText = await authenticatedFetch('/documents/submit', {
                method: 'POST',
                body: formData,
                headers: {}, // Browser handles Content-Type for FormData
            });

            // --- ADDED LOG FOR DEBUGGING ---
            console.log("Response text from backend:", responseText);
            // --- END ADDED LOG ---

            // Directly use the responseText as the success message
            setSuccessMessage(responseText);
            setShowModal(true); // Show the modal

            // Clear form fields after successful submission (will be done after modal close)

        } catch (err) {
            console.error('Application submission error:', err);
            const errorMessage = err.message || 'Failed to submit application. Please try again.';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    // Ensure only citizens can access this page
    if (user && user.role !== 'CITIZEN') {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-danger rounded-3" role="alert">
                    Access Denied. Only Citizens can submit applications.
                </div>
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Go to Dashboard
                </button>
            </div>
        );
    }

    // Get the schema for the currently selected document type
    const currentDocumentSchema = documentSchemas[documentType] || [];

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8 col-lg-7">
                    <div className="card shadow-lg p-4 rounded-4">
                        <h2 className="card-title text-center mb-4 text-primary fw-bold">Submit New Document Application</h2>
                        <form onSubmit={handleSubmit}>
                            {error && <div className="alert alert-danger rounded-3">{error}</div>}
                            {/* Success message will be shown in modal, not here */}

                            <div className="mb-3">
                                <label htmlFor="documentType" className="form-label">Document Type</label>
                                <select
                                    className="form-select rounded-3"
                                    id="documentType"
                                    value={documentType}
                                    onChange={(e) => setDocumentType(e.target.value)}
                                    required
                                >
                                    <option value="">Select Document Type</option>
                                    <option value="INCOME">Income Certificate</option>
                                    <option value="CASTE">Caste Certificate</option>
                                    <option value="DOMICILE">Domicile Certificate</option>
                                    <option value="BIRTH">Birth Certificate</option>
                                </select>
                            </div>

                            <div className="mb-3">
                                <label htmlFor="purpose" className="form-label">Purpose of Application</label>
                                <textarea
                                    className="form-control rounded-3"
                                    id="purpose"
                                    rows="2"
                                    value={purpose}
                                    onChange={(e) => setPurpose(e.target.value)}
                                    placeholder="e.g., For educational scholarship, Job application"
                                    required
                                ></textarea>
                            </div>

                            {/* Dynamically rendered form fields based on documentType */}
                            {documentType && currentDocumentSchema.length > 0 && (
                                <div className="mb-4 p-3 border rounded-3 bg-light">
                                    <h5 className="mb-3 text-secondary">Required Details for {documentType.replace('_', ' ')} Certificate</h5>
                                    {currentDocumentSchema.map((field) => (
                                        <div className="mb-3" key={field.name}>
                                            <label htmlFor={field.name} className="form-label">
                                                {field.label} {field.required && <span className="text-danger">*</span>}
                                            </label>
                                            {field.type === 'textarea' ? (
                                                <textarea
                                                    className="form-control rounded-3"
                                                    id={field.name}
                                                    name={field.name}
                                                    rows="3"
                                                    value={dynamicFormData[field.name] || ''}
                                                    onChange={handleDynamicInputChange}
                                                    required={field.required}
                                                ></textarea>
                                            ) : field.type === 'select' ? (
                                                <select
                                                    className="form-select rounded-3"
                                                    id={field.name}
                                                    name={field.name}
                                                    value={dynamicFormData[field.name] || ''}
                                                    onChange={handleDynamicInputChange}
                                                    required={field.required}
                                                >
                                                    <option value="">Select {field.label}</option>
                                                    {field.options.map(option => (
                                                        <option key={option} value={option}>{option}</option>
                                                    ))}
                                                </select>
                                            ) : (
                                                <input
                                                    type={field.type}
                                                    className="form-control rounded-3"
                                                    id={field.name}
                                                    name={field.name}
                                                    value={dynamicFormData[field.name] || ''}
                                                    onChange={handleDynamicInputChange}
                                                    required={field.required}
                                                />
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}

                            <div className="mb-3">
                                <label htmlFor="documents" className="form-label">Upload Supporting Documents</label>
                                <input
                                    type="file"
                                    className="form-control rounded-3"
                                    id="documents"
                                    multiple
                                    onChange={handleFileChange}
                                    required
                                    aria-describedby="fileHelp"
                                />
                                <div className="form-text text-muted" id="fileHelp">
                                    Upload relevant documents (e.g., scanned copies of ID, proofs). Multiple files can be selected.
                                </div>
                                {/* Display selected files */}
                                {selectedFiles.length > 0 && (
                                    <div className="mt-2">
                                        <p className="fw-bold">Selected Files:</p>
                                        <ul className="list-group">
                                            {selectedFiles.map((file, index) => (
                                                <li key={index} className="list-group-item list-group-item-action d-flex justify-content-between align-items-center rounded-3 mb-1">
                                                    {file.name}
                                                    <span className="badge bg-primary rounded-pill">{Math.round(file.size / 1024)} KB</span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>

                            <button type="submit" className="btn btn-primary w-100 rounded-3 py-2 fw-bold" disabled={loading}>
                                {loading ? (
                                    <>
                                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                        Submitting...
                                    </>
                                ) : (
                                    'Submit Application'
                                )}
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            {/* Success Modal */}
            {showModal && (
                <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-labelledby="successModalLabel" aria-hidden="true">
                    <div className="modal-dialog modal-dialog-centered" role="document">
                        <div className="modal-content rounded-4 shadow-lg">
                            <div className="modal-header bg-success text-white rounded-top-4">
                                <h5 className="modal-title" id="successModalLabel">Application Submitted!</h5>
                                <button type="button" className="btn-close btn-close-white" aria-label="Close" onClick={handleCloseModal}></button>
                            </div>
                            <div className="modal-body p-4 text-center">
                                {/* Display the full success message directly */}
                                <p className="lead fw-bold text-success">{successMessage}</p>
                                <p className="text-muted">You will be redirected to the dashboard shortly.</p>
                            </div>
                            <div className="modal-footer justify-content-center border-top-0">
                                <button type="button" className="btn btn-primary rounded-pill px-4 py-2" onClick={handleCloseModal}>
                                    Go to Dashboard
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
            {/* Modal Backdrop */}
            {showModal && <div className="modal-backdrop fade show"></div>}
        </div>
    );
};

export default SubmitApplication;
