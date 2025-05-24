// --- src/pages/citizen/MyApplications.js ---
import React, { useState, useEffect } from "react";
import { useAuth } from "../../contexts/AuthContext";
import { authenticatedFetch } from "../../utils/api";
import { useNavigate, Link } from "react-router-dom";

const MyApplications = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Pagination states
  const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-indexed pages
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0); // Total number of applications

  useEffect(() => {
    // Redirect if not a citizen or not logged in
    if (!user || user.role !== "CITIZEN") {
      navigate("/dashboard"); // Or '/login' if not logged in
      return;
    }

    const fetchApplications = async () => {
      try {
        setLoading(true);
        setError("");
        // Append page parameter to the API call
        const data = await authenticatedFetch(
          `/documents/applications?page=${currentPage}`,
          {
            method: "GET",
          }
        );
        // Assuming data is the Page object from Spring Boot
        setApplications(data.content); // Extract the list of applications
        setTotalPages(data.totalPages);
        setCurrentPage(data.number); // Ensure currentPage is in sync with backend's response
        setTotalElements(data.totalElements);
      } catch (err) {
        console.error("Failed to fetch applications:", err);
        setError(
          err.message || "Failed to load your applications. Please try again."
        );
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      // Only fetch if token is available (user is logged in)
      fetchApplications();
    }
  }, [user, token, navigate, currentPage]); // Re-run if user, token, or currentPage changes

  // Function to navigate to application details
  const handleReviewClick = (appId) => {
    navigate(`/citizen/applications/details/${appId}`);
  };

  // Handlers for pagination
  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ height: "80vh" }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading applications...</span>
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
          onClick={() => navigate("/dashboard")}
        >
          Go to Dashboard
        </button>
      </div>
    );
  }

  // If no applications found on the current page or overall
  if (applications.length === 0 && totalElements === 0) {
    return (
      <div className="container mt-5 text-center">
        <div className="alert alert-info rounded-3" role="alert">
          You have not submitted any applications yet.
        </div>
        <Link
          to="/citizen/submit-application"
          className="btn btn-primary rounded-pill px-4 py-2"
        >
          Submit Your First Application
        </Link>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <h2 className="text-center mb-4 text-primary fw-bold">
        My Submitted Applications
      </h2>
      <div className="table-responsive shadow-lg rounded-4 overflow-hidden">
        <table className="table table-hover table-striped mb-0">
          <thead className="bg-primary text-white">
            <tr>
              <th scope="col" className="py-3">
                ID
              </th>
              <th scope="col" className="py-3">
                Document Type
              </th>
              <th scope="col" className="py-3">
                Purpose
              </th>
              <th scope="col" className="py-3">
                Status
              </th>
              <th scope="col" className="py-3">
                Submitted On
              </th>
              <th scope="col" className="py-3">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {applications.map((app) => (
              <tr key={app.id}>
                <td>{app.id}</td>
                <td>{app.documentType}</td>
                <td>{app.purpose}</td>
                <td>
                  <span
                    className={`badge ${
                      app.status === "PENDING"
                        ? "bg-warning text-dark"
                        : app.status === "APPROVED"
                        ? "bg-success"
                        : app.status === "REJECTED"
                        ? "bg-danger"
                        : "bg-secondary"
                    } rounded-pill px-3 py-2`}
                  >
                    {app.status}
                  </span>
                </td>
                <td>{new Date(app.submissionDate).toLocaleDateString()}</td>
                <td>
                  {/* Placeholder for viewing application details */}
                  <button
                    className="btn btn-sm btn-outline-info rounded-pill"
                    onClick={() => handleReviewClick(app.id)}
                    // In a real app, you'd navigate to a detail page: navigate(`/citizen/applications/${app.id}`)
                  >
                    View Details
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <nav aria-label="Page navigation" className="mt-4">
          <ul className="pagination justify-content-center">
            <li className={`page-item ${currentPage === 0 ? "disabled" : ""}`}>
              <button
                className="page-link"
                onClick={handlePreviousPage}
                disabled={currentPage === 0}
              >
                Previous
              </button>
            </li>
            {[...Array(totalPages)].map((_, index) => (
              <li
                key={index}
                className={`page-item ${currentPage === index ? "active" : ""}`}
              >
                <button
                  className="page-link"
                  onClick={() => setCurrentPage(index)}
                >
                  {index + 1}
                </button>
              </li>
            ))}
            <li
              className={`page-item ${
                currentPage === totalPages - 1 ? "disabled" : ""
              }`}
            >
              <button
                className="page-link"
                onClick={handleNextPage}
                disabled={currentPage === totalPages - 1}
              >
                Next
              </button>
            </li>
          </ul>
        </nav>
      )}
      {totalElements > 0 && (
        <p className="text-center mt-2 text-muted">
          Showing {applications.length} of {totalElements} applications.
        </p>
      )}
    </div>
  );
};

export default MyApplications;
