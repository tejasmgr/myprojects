import React, { createContext, useState, useEffect } from 'react';

// Create the AuthContext
export const AuthContext = createContext();

// AuthProvider component to wrap around components that need auth info
export const AuthProvider = ({ children }) => {
  const [authData, setAuthData] = useState(() => {
    // Try to get the auth data from localStorage on load
    const saved = localStorage.getItem('authData');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    // Store authData in localStorage whenever it changes
    if (authData) {
      localStorage.setItem('authData', JSON.stringify(authData));
    } else {
      localStorage.removeItem('authData');
    }
  }, [authData]);

  return (
    <AuthContext.Provider value={{ authData, setAuthData }}>
      {children}
    </AuthContext.Provider>
  );
};
