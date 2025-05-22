import React, { useContext } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register'; // will create next
import Dashboard from './pages/Dashboard'; // will create later
import Home from './pages/Home'; // optional homepage
import { AuthContext } from './context/AuthContext';


function App() {
  const { authData } = useContext(AuthContext);
  return (
    <Router>
      <Routes>
        <Route path="/login" element={!authData ? <Login /> : <Navigate to="/dashboard" />} />
        <Route path="/" element={<Home />} />
        
        <Route path="/register" element={!authData ? <Register /> : <Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={authData ? <Dashboard /> : <Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
