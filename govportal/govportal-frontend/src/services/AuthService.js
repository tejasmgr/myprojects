import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth'; // Adjust if your backend API URL/port is different

const login = (email, password) => {
  return axios.post(`${API_BASE_URL}/login`, {
    email,
    password
  });
};

const register = (userData) => {
  return axios.post(`${API_BASE_URL}/register`, userData);
};

// You can extend this service later with register, logout, etc.

const AuthService = {
  login,register
};

export default AuthService;
