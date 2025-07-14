// src/pages/HomePage.js
import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Import useNavigate
import { useAuth } from '../contexts/AuthContext'; // Import useAuth to check login status

const HomePage = () => {
    const { user, loading } = useAuth(); // Get user and loading state from AuthContext
    const navigate = useNavigate(); // Initialize navigate hook

    // Function to handle clicks on service buttons
    const handleServiceClick = (path) => {
        if (user) {
            // If user is logged in, navigate directly
            navigate(path);
        } else {
            // If not logged in, store intended path and redirect to login
            localStorage.setItem('intendedPath', path); // Store the path
            navigate('/login', { state: { message: 'Please login to view this service.' } }); // Pass message via state
        }
    };

    // Show a loading spinner if authentication state is still being determined
    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center vh-100">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    return (
        <div className="container-fluid p-0">
            {/* Hero Section */}
            <div className="bg-primary text-white text-center py-5 shadow-sm rounded-bottom-4">
                <div className="container">
                    <h1 className="display-3 fw-bold mb-3">Welcome to GovPortal</h1>
                    <p className="lead fs-4 mb-4">Your streamlined platform for government document applications and verification.</p>
                    {user ? (
                        <Link to="/dashboard" className="btn btn-light btn-lg rounded-pill px-5 py-3 fw-bold shadow-sm">
                            Go to Dashboard
                        </Link>
                    ) : (
                        <div className="d-flex justify-content-center gap-3">
                            <Link to="/login" className="btn btn-light btn-lg rounded-pill px-5 py-3 fw-bold shadow-sm">
                                Login
                            </Link>
                            <Link to="/register" className="btn btn-outline-light btn-lg rounded-pill px-5 py-3 fw-bold shadow-sm">
                                Register
                            </Link>
                        </div>
                    )}
                    <div className="mt-4">
                        <Link to="/about-us" className="btn btn-info btn-lg rounded-pill px-5 py-3 fw-bold shadow-sm">
                            Learn More About Us
                        </Link>
                    </div>
                </div>
            </div>

            {/* Carousel Section */}
            <div id="carouselExampleAutoplaying" className="carousel slide carousel-fade" data-bs-ride="carousel" data-bs-interval="3000">
                <div className="carousel-inner">
                    <div className="carousel-item active">
                        <img src="https://placehold.co/1920x600/6610f2/ffffff?text=Digital+Gov+Services" className="d-block w-100 img-fluid" alt="Digital Government Services" />
                        <div className="carousel-caption d-none d-md-block bg-dark bg-opacity-50 rounded-3 p-3">
                            <h5 className="fw-bold">Streamlined Digital Services</h5>
                            <p>Access government services from the comfort of your home.</p>
                        </div>
                    </div>
                    <div className="carousel-item">
                        <img src="https://placehold.co/1920x600/20c997/ffffff?text=Secure+and+Transparent" className="d-block w-100 img-fluid" alt="Secure and Transparent Process" />
                        <div className="carousel-caption d-none d-md-block bg-dark bg-opacity-50 rounded-3 p-3">
                            <h5 className="fw-bold">Secure & Transparent Process</h5>
                            <p>Your data is safe, and every step is trackable.</p>
                        </div>
                    </div>
                    <div className="carousel-item">
                        <img src="https://placehold.co/1920x600/0dcaf0/ffffff?text=Fast+Verification" className="d-block w-100 img-fluid" alt="Fast Verification" />
                        <div className="carousel-caption d-none d-md-block bg-dark bg-opacity-50 rounded-3 p-3">
                            <h5 className="fw-bold">Efficient Verification</h5>
                            <p>Quick and reliable document verification by dedicated verifiers.</p>
                        </div>
                    </div>
                </div>
                <button className="carousel-control-prev" type="button" data-bs-target="#carouselExampleAutoplaying" data-bs-slide="prev">
                    <span className="carousel-control-prev-icon" aria-hidden="true"></span>
                    <span className="visually-hidden">Previous</span>
                </button>
                <button className="carousel-control-next" type="button" data-bs-target="#carouselExampleAutoplaying" data-bs-slide="next">
                    <span className="carousel-control-next-icon" aria-hidden="true"></span>
                    <span className="visually-hidden">Next</span>
                </button>
            </div>


            {/* Features Section */}
            <div className="container my-5">
                <h2 className="text-center text-primary fw-bold mb-5">Our Services</h2>
                <div className="row g-4">
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4 text-center">
                            <div className="card-body">
                                <i className="bi bi-file-earmark-text display-4 text-success mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Easy Application Submission</h5>
                                <p className="card-text text-muted">Apply for various government certificates online with a simple and intuitive process.</p>
                                {/* Changed to button with onClick handler */}
                                <button
                                    onClick={() => handleServiceClick('/citizen/submit-application')}
                                    className="btn btn-outline-success rounded-pill mt-3"
                                >
                                    Apply Now
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4 text-center">
                            <div className="card-body">
                                <i className="bi bi-search display-4 text-info mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Track Application Status</h5>
                                <p className="card-text text-muted">Monitor the real-time status of your submitted applications from anywhere, anytime.</p>
                                {/* Changed to button with onClick handler */}
                                <button
                                    onClick={() => handleServiceClick('/citizen/my-applications')}
                                    className="btn btn-outline-info rounded-pill mt-3"
                                >
                                    Track Status
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4 text-center">
                            <div className="card-body">
                                <i className="bi bi-shield-check display-4 text-warning mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Secure Verification Process</h5>
                                <p className="card-text text-muted">Our dedicated verifiers ensure a secure and efficient review of all applications.</p>
                                {/* Conditional rendering for role-specific buttons or login prompt */}
                                {user && user.role === 'VERIFIER' && (
                                    <Link to="/verifier/pending-applications" className="btn btn-outline-warning rounded-pill mt-3">Verifier Portal</Link>
                                )}
                                {user && user.role === 'ADMIN' && (
                                    <Link to="/admin/stats" className="btn btn-outline-warning rounded-pill mt-3">Admin Dashboard</Link>
                                )}
                                {!user && (
                                    <button
                                        onClick={() => handleServiceClick('/login')} // Redirect to login, no specific service after
                                        className="btn btn-outline-warning rounded-pill mt-3"
                                    >
                                        Learn More
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* About Section */}
            <div className="bg-light py-5">
                <div className="container">
                    <div className="row align-items-center">
                        <div className="col-md-6 mb-4 mb-md-0">
                            <img
                                src="https://placehold.co/600x400/E0F2F7/007BFF?text=Secure+Digital+Platform"
                                alt="Secure Digital Platform"
                                className="img-fluid rounded-4 shadow"
                            />
                        </div>
                        <div className="col-md-6">
                            <h2 className="text-primary fw-bold mb-3">About GovPortal</h2>
                            <p className="text-muted fs-5">
                                GovPortal is committed to simplifying government services for citizens. We aim to provide a transparent, efficient, and secure platform for all your document-related needs. Our mission is to bridge the gap between citizens and government services through technology.
                            </p>
                            <p className="text-muted">
                                From submitting applications to tracking their progress and receiving official certificates, GovPortal offers a seamless experience.
                            </p>
                            <Link to="/register" className="btn btn-primary rounded-pill px-4 py-2 mt-3">Join Us Today</Link>
                        </div>
                    </div>
                </div>
            </div>

            {/* Footer Placeholder (You might have a separate Footer component) */}
            <footer className="bg-dark text-white text-center py-3 mt-5">
                <div className="container">
                    <p className="mb-0">&copy; {new Date().getFullYear()} GovPortal. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default HomePage;
