
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { authenticatedFetch } from '../../utils/api';
import { useNavigate } from 'react-router-dom';

const AdminSystemStats = () => {
    const { user, token } = useAuth();
    const navigate = useNavigate();

    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        // Redirect if not an admin or not logged in
        if (!user || user.role !== 'ADMIN') {
            navigate('/dashboard');
            return;
        }

        const fetchSystemStats = async () => {
            setLoading(true);
            setError('');
            try {
                // Call the backend API to get system statistics
                const response = await authenticatedFetch('/admin/stats', {
                    method: 'GET',
                });

               

                const data =  response;

                console.log(data);
                setStats(data);
            } catch (err) {
                console.error('Error fetching system stats:', err);
                setError(err.message || 'Failed to fetch system statistics. Please try again.');
            } finally {
                setLoading(false);
            }
        };

        if (token) {
            fetchSystemStats();
        }
    }, [user, token, navigate]);

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center vh-100">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading system statistics...</span>
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
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Go to Dashboard
                </button>
            </div>
        );
    }

    if (!stats) {
        return (
            <div className="container mt-5 text-center">
                <div className="alert alert-warning rounded-3" role="alert">
                    No system statistics available.
                </div>
                <button className="btn btn-primary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Go to Dashboard
                </button>
            </div>
        );
    }

    return (
        <div className="container mt-5">
            <h2 className="text-center mb-4 text-primary fw-bold">System Statistics</h2>
            <div className="row justify-content-center">
                <div className="col-md-8 col-lg-6">
                    <div className="card shadow-lg p-4 rounded-4">
                        <div className="card-body">
                            <ul className="list-group list-group-flush">
                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Total Citizens:</strong>
                                    <span className="badge bg-primary rounded-pill fs-6">{stats.totalCitizens}</span>
                                </li>
                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Active Verifiers:</strong>
                                    <span className="badge bg-success rounded-pill fs-6">{stats.activeVerifiers}</span>
                                </li>
                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Pending Applications:</strong>
                                    <span className="badge bg-warning text-dark rounded-pill fs-6">{stats.pendingApplications}</span>
                                </li>
                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Approved Applications:</strong>
                                    <span className="badge bg-info rounded-pill fs-6">{stats.approvedApplications}</span>
                                </li>
                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                    <strong>Blocked Accounts:</strong>
                                    <span className="badge bg-danger rounded-pill fs-6">{stats.blockedAccounts}</span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            <div className="text-center mt-4">
                <button className="btn btn-secondary rounded-pill px-4 py-2" onClick={() => navigate('/dashboard')}>
                    Back to Dashboard
                </button>
            </div>
        </div>
    );
};

export default AdminSystemStats;
