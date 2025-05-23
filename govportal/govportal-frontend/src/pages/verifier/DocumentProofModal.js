// --- src/pages/verifier/DocumentProofModal.js ---
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
import 'react-pdf/dist/esm/Page/TextLayer.css';
import './DocumentProofModal.css'; // Import CSS for the modal

// Set the worker source to a reliable CDN URL for react-pdf.
// This is generally the most robust way to ensure the worker loads correctly,
// avoiding local file system or module import complexities.
// Using pdfjs.version ensures compatibility with your installed react-pdf version.
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`;

const DocumentProofModal = ({ documentProofId, onClose }) => {
    const [documentUrl, setDocumentUrl] = useState(null);
    const [error, setError] = useState('');
    const [numPages, setNumPages] = useState(null);
    const [pageNumber, setPageNumber] = useState(1);
    const [contentType, setContentType] = useState('');
    const { token } = useAuth();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDocument = async () => {
            setLoading(true);
            setError('');
            setDocumentUrl(null); // Reset documentUrl on new fetch
            setContentType('');

            try {
                const response = await authenticatedFetch(
                    `/verifier/proofs/${documentProofId}/view`,
                    { method: 'GET' }
                );

                console.log("Fetch Response:", response);

                if (!response.ok) {
                    let errorMessage = `Failed to fetch document: ${response.status}`;
                    try {
                        // Attempt to read error response as text, but handle if it's not text
                        const errorText = await response.text();
                        errorMessage += ` - ${errorText}`;
                    } catch (e) {
                        console.warn("Failed to read error response as text:", e);
                    }
                    throw new Error(errorMessage);
                }

                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                setDocumentUrl(url);
                setContentType(response.headers.get('Content-Type'));
                setLoading(false);

            } catch (err) {
                console.error('Error fetching document:', err);
                setError(err.message || 'Failed to load document.');
                setLoading(false);
            }
        };

        if (documentProofId && token) {
            fetchDocument();
        }

        // Cleanup function to revoke the object URL when the component unmounts
        // or when documentUrl changes to prevent memory leaks.
        // documentUrl is NOT included in the dependency array to prevent infinite loops.
        return () => {
            if (documentUrl) {
                URL.revokeObjectURL(documentUrl);
            }
        };
    }, [documentProofId, token]); // Dependencies: only re-run when documentProofId or token changes

    const onDocumentLoadSuccess = ({ numPages }) => {
        setNumPages(numPages);
        setPageNumber(1); // Reset to first page on new document load
        setError(''); // Clear any previous loading errors
    };

    // New handler for errors from react-pdf's Document component
    const onDocumentLoadError = (pdfError) => {
        console.error('Error loading PDF document with react-pdf:', pdfError);
        setError(`Failed to load PDF file. Details: ${pdfError.message || 'Unknown error'}`);
        setNumPages(null);
        setPageNumber(1);
        setDocumentUrl(null); // Clear URL to prevent re-attempts
    };


    const goToPrevPage = () => setPageNumber(pageNumber - 1);
    const goToNextPage = () => setPageNumber(pageNumber + 1);

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="btn btn-sm btn-secondary float-end" onClick={onClose}>
                    Close
                </button>
                <h5 className="mb-3">Document Preview</h5>
                {error && <div className="alert alert-danger">{error}</div>}
                {loading && <p>Loading document...</p>}
                {!loading && documentUrl && (
                    <>
                        {/* Log the content type here for debugging */}
                        {console.log("Rendering document with contentType:", contentType)}
                        {contentType === 'application/pdf' && (
                            <div>
                                <Document
                                    file={documentUrl}
                                    onLoadSuccess={onDocumentLoadSuccess}
                                    onLoadError={onDocumentLoadError} // Add this error handler
                                    className="w-100" // Ensure document takes full width
                                >
                                    <Page pageNumber={pageNumber} width={Math.min(window.innerWidth * 0.7, 800)} /> {/* Adjust width for responsiveness */}
                                </Document>
                                {numPages && ( // Only show pagination if numPages is available
                                    <>
                                        <p className="mt-2 text-center">
                                            Page {pageNumber} of {numPages}
                                        </p>
                                        <div className="d-flex justify-content-center mt-2">
                                            <button
                                                className="btn btn-sm btn-primary me-2"
                                                onClick={goToPrevPage}
                                                disabled={pageNumber <= 1}
                                            >
                                                Previous
                                            </button>
                                            <button
                                                className="btn btn-sm btn-primary"
                                                onClick={goToNextPage}
                                                disabled={pageNumber >= numPages}
                                            >
                                                Next
                                            </button>
                                        </div>
                                    </>
                                )}
                            </div>
                        )}
                        {contentType.startsWith('image/') && (
                            <img src={documentUrl} alt="Document Proof" className="img-fluid" />
                        )}
                        {!contentType.startsWith('application/pdf') && !contentType.startsWith('image/') && (
                            <p>Unsupported document type. <a href={documentUrl} target="_blank" rel="noopener noreferrer">Open in new tab</a> or download.</p>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default DocumentProofModal;
