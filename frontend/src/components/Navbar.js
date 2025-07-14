import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Dropdown from "react-bootstrap/Dropdown";

import NavDropdown from 'react-bootstrap/NavDropdown';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm py-2">
      <div className="container-fluid">
        <Link className="navbar-brand fw-bold text-white" to="/">
          GovPortal
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {user ? (
              <>
                <li className="nav-item">
                  <Link className="nav-link text-white" to="/dashboard">
                    Dashboard
                  </Link>
                </li>

                {user.role === "CITIZEN" && (
                  <>
                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/citizen/submit-application"
                      >
                        Submit Application
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/citizen/my-applications"
                      >
                        My Applications
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/citizen/approved-applications"
                      >
                        Approved Applications
                      </Link>
                    </li>
                  </>
                )}

                {(user.role === "VERIFIER" || user.role === "ADMIN") && (
                  <>
                    {/* <li className="nav-item">
                      <Link className="nav-link text-white" to="/verifier/pending-applications">
                        Pending Applications
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link className="nav-link text-white" to="/verifier/approved-applications">
                        Approved Applications
                      </Link>
                    </li> */}
                  </>
                )}

                {user.role === "ADMIN" && (
                  <>
                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/admin/manage-verifiers"
                      >
                        Manage Verifiers
                      </Link>
                    </li>

                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/admin/manage-user"
                      >
                        Manage Citizens
                      </Link>
                    </li>

                    <li className="nav-item">
                      <Link
                        className="nav-link text-white"
                        to="/admin/manage-applications"
                      >
                        Manage Applications
                      </Link>
                    </li>

                    

                  </>
                )}
              </>
            ) : (
              <>
                <li className="nav-item">
                  <Link className="nav-link text-white" to="/login">
                    Login
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link text-white" to="/register">
                    Register
                  </Link>
                </li>
              </>
            )}
          </ul>

          {user && (
            <div className="d-flex align-items-center gap-3">
              <span className="text-white">
                Welcome, <strong>{user.fullName || user.firstName}</strong> (
                <span className="text-warning">{user.role}</span>)
              </span>

              <Dropdown align="end">
                <Dropdown.Toggle
                  variant="light"
                  className="rounded-pill px-3 py-1 fw-semibold"
                >
                  Profile
                </Dropdown.Toggle>

                <Dropdown.Menu>
                  <Dropdown.Item as={Link} to="/profile">
                    View Profile
                  </Dropdown.Item>
                  <Dropdown.Item as={Link} to="/profile/change-password-page">
                    Change Password
                  </Dropdown.Item>
                  <Dropdown.Divider />
                  <Dropdown.Item
                    as="button"
                    onClick={handleLogout}
                    className="text-danger"
                  >
                    Logout
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
