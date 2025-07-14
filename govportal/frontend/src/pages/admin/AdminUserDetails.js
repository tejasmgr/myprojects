// --- src/pages/admin/UserDetails.js ---
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { authenticatedFetch } from "../../utils/api";
// import "./UserDetails.css"; // You can create a CSS file for styling

const UserDetails = () => {
  const { id } = useParams(); // Get user ID from URL
  const navigate = useNavigate();
  const { user: loggedInUser, token } = useAuth();
  const [userDetails, setUserDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    // Redirect if not an admin or not logged in
    if (!loggedInUser || loggedInUser.role !== "ADMIN") {
      navigate("/dashboard");
      return;
    }

    const fetchUserDetails = async () => {
      try {
        setLoading(true);
        setError("");
        const data = await authenticatedFetch(`/admin/user/${id}`, {
          method: "GET",
        });
        console.log("Received user details:", data);
        setUserDetails(data);
      } catch (err) {
        console.error("Failed to fetch user details:", err);
        setError(
          err.message || "Failed to load user details. Please try again."
        );
      } finally {
        setLoading(false);
      }
    };

    if (token && id) {
      fetchUserDetails();
    }
  }, [loggedInUser, token, id, navigate]);

  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ height: "80vh" }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading user details...</span>
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
        <button
          className="btn btn-primary rounded-pill px-4 py-2"
          onClick={() => navigate("/admin/users")} // Assuming you have a users list page
        >
          Back to Users
        </button>
      </div>
    );
  }

  if (!userDetails) {
    return (
      <div className="container mt-5 text-center">
        <div className="alert alert-warning rounded-3" role="alert">
          User not found or you do not have access.
        </div>
        <button
          className="btn btn-primary rounded-pill px-4 py-2"
          onClick={() => navigate("/admin/users")}
        >
          Back to Users
        </button>
      </div>
    );
  }


  

  return (
    <div className="container mt-5">
      <h2 className="text-center mb-4 text-info fw-bold">
        User Details (ID: {userDetails.id})
      </h2>
      <div className="card shadow-lg p-4 rounded-4">
        <div className="card-body">
          <h5 className="card-title text-primary mb-3">Personal Information</h5>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Full Name:</strong> {userDetails.fullName || "N/A"}
            </div>
            <div className="col-md-6">
              <strong>Email:</strong> {userDetails.email || "N/A"}
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Gender:</strong> {userDetails.gender || "N/A"}
            </div>
            <div className="col-md-6">
              <strong>Date of Birth:</strong>{" "}
              {userDetails.dateOfBirth
                ? new Date(userDetails.dateOfBirth).toLocaleDateString()
                : "N/A"}
            </div>
          </div>
          <div className="mb-3">
            <strong>Address:</strong> {userDetails.address || "N/A"}
          </div>
          <hr className="my-4" />

          <h5 className="card-title text-success mb-3">Account Information</h5>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Role:</strong>{" "}
              <span
                className={`badge ms-2 ${
                  userDetails.role === "ADMIN"
                    ? "bg-danger"
                    : userDetails.role === "VERIFIER"
                    ? "bg-warning text-dark"
                    : "bg-info"
                } rounded-pill px-3 py-2`}
              >
                {userDetails.role || "N/A"}
              </span>
            </div>
            <div className="col-md-6">
              <strong>Designation:</strong> {userDetails.designation || "N/A"}
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Enabled:</strong>{" "}
              <span
                className={`badge ms-2 ${
                  userDetails.enabled ? "bg-success" : "bg-danger"
                } rounded-pill px-3 py-2`}
              >
                {userDetails.enabled ? "Yes" : "No"}
              </span>
            </div>
            <div className="col-md-6">
              <strong>Blocked:</strong>{" "}
              <span
                className={`badge ms-2 ${
                  userDetails.blocked ? "bg-danger" : "bg-success"
                } rounded-pill px-3 py-2`}
              >
                {userDetails.blocked ? "Yes" : "No"}
              </span>
            </div>
          </div>
          <div className="mb-3">
            <strong>Aadhar Number:</strong> {userDetails.aadharNumber || "N/A"}
          </div>





<div className="row mb-3">
            <div className="col-md-6">
               <strong>Account Created At:</strong>{" "}
            {userDetails.updatedAt
              ? new Date(userDetails.createdAt).toLocaleString()
              : "N/A"}
            </div>
            <div className="col-md-6">
               <strong>Account Updated At:</strong>{" "}
            {userDetails.updatedAt
              ? new Date(userDetails.updatedAt).toLocaleString()
              : "N/A"}
            </div>
          </div>




        </div>
      </div>
    

      <div className="text-center mt-4">
        <button
          className="btn btn-secondary rounded-pill px-4 py-2"
          onClick={() => navigate("/dashboard")} // Assuming you have a users list page
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
};

export default UserDetails;