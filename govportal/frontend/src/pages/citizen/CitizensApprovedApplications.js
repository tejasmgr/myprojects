// --- src/pages/citizens/ApprovedApplications.js ---
import React, { useState, useEffect } from "react";
import { useAuth } from "../../contexts/AuthContext";
import { authenticatedFetch } from "../../utils/api";
import { useNavigate } from "react-router-dom"; // Ensure useNavigate is imported
import { saveAs } from 'file-saver';

const CitizensApprovedApplications = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const [citizensApprovedApplications, setCitizensApprovedApplications] =
    useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Pagination states
  const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-indexed pages
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0); // Total number of applications

  // Fetch applications based on current page
  useEffect(() => {
    // Redirect if not a verifier or not logged in
    if (!user) {
      navigate("/dashboard"); // Redirect to dashboard if not authorized
      return;
    }

    const fetchCitizensApprovedApplications = async () => {
      try {
        setLoading(true);
        setError("");
        const data = await authenticatedFetch(
          `/documents/citizen/approvalPassed-applications?page=${currentPage}`,
          { method: "GET" }
        );
        setCitizensApprovedApplications(data.content);
        setTotalPages(data.totalPages);
        setCurrentPage(data.number);
        setTotalElements(data.totalElements);
      } catch (err) {
        console.error(`failed to fetchcitizens approvedapplications: ${err}`);
        setError(err.message || "Failed to fetch approved applications");
      } finally {
        setLoading(false);
      }
    };
    if (token) {
      fetchCitizensApprovedApplications();
    }
  }, [user, token, navigate, currentPage]);

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

  // Function to navigate to application details
  const handleReviewClick = (appId) => {
    navigate(`/citizen/applications/details/${appId}`);
  };


  const handleDownloadCertificate = async (appId) => {
    try {
        const response = await authenticatedFetch(
            `/documents/certificate/${appId}/download`,
            {
                method: 'GET',
                // You might need to explicitly set headers if your backend requires them
                // headers: {
                //     'Authorization': `Bearer ${token}`, // If your token is needed
                // },
                // responseType: 'blob', // Not needed with fetch's blob() method
            }
        );

        if (response.ok) {
            const blob = await response.blob();
            const filename = `certificate_${appId}.pdf`; // Or try to get filename from response headers
            saveAs(blob, filename);
        } else {
            const errorData = await response.json();
            console.error('Failed to download certificate:', errorData);
            // Optionally display an error message to the user
        }
    } catch (error) {
        console.error('Error downloading certificate:', error);
        // Optionally display an error message to the user
    }
};


  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ height: "80vh" }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">
            Loading your approved applications...
          </span>
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

  if (CitizensApprovedApplications.length === 0 && totalElements === 0) {
    return (
      <div className="container mt-5 text-center">
        <div className="alert alert-info rounded-3" role="alert">
          No applications are approved at the moment.
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

  return (
    <div className="container mt-5">
      <h2 className="text-center mb-4 text-success fw-bold">
        Your Approved Applications
      </h2>
      <div className="table-responsive shadow-lg rounded-4 overflow-hidden">
        <table className="table table-hover table-striped mb-0">
          <thead className="bg-success text-white">
            <tr>
              <th scope="col" className="py-3">
                ID
              </th>
              {/* <th scope="col" className="py-3">Applicant Email</th> */}
              <th scope="col" className="py-3">
                Document Type
              </th>
              {/* <th scope="col" className="py-3">Purpose</th> */}
              <th scope="col" className="py-3">
                Submitted On
              </th>
              <th scope="col" className="py-3">
                Actions
              </th>
              <th scope="col" className="py-3">
                Certificate
              </th>
            </tr>
          </thead>
          <tbody>
            {citizensApprovedApplications.map((app) => (
              <tr key={app.id}>
                <td>{app.id}</td>
                {/* <td>{app.applicantEmail}</td> */}
                <td>{app.documentType}</td>
                {/* <td>{app.purpose}</td> */}
                <td>{new Date(app.submissionDate).toLocaleDateString()}</td>
                <td>
                  <button
                    className="btn btn-info"
                    onClick={() => handleReviewClick(app.id)} // Changed to navigate
                  >
                    View Details
                  </button>
                </td>
                <td>
                  <button
                   type="button" 
                   class="btn btn-success"
                   onClick={()=>handleDownloadCertificate(app.id)}
                  >
                    Download Certificate
                    
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
          Showing {citizensApprovedApplications.length} of {totalElements}{" "}
          applications.
        </p>
      )}
  


    </div>
  );
};

export default CitizensApprovedApplications;
