import React, { useState } from 'react';
import { authenticatedFetch } from '../../../utils/api'; // Adjust path if your api.js is elsewhere
import { useNavigate } from 'react-router-dom';

/**
 * CreateVerifierPage Component
 *
 * This component provides a form for an administrator to create new verifier accounts.
 * It includes client-side validation, handles API calls, and displays a success modal
 * upon successful account creation.
 */
const CreateVerifierPage = () => {
  const navigate = useNavigate();
  // State to manage form input values
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    designation: '', // Stores the selected designation
    address: '',
    dateOfBirth: '', // Format YYYY-MM-DD for HTML date input
    gender: '',
    aadharNumber: '',
  });

  // State to manage validation errors for each form field
  const [errors, setErrors] = useState({});
  // State to manage loading status during API calls
  const [loading, setLoading] = useState(false);
  // State to control the visibility of the success modal
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  // State to store and display API-related error messages
  const [apiError, setApiError] = useState('');

  // Predefined lists for dropdowns
  const designations = ['JUNIOR_VERIFIER', 'SENIOR_VERIFIER'];
  const genders = ['Male', 'Female', 'Other'];

  /**
   * Handles changes to form input fields.
   * Updates the formData state and clears any existing validation error for the changed field.
   * @param {Object} e - The event object from the input change.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    // Clear error for the field as the user types
    if (errors[name]) {
      setErrors((prevErrors) => ({ ...prevErrors, [name]: '' }));
    }
  };

  /**
   * Performs client-side validation of the form data.
   * Checks each field against predefined rules (e.g., not blank, email format, password regex).
   * Updates the errors state and returns true if the form is valid, false otherwise.
   * @returns {boolean} - True if the form is valid, false otherwise.
   */
  const validateForm = () => {
    let newErrors = {};
    let isValid = true;

    // Full Name validation: required and should ideally contain at least two parts
    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Full name is required';
      isValid = false;
    } else if (formData.fullName.trim().split(' ').length < 2) {
      newErrors.fullName = 'Please enter both first and last name';
      isValid = false;
    }

    // Email validation: required and must be a valid email format
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
      isValid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
      isValid = false;
    }

    // Password validation: required and must match the specified regex pattern
    // Pattern: 8+ chars, at least one letter, one number, and one special character
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!formData.password) {
      newErrors.password = 'Password is required';
      isValid = false;
    } else if (!passwordRegex.test(formData.password)) {
      newErrors.password = 'Password must be 8+ chars with letters, numbers, and special chars';
      isValid = false;
    }

    // Designation validation: required
    if (!formData.designation) {
      newErrors.designation = 'Designation is required';
      isValid = false;
    }

    // Date of Birth validation: optional, but if provided, check for valid date format
    if (formData.dateOfBirth && isNaN(new Date(formData.dateOfBirth).getTime())) {
      newErrors.dateOfBirth = 'Invalid date format';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  /**
   * Handles the form submission.
   * Prevents default form submission, performs validation, calls the backend API,
   * and handles success/error states.
   * @param {Object} e - The event object from the form submission.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(''); // Clear any previous API errors

    // Perform client-side validation
    if (!validateForm()) {
      return; // Stop submission if validation fails
    }

    setLoading(true); // Set loading state to true during API call
    try {
      // Prepare payload for the backend.
      // dateOfBirth should be null if the input is empty, as backend expects LocalDate.
      const payload = {
        ...formData,
        dateOfBirth: formData.dateOfBirth ? formData.dateOfBirth : null,
      };

      // Call the backend API using the authenticatedFetch utility
      await authenticatedFetch('/admin/create-verifier', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      setShowSuccessModal(true); // Show success modal on successful creation
      // Clear the form and errors after successful submission
      setFormData({
        fullName: '',
        email: '',
        password: '',
        designation: '',
        address: '',
        dateOfBirth: '',
        gender: '',
        aadharNumber: '',
      });
      setErrors({});
    } catch (error) {
      console.error('Error creating verifier:', error);
      // Handle specific backend error messages or HTTP status codes
      if (error.message.includes('Email already registered') || error.message.includes('HTTP error! status: 409')) {
        setApiError('Email already registered. Please use a different email.');
      } else if (error.message.includes('HTTP error! status: 500')) {
        setApiError('A server error occurred. Please try again later.');
      } else {
        setApiError(error.message || 'Failed to create verifier account.');
      }
    } finally {
      setLoading(false); // Reset loading state
    }
  };

  /**
   * Closes the success modal.
   */
  const handleCloseSuccessModal = () => {
    setShowSuccessModal(false);
    navigate('/admin/manage-verifiers')
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-8 col-lg-6">
          <div className="card shadow-lg rounded-3">
            <div className="card-header bg-primary text-white text-center py-3 rounded-top-3">
              <h3 className="mb-0">Create Verifier Account</h3>
            </div>
            <div className="card-body p-4">
              {/* Display API error message if any */}
              {apiError && (
                <div className="alert alert-danger" role="alert">
                  {apiError}
                </div>
              )}
              <form onSubmit={handleSubmit}>
                {/* Full Name Input */}
                <div className="mb-3">
                  <label htmlFor="fullName" className="form-label">Full Name</label>
                  <input
                    type="text"
                    className={`form-control rounded-pill ${errors.fullName ? 'is-invalid' : ''}`}
                    id="fullName"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                  />
                  {errors.fullName && <div className="invalid-feedback">{errors.fullName}</div>}
                </div>

                {/* Email Input */}
                <div className="mb-3">
                  <label htmlFor="email" className="form-label">Email address</label>
                  <input
                    type="email"
                    className={`form-control rounded-pill ${errors.email ? 'is-invalid' : ''}`}
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                  {errors.email && <div className="invalid-feedback">{errors.email}</div>}
                </div>

                {/* Password Input */}
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">Password</label>
                  <input
                    type="password"
                    className={`form-control rounded-pill ${errors.password ? 'is-invalid' : ''}`}
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                  />
                  {errors.password && <div className="invalid-feedback">{errors.password}</div>}
                </div>

                {/* Designation Dropdown */}
                <div className="mb-3">
                  <label htmlFor="designation" className="form-label">Designation</label>
                  <select
                    className={`form-select rounded-pill ${errors.designation ? 'is-invalid' : ''}`}
                    id="designation"
                    name="designation"
                    value={formData.designation}
                    onChange={handleChange}
                    required
                  >
                    <option value="">Select Designation</option>
                    {designations.map((designation) => (
                      <option key={designation} value={designation}>
                        {/* Display user-friendly text by replacing underscores with spaces */}
                        {designation.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                  {errors.designation && <div className="invalid-feedback">{errors.designation}</div>}
                </div>

                {/* Address Input (Optional) */}
                <div className="mb-3">
                  <label htmlFor="address" className="form-label">Address (Optional)</label>
                  <input
                    type="text"
                    className="form-control rounded-pill"
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                  />
                </div>

                {/* Date of Birth Input (Optional) */}
                <div className="mb-3">
                  <label htmlFor="dateOfBirth" className="form-label">Date of Birth (Optional)</label>
                  <input
                    type="date"
                    className={`form-control rounded-pill ${errors.dateOfBirth ? 'is-invalid' : ''}`}
                    id="dateOfBirth"
                    name="dateOfBirth"
                    value={formData.dateOfBirth}
                    onChange={handleChange}
                  />
                  {errors.dateOfBirth && <div className="invalid-feedback">{errors.dateOfBirth}</div>}
                </div>

                {/* Gender Dropdown (Optional) */}
                <div className="mb-3">
                  <label htmlFor="gender" className="form-label">Gender (Optional)</label>
                  <select
                    className="form-select rounded-pill"
                    id="gender"
                    name="gender"
                    value={formData.gender}
                    onChange={handleChange}
                  >
                    <option value="">Select Gender</option>
                    {genders.map((gender) => (
                      <option key={gender} value={gender}>
                        {gender}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Aadhar Number Input (Optional) */}
                <div className="mb-4">
                  <label htmlFor="aadharNumber" className="form-label">Aadhar Number (Optional)</label>
                  <input
                    type="text"
                    className="form-control rounded-pill"
                    id="aadharNumber"
                    name="aadharNumber"
                    value={formData.aadharNumber}
                    onChange={handleChange}
                  />
                </div>

                {/* Submit Button */}
                <div className="d-grid">
                  <button
                    type="submit"
                    className="btn btn-primary btn-lg rounded-pill"
                    disabled={loading} // Disable button while loading
                  >
                    {loading ? (
                      <>
                        {/* Spinner for loading state */}
                        <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                        <span className="ms-2">Creating...</span>
                      </>
                    ) : (
                      'Create Verifier Account'
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-labelledby="successModalLabel" aria-hidden="true">
          <div className="modal-dialog modal-dialog-centered" role="document">
            <div className="modal-content rounded-3 shadow-lg">
              <div className="modal-header bg-success text-white border-0 rounded-top-3">
                <h5 className="modal-title" id="successModalLabel">Success!</h5>
              </div>
              <div className="modal-body text-center py-4">
                <p className="fs-5">Verifier account created successfully!</p>
              </div>
              <div className="modal-footer justify-content-center border-0 rounded-bottom-3">
                <button type="button" className="btn btn-success rounded-pill px-4" onClick={handleCloseSuccessModal}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      {/* Modal Backdrop (for overlay effect) */}
      {showSuccessModal && <div className="modal-backdrop fade show"></div>}
    </div>
  );
};

export default CreateVerifierPage;