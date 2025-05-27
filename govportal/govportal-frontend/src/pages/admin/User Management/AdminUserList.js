import React, { useState, useEffect } from "react";
import { useAuth } from "../../../contexts/AuthContext";
import { authenticatedFetch } from "../../../utils/api";
import { useNavigate } from "react-router-dom";
// import "./ModalStyles.css"

const AdminUserList = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Pagination states
  const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-indexed pages
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0); // Total number of applications

  // Modal States
  const [actionMessage, setActionMessage] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [selectedDeleteId, setSelectedDeleteId] = useState("");
  const [deleteWarnModal, setDeleteWarnModal] = useState(false);
  const [deleteSuccessModal, setDeleteSuccessModal] = useState(false);
  const [toggleLoadingId, setToggleLoadingId] = useState(null); // To show loading state for toggle

  useEffect(() => {
    // Redirect if not an ADMIN or not logged in
    if (!user || user.role !== "ADMIN") {
      navigate("/dashboard"); // Or '/login' if not logged in
      return;
    }

    const fetchUsers = async () => {
      try {
        setLoading(true);
        setError("");
        // Append page parameter to the API call
        const data = await authenticatedFetch(
          `/admin/citizens?page=${currentPage}`,
          {
            method: "GET",
          }
        );
        // Assuming data is the Page object from Spring Boot
        setUsersList(data.content); // Extract the list of applications
        setTotalPages(data.totalPages);
        setCurrentPage(data.number); // Ensure currentPage is in sync with backend's response
        setTotalElements(data.totalElements);
      } catch (err) {
        console.error("Failed to fetch Useers:", err);
        setError(err.message || "Failed to load users. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      // Only fetch if token is available (user is logged in)
      fetchUsers();
    }
  }, [user, token, navigate, currentPage]); // Re-run if user, token, or currentPage changes

  // Function to navigate to application details
  const handleReviewClick = (userID) => {
    navigate(`/admin/user/details/${userID}`);
  };

  const handleOpenDeleteWarnModal = (userID) => {
    setSelectedDeleteId(userID);
    setDeleteWarnModal(true);
  };

  const handleCloseDeleteWarnModal = () => {
    setSelectedDeleteId("");
    setDeleteWarnModal(false);
  };

  const handleDeleteUser = async () => {
    if (!selectedDeleteId) return;

    setDeleteLoading(true);
    try {
      const response = await authenticatedFetch(
        `/admin/delete-verifier/${selectedDeleteId}`,
        {
          method: "DELETE",
        },true
      );
      console.log("Delete response:", response.status, response.statusText);

      if (response.ok) {
        setActionMessage("User deleted successfully.");
        setDeleteSuccessModal(true);
        setDeleteWarnModal(false);
        const updatedUsers = usersList.filter(
          (user) => user.id !== selectedDeleteId
        );
        setUsersList(updatedUsers);
      } else {
        let errorMessage = "Failed to delete user.";
        try {
          const contentType = response.headers.get("content-type");
          if (contentType && contentType.includes("application/json")) {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
          }
        } catch (jsonError) {
          // JSON parsing failed – likely because it's a 204 or plain text
        }
        setError(errorMessage);
      }
    } catch (err) {
      console.error("Error deleting user:", err);
      setError(err.message || "Failed to delete user.");
    } finally {
      setDeleteLoading(false);
      setSelectedDeleteId("");
    }
  };

  const handleToggleUserBlock = async (userId, isBlocked) => {
  setToggleLoadingId(userId);
  try {
    const shouldBlock = !isBlocked;
    const response = await authenticatedFetch(
      `/admin/block-user/${userId}?block=${shouldBlock}`,
      {
        method: "PUT",
      },
      true // Return raw Response object
    );

    if (response.ok) {
      const updatedUsers = usersList.map((user) =>
        user.id === userId
          ? { ...user, blocked: shouldBlock }
          : user
      );
      setUsersList(updatedUsers);
    } else {
      let errorMessage = "Failed to toggle user status.";
      try {
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        }
      } catch (jsonErr) {
        // Do nothing – fallback message will be shown
      }
      setError(errorMessage);
    }
  } catch (err) {
    console.error("Error toggling user block:", err);
    setError(err.message || "Failed to toggle user status.");
  } finally {
    setToggleLoadingId(null);
  }
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
          <span className="visually-hidden">Loading users...</span>
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
  if (!loading && !error && usersList.length === 0) {
    return (
      <div className="container mt-5 text-center">
        <div className="alert alert-info rounded-3" role="alert">
          There are no any users
        </div>
        
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <h2 className="text-center mb-4 text-primary fw-bold">Users</h2>
      <div className="table-responsive shadow-lg rounded-4 overflow-hidden">
        <table className="table table-hover table-striped mb-0">
          <thead className="bg-primary text-white">
            <tr>
              <th scope="col" className="py-3">
                ID
              </th>
              <th scope="col" className="py-3">
                User Name
              </th>
              <th scope="col" className="py-3">
                Email Address
              </th>

              <th scope="col" className="py-3">
                Created On
              </th>
              <th scope="col" className="py-3">
                Details
              </th>
              <th scope="col" className="py-3">
                Block/Unblock
              </th>
              {/* <th scope="col" className="py-3">
                Delete
              </th> */}
            </tr>
          </thead>
          <tbody>
            {usersList.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.fullName}</td>
                <td>{user.email || "N/A"}</td>

                <td>{new Date(user.updatedAt).toLocaleDateString()}</td>
                <td>
                  {/* Placeholder for viewing application details */}
                  <button
                    className="btn btn-sm btn-outline-info rounded-pill"
                    onClick={() => handleReviewClick(user.id)}
                    // In a real app, you'd navigate to a detail page: navigate(`/citizen/applications/${app.id}`)
                  >
                    View Details
                  </button>
                </td>
                <td>
                  <button
                    className={`btn btn-sm rounded-pill ${
                      user.blocked ? "btn-danger" : "btn-success"
                    }`}
                    onClick={() =>
                      handleToggleUserBlock(user.id, user.blocked)
                    }
                    disabled={toggleLoadingId === user.id}
                  >
                    {toggleLoadingId === user.id ? (
                      <span
                        className="spinner-border spinner-border-sm"
                        role="status"
                        aria-hidden="true"
                      ></span>
                    ) : user.blocked ? (
                      "Unblock"
                    ) : (
                      "Block"
                    )}
                  </button>
                </td>
                {/* <td>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm rounded-pill"
                    onClick={() => handleOpenDeleteWarnModal(user.id)}
                  >
                    Delete
                  </button>
                </td> */}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Delete Warning Modal */}
      {deleteWarnModal && (
        <div
          className="modal fade show d-block"
          tabIndex="-1"
          role="dialog"
          
        >
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title text-warning">Warning!</h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={handleCloseDeleteWarnModal}
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <p>Are you sure you want to delete this user?</p>
                <p className="text-danger">This action cannot be undone.</p>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary rounded-pill"
                  onClick={handleCloseDeleteWarnModal}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger rounded-pill"
                  onClick={handleDeleteUser}
                  disabled={deleteLoading}
                >
                  {deleteLoading ? (
                    <span
                      className="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                  ) : (
                    "Delete"
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {deleteSuccessModal && (
        <div
          className="modal fade show d-block"
          tabIndex="-1"
          role="dialog"
         
        >
          <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title text-success">Delete Successful!</h5>
            </div>
            <div className="modal-body">
              <p>{actionMessage}</p>
              <p>You will be redirected to the Users list.</p>
            </div>
            <div className="modal-footer">
              <button
                className="btn btn-success rounded-pill"
                onClick={() => {
                  setDeleteSuccessModal(false);
                  // Optionally, you might want to refresh the list again here
                  // fetchVerifiers();
                }}
              >
                OK
              </button>
            </div>
          </div>
        </div></div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <nav aria-label="Page navigation" className="mt-4">
          <ul className="pagination justify-content-center">
            <li className={`page-item ${currentPage === 0 ? "disabled" : ""}`}>
              <button
                className="page-link rounded-pill"
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
                  className="page-link rounded-pill"
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
                className="page-link rounded-pill"
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
          Showing {usersList.length} of {totalElements} users.
        </p>
      )}
      <div className="text-center mt-4">
        <button
          className="btn btn-secondary rounded-pill px-4 py-2"
          onClick={() => navigate("/dashboard")}
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
};

export default AdminUserList;
