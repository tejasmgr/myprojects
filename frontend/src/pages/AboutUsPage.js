// src/pages/AboutUsPage.js
import React from 'react';
import { Link } from 'react-router-dom';

const AboutUsPage = () => {
    return (
        <div className="container-fluid p-0">
            {/* Hero Section */}
            <div className="bg-info text-white text-center py-5 shadow-sm rounded-bottom-4">
                <div className="container">
                    <h1 className="display-3 fw-bold mb-3">About GovPortal</h1>
                    <p className="lead fs-4 mb-4">
                        Revolutionizing government services through secure and accessible digital solutions.
                    </p>
                </div>
            </div>

            {/* Mission Section */}
            <div className="container my-5">
                <div className="row align-items-center">
                    <div className="col-md-6 mb-4 mb-md-0">
                        <img
                            src="https://placehold.co/600x400/D1ECF1/0C5460?text=Our+Mission"
                            alt="Our Mission"
                            className="img-fluid rounded-4 shadow-sm"
                        />
                    </div>
                    <div className="col-md-6">
                        <h2 className="text-info fw-bold mb-3">Our Mission</h2>
                        <p className="text-muted fs-5">
                            Our mission at GovPortal is to empower citizens by providing a seamless, transparent, and efficient digital platform for accessing essential government services. We strive to simplify complex bureaucratic processes, ensuring that every citizen can apply for, track, and receive official documents with ease and confidence.
                        </p>
                        <p className="text-muted">
                            We are dedicated to leveraging technology to foster a more accessible and responsive governance, reducing wait times and enhancing public convenience.
                        </p>
                    </div>
                </div>
            </div>

            {/* Vision Section */}
            <div className="bg-light py-5">
                <div className="container">
                    <div className="row align-items-center flex-row-reverse"> {/* Reverse order for image on right */}
                        <div className="col-md-6 mb-4 mb-md-0">
                            <img
                                src="https://placehold.co/600x400/CCE5FF/004085?text=Our+Vision"
                                alt="Our Vision"
                                className="img-fluid rounded-4 shadow-sm"
                            />
                        </div>
                        <div className="col-md-6">
                            <h2 className="text-primary fw-bold mb-3">Our Vision</h2>
                            <p className="text-muted fs-5">
                                We envision a future where government services are universally accessible, digital-first, and citizen-centric. GovPortal aims to be the leading platform for digital governance, setting new standards for efficiency, security, and user experience in public service delivery.
                            </p>
                            <p className="text-muted">
                                By continuously innovating and expanding our offerings, we aspire to build a more connected and digitally empowered society where every interaction with government is simple, swift, and secure.
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Values Section */}
            <div className="container my-5">
                <h2 className="text-center text-success fw-bold mb-5">Our Core Values</h2>
                <div className="row g-4 text-center">
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4">
                            <div className="card-body">
                                <i className="bi bi-person-check display-4 text-success mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Citizen-Centricity</h5>
                                <p className="card-text text-muted">
                                    Placing the needs and convenience of citizens at the forefront of every decision and development.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4">
                            <div className="card-body">
                                <i className="bi bi-shield-lock display-4 text-danger mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Security & Trust</h5>
                                <p className="card-text text-muted">
                                    Ensuring the highest standards of data security and privacy to build unwavering trust with our users.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm rounded-4 p-4">
                            <div className="card-body">
                                <i className="bi bi-lightbulb display-4 text-warning mb-3"></i>
                                <h5 className="card-title fw-bold mb-3">Innovation</h5>
                                <p className="card-text text-muted">
                                    Continuously seeking new and better ways to improve service delivery and user experience through technology.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Call to Action Section */}
            <div className="bg-primary text-white text-center py-5">
                <div className="container">
                    <h2 className="display-4 fw-bold mb-3">Join the Digital Transformation</h2>
                    <p className="lead fs-5 mb-4">
                        Experience the ease and efficiency of modern government services.
                    </p>
                    <Link to="/register" className="btn btn-light btn-lg rounded-pill px-5 py-3 fw-bold shadow-sm">
                        Get Started Today
                    </Link>
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

export default AboutUsPage;
