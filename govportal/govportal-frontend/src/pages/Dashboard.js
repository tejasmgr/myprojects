// --- src/pages/Dashboard.js ---
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Dashboard = () => {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-warning rounded-3" role="alert">
                    Please log in to view the dashboard.
                </div>
                <Link to="/login" className="btn btn-primary rounded-pill px-4 py-2">Go to Login</Link>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <div className="jumbotron bg-light p-5 rounded-4 shadow-sm text-center">
                <h1 className="display-4 text-primary mb-3">Welcome, {user.fullName || user.firstName}!</h1>
                <p className="lead fs-5">You are logged in as a <span className="badge bg-info text-dark">{user.role}</span>.</p>
                <hr className="my-4" />
                <p className="mb-4">This is your personalized dashboard. Explore the options available based on your role.</p>

                {/* Role-specific content and navigation suggestions */}
                {user.role === 'ADMIN' && (
                    <div className="mt-4">
                        <h3 className="text-secondary">Admin Actions:</h3>
                        <div className="d-grid gap-2 col-md-6 mx-auto">
                            <Link to="/admin/manage-verifiers" className="btn btn-outline-primary btn-lg rounded-pill">Manage Verifiers</Link>
                            <Link to="/admin/citizens" className="btn btn-outline-secondary btn-lg rounded-pill">View Citizens</Link>
                            <Link to="/admin/applications" className="btn btn-outline-info btn-lg rounded-pill">View All Applications</Link>
                            <Link to="/admin/stats" className="btn btn-outline-warning btn-lg rounded-pill">View System Stats</Link>
                        </div>
                    </div>
                )}
                {user.role === 'VERIFIER' && (
                    <div className="mt-4">
                        <h3 className="text-secondary">Verifier Tasks:</h3>
                        <div className="d-grid gap-2 col-md-6 mx-auto">
                            <Link to="/verifier/pending-applications" className="btn btn-outline-success btn-lg rounded-pill">Review Pending Applications</Link> {/* Updated link */}
                            <Link to="/verifier/approved-applications" className="btn btn-outline-info btn-lg rounded-pill">View Approved Applications</Link>
                            <Link to="/verifier/stats" className="btn btn-outline-warning btn-lg rounded-pill">View Verification Stats</Link>
                        </div>
                    </div>
                )}
                {user.role === 'CITIZEN' && (
                    <div className="mt-4">
                        <h3 className="text-secondary">Citizen Services:</h3>
                        <div className="d-grid gap-2 col-md-6 mx-auto">
                            <Link to="/citizen/submit-application" className="btn btn-outline-primary btn-lg rounded-pill">Submit New Application</Link>
                            <Link to="/citizen/my-applications" className="btn btn-outline-info btn-lg rounded-pill">View My Applications</Link>
                            <Link to="/profile" className="btn btn-outline-secondary btn-lg rounded-pill">Manage My Profile</Link>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Dashboard;
