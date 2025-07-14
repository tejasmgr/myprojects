// src/pages/auth/VerifyEmailPage.js
import React, { useEffect, useState, useRef } from 'react'; // Import useRef
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { unauthenticatedFetch } from '../../utils/api';

const VerifyEmailPage = () => {
    const [searchParams] = useSearchParams();
    const urlToken = searchParams.get('token'); // Token from URL query parameter
    const navigate = useNavigate();

    const [manualToken, setManualToken] = useState(''); // State for manual token input
    const [verificationStatus, setVerificationStatus] = useState('Awaiting token...');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    // Use a ref to track if the verification has already been attempted
    const hasVerified = useRef(false);

    // Function to handle the actual verification API call
    const performVerification = async (tokenToVerify) => {
        // Prevent re-execution if already verified or no token
        if (hasVerified.current || !tokenToVerify) {
            if (!tokenToVerify) {
                setError('No token provided for verification.');
                setVerificationStatus('Verification failed.');
            }
            return;
        }

        hasVerified.current = true; // Mark as attempted

        setLoading(true);
        setError('');
        setVerificationStatus('Verifying your email...');

        try {
            // The unauthenticatedFetch function should return data or throw an error if response.ok is false.
            await unauthenticatedFetch(`/auth/verify-email?token=${tokenToVerify}`, {
                method: 'GET',
            });
            setVerificationStatus('Your email has been successfully verified! Redirecting to login...');
            // Only set hasVerified.current to false if you want to allow re-verification under some condition
            // For email verification, it's usually a one-time action.
            setTimeout(() => navigate('/login'), 3000);
        } catch (err) {
            console.error('Email verification caught error:', err); // Log the full error object for debugging
            let errorMessage = 'Failed to verify email. Please try again.';

            // Check for network-related errors
            if (err instanceof TypeError && err.message === 'Failed to fetch') {
                errorMessage = 'Network error: Could not connect to the server. Please ensure the backend is running and accessible (check CORS settings).';
            } else if (err.message) {
                // Use the error message propagated from unauthenticatedFetch (which comes from backend JSON or statusText)
                errorMessage = err.message;
            }

            setError(errorMessage);
            setVerificationStatus('Email verification failed.');
            hasVerified.current = false; // Allow re-attempt if it was an error
        } finally {
            setLoading(false);
        }
    };

    // Effect to handle automatic verification if token is present in URL
    useEffect(() => {
        if (urlToken) {
            // Only call performVerification if it hasn't been called yet for this token
            // The ref 'hasVerified.current' handles the StrictMode double-call
            performVerification(urlToken);
        } else {
            setVerificationStatus('Please enter the verification token sent to your email.');
        }
    }, [urlToken]); // Depend on urlToken to trigger automatic verification

    // Handle manual token submission
    const handleManualSubmit = (e) => {
        e.preventDefault();
        // Reset hasVerified ref for manual submission to allow a new attempt
        hasVerified.current = false;
        performVerification(manualToken);
    };

    return (
        <div className="container mt-5 text-center">
            <div className="card shadow-lg p-4 rounded-4">
                <h2 className="card-title text-primary fw-bold mb-4">Email Verification</h2>
                {error && <p className="alert alert-danger rounded-3">{error}</p>}
                {verificationStatus && <p className={`alert ${error ? 'alert-danger' : 'alert-info'} rounded-3`}>{verificationStatus}</p>}

                {/* Show manual token input if no URL token and not loading */}
                {!urlToken && !loading && !error && (
                    <form onSubmit={handleManualSubmit} className="mt-4">
                        <div className="mb-3">
                            <label htmlFor="manualTokenInput" className="form-label visually-hidden">Enter Verification Token</label>
                            <input
                                type="text"
                                className="form-control rounded-3"
                                id="manualTokenInput"
                                value={manualToken}
                                onChange={(e) => setManualToken(e.target.value)}
                                placeholder="Enter verification token"
                                required
                            />
                        </div>
                        <button type="submit" className="btn btn-primary rounded-pill px-4 py-2" disabled={loading}>
                            {loading ? (
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                            ) : (
                                'Verify Manually'
                            )}
                        </button>
                    </form>
                )}

                {loading && (
                    <div className="spinner-border text-primary mt-3" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                )}

                <p className="mt-3">
                    If you are not automatically redirected, please <Link to="/login" className="text-decoration-none fw-bold">click here to login</Link>.
                </p>
            </div>
        </div>
    );
};

export default VerifyEmailPage;
