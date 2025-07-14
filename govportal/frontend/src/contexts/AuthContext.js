// src/contexts/AuthContext.js
import React, { createContext, useState, useContext, useEffect } from "react";

// Create the AuthContext
const AuthContext = createContext(null);

// AuthProvider component to wrap your application and provide authentication state
export const AuthProvider = ({ children }) => {
  // State to hold the user object (from backend User model) and the JWT token
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true); // To manage initial loading state from localStorage

  // useEffect hook to load user and token from localStorage on component mount
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    const storedToken = localStorage.getItem("token");

    if (storedUser && storedToken) {
      try {
        // Parse stored user data (it was stringified when saved)
        setUser(JSON.parse(storedUser));
        setToken(storedToken);
      } catch (error) {
        console.error(
          "Failed to parse stored user data from localStorage:",
          error
        );
        // Clear invalid data if parsing fails
        localStorage.removeItem("user");
        localStorage.removeItem("token");
      }
    }
    setLoading(false); // Authentication check is complete
  }, []); // Empty dependency array means this runs once on mount

  // Function to handle user login: stores user data and token
  const login = (userData, accessToken) => {
    setUser(userData);
    setToken(accessToken);
    localStorage.setItem("user", JSON.stringify(userData)); // Store user data as a JSON string
    localStorage.setItem("token", accessToken);
  };

  // Function to handle user logout: clears user data and token
  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
  };

  // The value provided to consumers of this context
  const authContextValue = {
    user,
    token,
    loading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to easily consume the AuthContext in functional components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
