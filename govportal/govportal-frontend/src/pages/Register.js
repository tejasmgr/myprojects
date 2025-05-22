import React, { useState, useContext } from 'react';
import AuthService from '../services/AuthService';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Modal from 'react-modal';

// Make sure to bind Modal to your appElement (often the root div)
Modal.setAppElement('#root'); // Or whatever your root element ID is

const Register = () => {
  // Fields matching RegisterRequest DTO
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [address, setAddress] = useState('');
  const [aadharNumber, setAadharNumber] = useState('');

  const [error, setError] = useState('');
  const [registrationSuccess, setRegistrationSuccess] = useState(false); // State for yarn start visibility

  const { setAuthData } = useContext(AuthContext);
  const navigate = useNavigate();

  // Frontend validation patterns if needed
  const passwordPattern = /^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9]).{8,}$/; // Example: min 8 chars, 1 uppercase, 1 special char, 1 digit

  const validateInputs = () => {
    if (!firstName.trim()) {
      setError('First name is required');
      return false;
    }
    if (!lastName.trim()) {
      setError('Last name is required');
      return false;
    }
    if (!email.trim()) {
      setError('Email is required');
      return false;
    }
    if (!passwordPattern.test(password)) {
      setError('Password must be at least 8 characters long and include uppercase letters, numbers, and special characters');
      return false;
    }
    if (password !== confirmPassword) {
      setError('Password and Confirm password do not match');
      return false;
    }
    if (!address.trim()) {
      setError('Address is required');
      return false;
    }
    if (!aadharNumber.trim() || aadharNumber.length !== 12) {
      setError('Aadhar number must be 12 digits');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setRegistrationSuccess(false); // Reset success state

    if (!validateInputs()) return;

    const registerData = {
      firstName,
      lastName,
      email,
      password,
      address,
      aadharNumber,
    };

    try {
      const response = await AuthService.register(registerData);
      console.log('Registration successful:', response);
      setRegistrationSuccess(true); // Set success state to open modal
    } catch (err) {
      console.error('Registration error:', err);
      if (err.response && err.response.status == 409 && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else if (err.response && err.response.status == 500 && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Registration failed. Please try again.');
      }
    }
  };

  const closeModal = () => {
    setRegistrationSuccess(false);
    navigate('/login'); // Navigate to the login page after closing
  };

  return (
    <div style={styles.container}>
      <h2>Create Your Account</h2>
      <form onSubmit={handleSubmit} style={styles.form}>
        <label style={styles.label}>First Name</label>
        <input
          type="text"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          placeholder="First Name"
          required
          style={styles.input}
        />

        <label style={styles.label}>Last Name</label>
        <input
          type="text"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          placeholder="Last Name"
          required
          style={styles.input}
        />

        <label style={styles.label}>Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
          required
          style={styles.input}
        />

        <label style={styles.label}>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
          style={styles.input}
        />

        <label style={styles.label}>Confirm Password</label>
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="Confirm Password"
          required
          style={styles.input}
        />

        <label style={styles.label}>Address</label>
        <input
          type="text"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          placeholder="Address"
          required
          style={styles.input}
        />

        <label style={styles.label}>Aadhar Number</label>
        <input
          type="text"
          value={aadharNumber}
          onChange={(e) => setAadharNumber(e.target.value)}
          placeholder="Aadhar Number"
          required
          style={styles.input}
        />

        {error && <p style={styles.error}>{error}</p>}

        <button type="submit" style={styles.button}>Register</button>
      </form>

      <Modal
        isOpen={registrationSuccess}
        onRequestClose={closeModal}
        style={customStyles}
        contentLabel="Registration Successful Modal"
      >
        <h2>Registration Successful!</h2>
        <p>Your account has been created successfully.</p>
        <p>Please check your email inbox for verification link to activate your account before login</p>
        <button onClick={closeModal}>Go to Login Page</button>
      </Modal>
    </div>
  );
};

const customStyles = {
  content: {
    top: '50%',
    left: '50%',
    right: 'auto',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 0 10px rgba(0, 0, 0, 0.2)',
    textAlign: 'center',
  },
  overlay: {
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    zIndex: 1000,
  },
};

const styles = {
  container: {
    maxWidth: 400,
    margin: '50px auto',
    padding: 25,
    borderRadius: 10,
    backgroundColor: '#f0f4f8',
    boxShadow: '0 0 10px #ccc',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
  },
  label: {
    marginBottom: 6,
    fontWeight: 'bold',
    fontSize: 14,
  },
  input: {
    marginBottom: 18,
    padding: 12,
    fontSize: 16,
    borderRadius: 6,
    border: '1px solid #ccc',
  },
  button: {
    padding: 14,
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: 6,
    cursor: 'pointer',
    fontSize: 16,
    fontWeight: 'bold',
  },
  error: {
    color: '#c0392b',
    marginBottom: 10,
    fontWeight: 'bold',
  },
  success: { // You weren't using this, but it's here for consistency
    color: '#27ae60',
    marginBottom: 10,
    fontWeight: 'bold',
  },
};

export default Register;