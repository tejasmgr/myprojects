import axios from 'axios';
import api from '../utils/api'; // Axios instance with base URL and interceptors

const getCurrentUser = () => {
  // Corresponds to GET /api/user/profile or equivalent backend endpoint to get logged-in user details
  return api.get('/user/profile');
};

const UserService = {
  getCurrentUser,
};

export default UserService;

