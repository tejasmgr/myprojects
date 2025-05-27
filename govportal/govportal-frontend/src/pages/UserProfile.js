// src/pages/user/UserProfile.js
import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authenticatedFetch } from '../utils/api'; 
import { useNavigate } from 'react-router-dom';

const UserProfile = () => {
    const { token } = useAuth();
    const [userProfile, setUserProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isEditing, setIsEditing] = useState(false);
    const [updatedProfileData, setUpdatedProfileData] = useState({});
    const [updateSuccessMessage, setUpdateSuccessMessage] = useState('');
    const [updateErrorMessage, setUpdateErrorMessage] = useState('');

    const[updateSuccessModal, setUpdateSuccessModal] = useState(false);

    const navigate = useNavigate();

    const fetchUserProfile = async () => {
            setLoading(true);
            setError('');
            try {
                // Backend GET endpoint for current user profile
                const response = await authenticatedFetch('/users/profile', {
                    method: 'GET',
                });
                // Assuming authenticatedFetch returns the parsed JSON directly on success
                const data = response;

                if (!data) {
                    throw new Error('Empty response received from server.');
                }

                setUserProfile(data);
                // Initialize editing form data with current profile details
                // Note: fullName is derived, so we use firstName/lastName for editing
                setUpdatedProfileData({
                    firstName: data.fullName ? data.fullName.split(' ')[0] : '', // Assuming first word is first name
                    lastName: data.fullName ? data.fullName.split(' ').slice(1).join(' ') : '', // Remaining words are last name
                    email: data.email || '', // Email is typically not editable via this form due to verification flows
                    address: data.address || '',
                    dateOfBirth: data.dateOfBirth || '', // Format might need adjustment for input type="date"
                    gender: data.gender || '',
                    fatherName: data.fatherName || '', // Assuming fatherName exists in UserResponse
                    aadharNumber: data.aadharNumber || '',
                });
            } catch (err) {
                console.error('Error fetching profile:', err);
                setError(err.message || 'Failed to fetch profile data. Please try again.');
            } finally {
                setLoading(false);
            }
        };

    useEffect(() => {
        if (token) {
            fetchUserProfile();
        }
    }, [token]);

    const handleEditClick = () => {
        setIsEditing(true);
        setUpdateSuccessMessage('');
        setUpdateErrorMessage('');
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUpdatedProfileData(prev => ({ ...prev, [name]: value }));
    };

    const handleUpdateProfile = async () => {
        setLoading(true);
        setUpdateSuccessMessage('');
        setUpdateErrorMessage('');

        // Prepare data for UserUpdateRequest DTO
        const requestBody = {
            firstName: updatedProfileData.firstName,
            lastName: updatedProfileData.lastName,
            address: updatedProfileData.address,
            dateOfBirth: updatedProfileData.dateOfBirth, // Ensure this matches LocalDate format (YYYY-MM-DD)
            gender: updatedProfileData.gender,
            fatherName: updatedProfileData.fatherName,
            aadharNumber: updatedProfileData.aadharNumber,
        };

        try {
            // Backend PUT endpoint for updating user profile
            const response = await authenticatedFetch('/users/profile/update', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody),
            });

            // Assuming authenticatedFetch returns the parsed JSON (UserResponse) directly on success
            const updatedData = response;

            if (!updatedData) {
                throw new Error('Empty response received from server after update.');
            }

            // --- START FIX: Ensure fullName is correctly set for display ---
            // Reconstruct fullName from firstName and lastName if backend doesn't provide it
            if (!updatedData.fullName && (updatedProfileData.firstName || updatedProfileData.lastName)) {
                updatedData.fullName = `${updatedProfileData.firstName || ''} ${updatedProfileData.lastName || ''}`.trim();
            } else if (!updatedData.fullName) {
                updatedData.fullName = 'N/A'; // Fallback if no name parts are available
            }
            // --- END FIX ---

            setUserProfile(updatedData);
            // Re-initialize editing form data with the newly updated profile details
            setUpdatedProfileData({
                firstName: updatedData.fullName ? updatedData.fullName.split(' ')[0] : '',
                lastName: updatedData.fullName ? updatedData.fullName.split(' ').slice(1).join(' ') : '',
                email: updatedData.email || '',
                address: updatedData.address || '',
                dateOfBirth: updatedData.dateOfBirth || '',
                gender: updatedData.gender || '',
                fatherName: updatedData.fatherName || '',
                aadharNumber: updatedData.aadharNumber || '',
            });
            setUpdateSuccessMessage('Profile updated successfully!');
            setIsEditing(false);
            

        } catch (err) {
            console.error('Error updating profile:', err);
            // Attempt to parse error message from backend if available
            let errorMessage = 'Failed to update profile. Please try again.';
            try {
                // If authenticatedFetch returns raw Response on error, parse it
                const errorResponse = err.message; // Assuming err.message might contain the raw error response text
                if (typeof errorResponse === 'string' && errorResponse.includes('{')) {
                    const parsedError = JSON.parse(errorResponse);
                    errorMessage = parsedError.message || errorMessage;
                }
            } catch (parseError) {
                // Fallback to generic message if error response is not JSON
            }
            setUpdateErrorMessage(errorMessage);
        } finally {
            setLoading(false);
            setUpdateSuccessModal(true);
            // setTimeout(() => {
            //     setUpdateSuccessModal(false)
            // }, 3000);
            
        }
    };

    const handleCancelEdit = () => {
        // Revert changes in the form to the original userProfile data
        setUpdatedProfileData({
            firstName: userProfile.fullName ? userProfile.fullName.split(' ')[0] : '',
            lastName: userProfile.fullName ? userProfile.fullName.split(' ').slice(1).join(' ') : '',
            email: userProfile.email || '',
            address: userProfile.address || '',
            dateOfBirth: userProfile.dateOfBirth || '',
            gender: userProfile.gender || '',
            fatherName: userProfile.fatherName || '',
            aadharNumber: userProfile.aadharNumber || '',
        });
        setIsEditing(false);
        setUpdateSuccessMessage('');
        setUpdateErrorMessage('');
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center vh-100">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading profile...</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-danger rounded-3" role="alert">
                    {error}
                </div>
            </div>
        );
    }

    if (!userProfile) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-warning rounded-3" role="alert">
                    No profile information available.
                </div>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <div className="card shadow-lg p-4 rounded-4">
                <div className="card-body">
                    <h2 className="card-title text-center mb-4 text-primary">Your Profile</h2>

                    {updateSuccessMessage && <div className="alert alert-success">{updateSuccessMessage}</div>}
                    {updateErrorMessage && <div className="alert alert-danger">{updateErrorMessage}</div>}

                    {!isEditing ? (
                        <div className="profile-details">
                            <p><strong>Full Name:</strong> {userProfile.fullName || 'N/A'}</p>
                            <p><strong>Email:</strong> {userProfile.email || 'N/A'}</p>
                            <p><strong>Role:</strong> {userProfile.role || 'N/A'}</p>
                            <p><strong>Address:</strong> {userProfile.address || 'N/A'}</p>
                            <p><strong>Date of Birth:</strong> {userProfile.dateOfBirth || 'N/A'}</p>
                            <p><strong>Gender:</strong> {userProfile.gender || 'N/A'}</p>
                            <p><strong>Father's Name:</strong> {userProfile.fatherName || 'N/A'}</p>
                            <p><strong>Aadhar Number:</strong> {userProfile.aadharNumber || 'N/A'}</p>
                            <p><strong>Registered On:</strong> {userProfile.createdAt ? new Date(userProfile.createdAt).toLocaleString() : 'N/A'}</p>
                            {userProfile.updatedAt && <p><strong>Last Updated:</strong> {new Date(userProfile.updatedAt).toLocaleString()}</p>}

                            <div className="d-grid gap-2 mt-4">
                                <button className="btn btn-primary btn-lg rounded-pill" onClick={handleEditClick}>
                                    Update Profile
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="profile-edit-form">
                            <h3 className="text-center mb-4 text-secondary">Edit Profile</h3>
                            <form>
                                <div className="mb-3">
                                    <label htmlFor="firstName" className="form-label">First Name:</label>
                                    <input
                                        type="text"
                                        id="firstName"
                                        name="firstName"
                                        value={updatedProfileData.firstName || ''}
                                        onChange={handleInputChange}
                                        className="form-control rounded-pill"
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="lastName" className="form-label">Last Name:</label>
                                    <input
                                        type="text"
                                        id="lastName"
                                        name="lastName"
                                        value={updatedProfileData.lastName || ''}
                                        onChange={handleInputChange}
                                        className="form-control rounded-pill"
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="email" className="form-label">Email:</label>
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        value={updatedProfileData.email || ''}
                                        className="form-control rounded-pill"
                                        readOnly // Email is typically not editable via this form
                                        disabled // Make it visually disabled too
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="address" className="form-label">Address:</label>
                                    <textarea
                                        id="address"
                                        name="address"
                                        value={updatedProfileData.address || ''}
                                        onChange={handleInputChange}
                                        className="form-control rounded-3"
                                        rows="3"
                                        required
                                    ></textarea>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="dateOfBirth" className="form-label">Date of Birth:</label>
                                    <input
                                        type="date"
                                        id="dateOfBirth"
                                        name="dateOfBirth"
                                        value={updatedProfileData.dateOfBirth || ''}
                                        onChange={handleInputChange}
                                        className="form-control rounded-pill"
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="gender" className="form-label">Gender:</label>
                                    <select
                                        id="gender"
                                        name="gender"
                                        value={updatedProfileData.gender || ''}
                                        onChange={handleInputChange}
                                        className="form-select rounded-pill"
                                    >
                                        <option value="">Select Gender</option>
                                        <option value="MALE">Male</option>
                                        <option value="FEMALE">Female</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="fatherName" className="form-label">Father's Name:</label>
                                    <input
                                        type="text"
                                        id="fatherName"
                                        name="fatherName"
                                        value={updatedProfileData.fatherName || ''}
                                        onChange={handleInputChange}
                                        className="form-control rounded-pill"
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="aadharNumber" className="form-label">Aadhar Number:</label>
                                    <input
                                        type="text"
                                        id="aadharNumber"
                                        name="aadharNumber"
                                        value={updatedProfileData.aadharNumber || ''}
                                        className="form-control rounded-pill"
                                        readOnly // Aadhar number is typically not editable by the user
                                        disabled // Make it visually disabled too
                                    />
                                </div>

                                <div className="d-flex justify-content-end gap-2 mt-4">
                                    <button
                                        type="button"
                                        className="btn btn-success btn-lg rounded-pill px-4"
                                        onClick={handleUpdateProfile}
                                        disabled={loading}
                                    >
                                        {loading ? (
                                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                        ) : (
                                            'Save Changes'
                                        )}
                                    </button>
                                    <button
                                        type="button"
                                        className="btn btn-secondary btn-lg rounded-pill px-4"
                                        onClick={handleCancelEdit}
                                        disabled={loading}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}
                </div>
            </div>



{updateSuccessModal && (
        <div
          className="modal fade show d-block"
          tabIndex="-1"
          role="dialog"
         
        >
          <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title text-success">Profile Update Successful!</h5>
            </div>
            <div className="modal-body">
              <p>{updateSuccessMessage}</p>
              {/* <p>You will be redirected to the Verifiers list.</p> */}
            </div>
            <div className="modal-footer">
              <button
                className="btn btn-success rounded-pill"
                onClick={() => {
                  setUpdateSuccessModal(false);
                  // Optionally, you might want to refresh the list again here
                  fetchUserProfile();
                }}
              >
                OK
              </button>
            </div>
          </div>
        </div></div>
      )}











        </div>
    );
};

export default UserProfile;
