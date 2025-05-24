// --- src/components/Navbar.js ---
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
            <div className="container-fluid">
                <Link className="navbar-brand fw-bold" to="/">GovPortal</Link>
                <button
                    className="navbar-toggler"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#navbarNav"
                    aria-controls="navbarNav"
                    aria-expanded="false"
                    aria-label="Toggle navigation"
                >
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                        {user ? (
                            <>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/dashboard">Dashboard</Link>
                                </li>
                                {/* Citizen-specific links */}
                                {user.role === 'CITIZEN' && (
                                    <>
                                        <li className="nav-item">
                                            <Link className="nav-link" to="/citizen/submit-application">Submit Application</Link>
                                        </li>
                                        <li className="nav-item">
                                            <Link className="nav-link" to="/citizen/my-applications">My Applications</Link>
                                        </li>
                                        <li className="nav-item">
                                            <Link className="nav-link" to="/citizen/approved-applications">My Approved Applications</Link>
                                        </li>
                                    </>
                                )}
                                {/* Verifier-specific link */}
                                {(user.role === 'VERIFIER' || user.role === 'ADMIN') && (
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/verifier/pending-applications">Pending Applications</Link> {/* New link */}
                                    </li>
                                )}
                                {/* Verifier-specific link */}
                                {(user.role === 'VERIFIER' || user.role === 'ADMIN') && (
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/verifier/approved-applications">Approved Applications</Link> {/* New link */}
                                    </li>
                                )}


                                {/* Admin-specific link (placeholder for now) */}
                                {user.role === 'ADMIN' && (
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/admin/manage-verifiers">Manage Verifiers</Link>
                                    </li>
                                )}
                            </>
                        ) : (
                            <>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/login">Login</Link>
                                </li>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/register">Register</Link>
                                </li>
                            </>
                        )}
                    </ul>
                    {user && (
                        <div className="d-flex align-items-center">
                            <span className="navbar-text text-white me-3">
                                Welcome, <span className="fw-bold">{user.fullName || user.firstName}</span> (<span className="text-warning">{user.role}</span>)
                            </span>
                            <button className="btn btn-outline-light rounded-pill px-3" onClick={handleLogout}>
                                Logout
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
