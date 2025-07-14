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
import CitizensApprovedApplications from './pages/citizen/CitizensApprovedApplications';
import CitizenApplicationDetails from './pages/citizen/CitizenApplicationDetails';
import AdminVerifierList from './pages/admin/Verifier Management/AdminVerifierList';
import ManageVerifier from './pages/admin/Verifier Management/ManageVerifier';
import UserDetails from './pages/admin/AdminUserDetails';
import AdminUsersList from './pages/admin/User Management/AdminUserList';
import ManageUser from './pages/admin/User Management/ManageUser';
import CreateVerifier from './pages/admin/Verifier Management/CreateVerifier';
import ViewAllApplications from './pages/admin/Application Management/ViewAllApplications';
import ManageApplications from './pages/admin/Application Management/ManageApplications';
import ViewAllApprovedApplications from './pages/admin/Application Management/ViewAllApprovedApplications';
import ViewAllPendingApplications from './pages/admin/Application Management/ViewAllPendingApplications';
import ApplicationOnDesk1 from './pages/admin/Application Management/ApplicationsOnDesk1';
import ApplicationOnDesk2 from './pages/admin/Application Management/ApplicationsOnDesk2';
import UserProfile from './pages/UserProfile';
import ChangePasswordPage from './pages/ChangePasswordPage';
import ManageUserProfile from './pages/ManageUserProfile';
import AdminSystemStats from './pages/admin/AdminSystemStats';
import HomePage from './pages/HomePage';
import AboutUsPage from './pages/AboutUsPage';



const App = () => {
    return (
        <Router>
            <AuthProvider>
                <Navbar />
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/home" element={<HomePage />} />
                    <Route path="/about-us" element={<AboutUsPage />} />

                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/profile" element={<UserProfile />} />
                    <Route path="/profile/change-password-page" element={<ChangePasswordPage />} />
                    <Route path="/manage-profile" element={<ManageUserProfile />} />


                    


                    {/* Authentication Routes */}
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                    <Route path="/verify-email" element={<VerifyEmailPage />} />

                    {/* Citizen Routes */}
                    <Route path="/citizen/submit-application" element={<SubmitApplication />} />
                    <Route path="/citizen/my-applications" element={<MyApplications />} />
                    <Route path = "/citizen/approved-applications" element={< CitizensApprovedApplications />}/>
                    <Route path="/citizen/applications/details/:id" element={<CitizenApplicationDetails />} />
                   

                    {/* Verifier Routes */}
                    <Route path="/verifier/pending-applications" element={<PendingApplications />} />
                    <Route path="/verifier/approved-applications" element={<ApprovedApplications />} />
                    {/* New route for application details */}
                    <Route path="/verifier/applications/:id" element={<ApplicationDetails />} />

                    {/* Admin Routes */}
                    <Route path="/admin/verifiers" element={<AdminVerifierList />} />

                    <Route path="/admin/userslist" element={<AdminUsersList/>}/>

                    <Route path="/admin/manage-verifiers" element={<ManageVerifier/>}/>

                    <Route path="/admin/user/details/:id" element={<UserDetails/>}/>

                    <Route path="/admin/manage-user" element={<ManageUser/>}/>
                    <Route path="/admin/create-verifier" element={<CreateVerifier/>} />

                    <Route path="/admin/manage-applications" element={<ManageApplications/>}/>

                    <Route path="/admin/view-all-applications" element={<ViewAllApplications/>}/>
                    <Route path="/admin/view-all-approved-applications" element={<ViewAllApprovedApplications/>}/>

                    <Route path="/admin/view-all-pending-applications" element={<ViewAllPendingApplications/>}/>
                    <Route path="/admin/view-all-desk1-applications" element={<ApplicationOnDesk1/>}/>
                    <Route path="/admin/view-all-desk2-applications" element={<ApplicationOnDesk2/>}/>
                    <Route path="/admin/system-stats" element={<AdminSystemStats/>}/>






                    {/* Add more role-specific routes here as you build them out */}
                </Routes>
            </AuthProvider>
        </Router>
    );
};

export default App;
