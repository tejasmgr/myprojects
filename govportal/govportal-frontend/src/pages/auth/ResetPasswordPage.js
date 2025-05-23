// src/pages/auth/ResetPasswordPage.js
import React, { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom'; // Added Link import
import { unauthenticatedFetch } from '../../utils/api';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');
        setLoading(true);

        if (!token) {
            setError('Invalid reset link.');
            setLoading(false);
            return;
        }

        if (newPassword !== confirmNewPassword) {
            setError('Passwords do not match.');
            setLoading(false);
            return;
        }

        try {
            await unauthenticatedFetch('/auth/reset-password', {
                method: 'POST',
                body: JSON.stringify({ token, newPassword }),
            });
            setMessage('Your password has been reset successfully. You can now log in with your new password.');
            setTimeout(() => navigate('/login'), 3000);
        } catch (err) {
            console.error('Reset password error:', err);
            setError(err.message || 'Failed to reset password. Please try again or request a new link.');
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
                                    className="form-control rounded-3"
                                    id="newPasswordInput"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="confirmNewPasswordInput" className="form-label">Confirm New Password</label>
                                <input
                                    type="password"
                                    className="form-control rounded-3"
                                    id="confirmNewPasswordInput"
                                    value={confirmNewPassword}
                                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-info w-100 rounded-3 py-2 fw-bold" disabled={loading}>
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                ) : (
                                    'Reset Password'
                                )}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;
