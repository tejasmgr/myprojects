// src/pages/auth/ForgotPasswordPage.js
import React, { useState } from 'react';
import { Link , useNavigate} from 'react-router-dom';
import { unauthenticatedFetch } from '../../utils/api';

const ForgotPasswordPage = () => {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');
        setLoading(true);

        try {
            await unauthenticatedFetch(`/auth/forgot-password?email=${email}`, {
                method: 'POST'
                
            });
            setMessage('A password reset link has been sent to your email address.');
        } catch (err) {
            console.error('Forgot password error:', err);
            setError(err.message || 'Failed to send reset link. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-lg p-4 rounded-4">
                        <h2 className="card-title text-center mb-4 text-warning fw-bold">Forgot Your Password?</h2>
                        {error && <div className="alert alert-danger rounded-3">{error}</div>}
                        {message && <div className="alert alert-success rounded-3">{message}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="emailInput" className="form-label">Email address</label>
                                <input
                                    type="email"
                                    className="form-control rounded-3"
                                    id="emailInput"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    aria-describedby="emailHelp"
                                />
                                <div id="emailHelp" className="form-text text-muted">Enter your registered email address.</div>
                            </div>
                            <button type="submit" className="btn btn-warning w-100 rounded-3 py-2 fw-bold" disabled={loading}>
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                ) : (
                                    'Send Reset Link'
                                )}
                            </button>
                            <p className="text-center mt-3">
                                Remember your password? <Link to="/login" className="text-decoration-none fw-bold">Login here</Link>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;
