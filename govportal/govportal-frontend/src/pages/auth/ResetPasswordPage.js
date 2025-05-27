// src/pages/auth/ResetPasswordPage.js
import React, { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { unauthenticatedFetch } from '../../utils/api';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;

    const validatePassword = (password) => {
        if (!password) {
            return 'New password is required';
        }
        if (!passwordRegex.test(password)) {
            return 'Password must be 8+ chars with letters, numbers, and special chars';
        }
        return '';
    };

    const handleNewPasswordChange = (e) => {
        const newPasswordValue = e.target.value;
        setNewPassword(newPasswordValue);
        setPasswordError(validatePassword(newPasswordValue));
    };

    const handleConfirmNewPasswordChange = (e) => {
        setConfirmNewPassword(e.target.value);
        if (e.target.value !== newPassword) {
            setConfirmPasswordError('Passwords do not match.');
        } else {
            setConfirmPasswordError('');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');
        setLoading(true);

        const newPasswordValidationError = validatePassword(newPassword);
        setPasswordError(newPasswordValidationError);

        if (newPassword !== confirmNewPassword) {
            setConfirmPasswordError('Passwords do not match.');
        } else {
            setConfirmPasswordError('');
        }

        if (!token) {
            setError('Invalid reset link.');
            setLoading(false);
            return;
        }

        if (newPasswordValidationError || newPassword !== confirmNewPassword) {
            setLoading(false);
            return;
        }

        try {
            const response = await unauthenticatedFetch(`/auth/reset-password?token=${token}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json', // Backend expects a plain string
                },
                body: newPassword, // Send only the new password as a string
            });

            if (response.ok) {
                setMessage('Your password has been reset successfully. You can now log in with your new password.');
                setTimeout(() => navigate('/login'), 3000);
            } else {
                const errorData = await response.text(); // Backend sends error as plain text
                setError(errorData || 'Failed to reset password. Please try again or request a new link.');
            }
        } catch (err) {
            console.error('Reset password error:', err);
            setError('Failed to connect to the server. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-lg p-4 rounded-4">
                        <h2 className="card-title text-center mb-4 text-info fw-bold">Reset Your Password</h2>
                        {error && <div className="alert alert-danger rounded-3">{error}</div>}
                        {message && <div className="alert alert-success rounded-3">{message}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="newPasswordInput" className="form-label">New Password</label>
                                <input
                                    type="password"
                                    className={`form-control rounded-3 ${passwordError ? 'is-invalid' : ''}`}
                                    id="newPasswordInput"
                                    value={newPassword}
                                    onChange={handleNewPasswordChange}
                                    required
                                />
                                {passwordError && <div className="invalid-feedback">{passwordError}</div>}
                                <div className="form-text text-muted">
                                    Must be 8+ characters with letters, numbers, and special characters.
                                </div>
                            </div>
                            <div className="mb-3">
                                <label htmlFor="confirmNewPasswordInput" className="form-label">Confirm New Password</label>
                                <input
                                    type="password"
                                    className={`form-control rounded-3 ${confirmPasswordError ? 'is-invalid' : ''}`}
                                    id="confirmNewPasswordInput"
                                    value={confirmNewPassword}
                                    onChange={handleConfirmNewPasswordChange}
                                    required
                                />
                                {confirmPasswordError && <div className="invalid-feedback">{confirmPasswordError}</div>}
                            </div>
                            <button
                                type="submit"
                                className="btn btn-info w-100 rounded-3 py-2 fw-bold"
                                disabled={loading || passwordError || confirmPasswordError || !newPassword || !confirmNewPassword}
                            >
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                ) : (
                                    'Reset Password'
                                )}
                            </button>
                        </form>
                        <div className="mt-3 text-center">
                            <Link to="/login" className="text-muted">Back to Login</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;