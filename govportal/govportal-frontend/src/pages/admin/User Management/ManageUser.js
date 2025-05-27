// --- src/pages/Dashboard.js ---
import React from 'react';
import { Link , useNavigate} from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';

const ManageUser = () => {
    const { user, loading } = useAuth();
    const navigate = useNavigate();
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

    if (!user.role === 'ADMIN') {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-warning rounded-3" role="alert">
                    You are Not Authorized for Managing Verifiers
                </div>
                {/* <Link to="/login" className="btn btn-primary rounded-pill px-4 py-2">Go to Login</Link> */}
            </div>
        );
    }



    return (
        <div className="container mt-5">
            <div className="jumbotron bg-light p-5 rounded-4 shadow-sm text-center">
                <h1 className="display-4 text-primary mb-3">Welcome, {user.fullName || user.firstName}!</h1>
                <p className="lead fs-5">You are logged in as a <span className="badge bg-info text-dark">{user.role}</span>.</p>
                <hr className="my-4" />
                {/* <p className="mb-4">This is your personalized dashboard. Explore the options available based on your role.</p> */}

                {/* Role-specific content and navigation suggestions */}
                {user.role === 'ADMIN' && (
                    <div className="mt-4">
                        <h3 className="text-secondary">Admin Actions:</h3>
                        <div className="d-grid gap-2 col-md-6 mx-auto">
                            <Link to="/admin/userslist" className="btn btn-outline-primary btn-lg rounded-pill">See All Citizen Users</Link>
                            <Link to="/admin/userslist" className="btn btn-outline-primary btn-lg rounded-pill">Block/Unblock a Citizen Account</Link>
                            {/* <Link to="/admin/citizens" className="btn btn-outline-secondary btn-lg rounded-pill">Delete a Citizen Account</Link> */}
                            
                        </div>

                        <div className="text-center mt-4">
                            <button
                            className="btn btn-secondary rounded-pill px-4 py-2"
                            onClick={() => navigate("/dashboard")}
                            >
                            Back to Dashboard
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ManageUser;
