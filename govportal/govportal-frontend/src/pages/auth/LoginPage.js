// src/pages/auth/LoginPage.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Import useAuth hook
import { unauthenticatedFetch } from '../../utils/api'; // Import API utility

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth(); // Get the login function from AuthContext
    const navigate = useNavigate();

    // Handle form submission for login
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission behavior
        setError(''); // Clear previous errors
        setLoading(true); // Set loading state to true

        try {
            // Make API call to your backend's login endpoint
            const data = await unauthenticatedFetch('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password }),
            });

            // If login is successful, update auth context and redirect
            login(data.user, data.accessToken);
            navigate('/dashboard'); // Redirect to dashboard
        } catch (err) {
            console.error('Login error:', err);
            // Handle specific error messages from backend based on your DTOs and exceptions
            if (err.message.includes('Invalid Username or Password')) {
                setError('Invalid email or password. Please try again.');
            } else if (err.message.includes('Account not verified')) {
                setError('Your account is not verified. Please check your email.');
            } else if (err.message.includes('Account is blocked')) {
                setError('Your account is blocked. Please contact support.');
            } else {
                setError('An unexpected error occurred during login. Please try again later.');
            }
        } finally {
            setLoading(false); // Reset loading state
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-lg p-4 rounded-4">
                        <h2 className="card-title text-center mb-4 text-primary fw-bold">Login to GovPortal</h2>
                        <form onSubmit={handleSubmit}>
                            {error && <div className="alert alert-danger rounded-3">{error}</div>}
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
                            </div>
                            <div className="mb-3">
                                <label htmlFor="passwordInput" className="form-label">Password</label>
                                <input
                                    type="password"
                                    className="form-control rounded-3"
                                    id="passwordInput"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary w-100 rounded-3 py-2 fw-bold" disabled={loading}>
                                {loading ? (
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                ) : (
                                    'Login'
                                )}
                            </button>
                            <p className="text-center mt-3 mb-1">
                                Don't have an account? <Link to="/register" className="text-decoration-none fw-bold">Register here</Link>
                            </p>
                            <p className="text-center">
                                <Link to="/forgot-password" className="text-decoration-none">Forgot Password?</Link>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
