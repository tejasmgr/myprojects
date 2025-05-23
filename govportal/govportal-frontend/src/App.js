// --- src/App.js ---
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Navbar from './components/Navbar';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import Dashboard from './pages/Dashboard';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';
import VerifyEmailPage from './pages/auth/VerifyEmailPage';
import SubmitApplication from './pages/citizen/SubmitApplication';
import MyApplications from './pages/citizen/MyApplications';
import PendingApplications from './pages/verifier/PendingApplications';
import ApprovedApplications from './pages/verifier/ApprovedApplications';
import ApplicationDetails from './pages/verifier/ApplicationDetails'; // Import the new component

const App = () => {
    return (
        <Router>
            <AuthProvider>
                <Navbar />
                <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/dashboard" element={<Dashboard />} />

                    {/* Authentication Routes */}
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                    <Route path="/verify-email" element={<VerifyEmailPage />} />

                    {/* Citizen Routes */}
                    <Route path="/citizen/submit-application" element={<SubmitApplication />} />
                    <Route path="/citizen/my-applications" element={<MyApplications />} />

                    {/* Verifier Routes */}
                    <Route path="/verifier/pending-applications" element={<PendingApplications />} />
                    <Route path="/verifier/approved-applications" element={<ApprovedApplications />} />
                    {/* New route for application details */}
                    <Route path="/verifier/applications/:id" element={<ApplicationDetails />} />

                    {/* Add more role-specific routes here as you build them out */}
                </Routes>
            </AuthProvider>
        </Router>
    );
};

export default App;
