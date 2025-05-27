// src/pages/auth/RegisterPage.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { unauthenticatedFetch } from '../../utils/api'; // Import API utility

const RegisterPage = () => {
    // State to manage form data
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '', // Client-side only for confirmation
        address: '',
        aadharNumber: ''
    });
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Handle input changes and update form data state
    const handleChange = (e) => {
        const { id, value } = e.target;
        setFormData(prev => ({ ...prev, [id]: value }));
    };

    // Handle form submission for registration
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission
        setError(''); // Clear previous errors
        setSuccessMessage(''); // Clear previous success messages
        setLoading(true); // Set loading state

        // Client-side password confirmation
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match.');
            setLoading(false);
            return;
        }

        // Destructure to exclude confirmPassword from the data sent to the backend
        const { confirmPassword, ...dataToSend } = formData;

        try {
            // Make API call to your backend's register endpoint
            await unauthenticatedFetch('/auth/register', { // Removed unused responseData
                method: 'POST',
                body: JSON.stringify(dataToSend),
            });

            // If successful, display success message and redirect to email verification page
            setSuccessMessage('Registration successful! Please check your email for a verification token.');
            // Redirect to verify-email page for manual token entry
            setTimeout(() => navigate('/verify-email'), 3000); // Navigate without a token in URL

        } catch (err) {
            console.error('Registration error:', err);
            // Handle specific error messages from backend
            if (err.message.includes('Email is Already Registered')) {
                setError('This email is already registered. Please try logging in or use a different email.');
            } else if (err.message.includes('Validation failed')) {
                // For backend validation errors (e.g., password format)
                setError(err.message);
            }else if(err.message.includes('Failed to fetch')){
                setError("Failed to Connect the server");
            }
            
            else {
                setError('An unexpected error occurred during registration. Please try again later.');
            }
        } finally {
            setLoading(false); // Reset loading state
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8 col-lg-6">
                    <div className="card shadow-lg p-4 rounded-4">
                        <h2 className="card-title text-center mb-4 text-success fw-bold">Register for GovPortal</h2>
                        <form onSubmit={handleSubmit}>
                            {error && <div className="alert alert-danger rounded-3">{error}</div>}
                            {successMessage && <div className="alert alert-success rounded-3">{successMessage}</div>}

                            <div className="row">
                                <div className="col-md-6 mb-3">
                                    <label htmlFor="firstName" className="form-label">First Name</label>
                                    <input type="text" className="form-control rounded-3" id="firstName" value={formData.firstName} onChange={handleChange} required />
                                </div>
                                <div className="col-md-6 mb-3">
                                    <label htmlFor="lastName" className="form-label">Last Name</label>
                                    <input type="text" className="form-control rounded-3" id="lastName" value={formData.lastName} onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="mb-3">
                                <label htmlFor="email" className="form-label">Email address</label>
                                <input type="email" className="form-control rounded-3" id="email" value={formData.email} onChange={handleChange} required />
                            </div>

                            <div className="row">
                                <div className="col-md-6 mb-3">
                                    <label htmlFor="password" className="form-label">Password</label>
                                    <input type="password" className="form-control rounded-3" id="password" value={formData.password} onChange={handleChange} required />
                                    <div className="form-text text-muted">Password must be 8+ chars with letters, numbers, and special chars.</div>
                                </div>
                                <div className="col-md-6 mb-3">
                                    <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                                    <input type="password" className="form-control rounded-3" id="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required />
                                </div>
                            </div>

                            <div className="mb-3">
                                <label htmlFor="address" className="form-label">Address</label>
                                <textarea className="form-control rounded-3" id="address" rows="3" value={formData.address} onChange={handleChange} required></textarea>
                            </div>

                            <div className="mb-3">
                                <label htmlFor="aadharNumber" className="form-label">Aadhar Number</label>
                                <input type="text" className="form-control rounded-3" id="aadharNumber" value={formData.aadharNumber} onChange={handleChange} maxLength="12" minLength="12" required />
                                <div className="form-text text-muted">Aadhar must be 12 digits.</div>
                            </div>

                            <button type="submit" className="btn btn-success w-100 rounded-3 py-2 fw-bold" disabled={loading}>
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                ) : (
                                    'Register'
                                )}
                            </button>
                            <p className="text-center mt-3">
                                Already have an account? <Link to="/login" className="text-decoration-none fw-bold">Login here</Link>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;