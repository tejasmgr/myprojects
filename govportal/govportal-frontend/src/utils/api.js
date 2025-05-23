// src/utils/api.js
// This file sets up a basic API client using fetch.
// You could replace this with Axios for more advanced features like interceptors.

const API_BASE_URL = "http://localhost:8080/api"; // Your backend API base URL

// Helper function to make authenticated API requests
export const authenticatedFetch = async (url, options = {}) => {
  const token = localStorage.getItem("token"); // Get token from localStorage

  const headers = {
    // 'Content-Type': 'application/json', // This will be handled by browser for FormData
    ...options.headers, // Allow overriding headers
  };

  // Add Authorization header if a token exists
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  // Handle HTTP errors
  if (!response.ok) {
    // Read the response body once, then try to parse it
    const errorText = await response.text();
    let errorData = {
      message: errorText || response.statusText || "Something went wrong.",
    };
    try {
      // If the error response is JSON, parse it for more details
      const jsonError = JSON.parse(errorText);
      errorData = { ...errorData, ...jsonError };
    } catch (parseError) {
      // If it's not JSON, the errorText already contains the message
    }
    // Throw an error with more details
    throw new Error(
      errorData.message || `HTTP error! status: ${response.status}`
    );
  }

  // --- START OF FIX: Handle different content types for successful responses ---
  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    // If it's JSON, parse it
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  } else if (
    contentType &&
    (contentType.includes("application/pdf") ||
      contentType.startsWith("image/"))
  ) {
    // If it's a PDF or an image, return the raw Response object
    // This allows the calling component (e.g., DocumentProofModal) to call .blob() on it
    return response;
  } else {
    // For other content types (e.g., plain text), return the text
    const text = await response.text();
    return text || {}; // Return the text if it exists, otherwise an empty object
  }
  // --- END OF FIX ---
};

// Helper function for unauthenticated API requests (e.g., login, register, verify-email)
export const unauthenticatedFetch = async (url, options = {}) => {
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    // Read the response body once, then try to parse it
    const errorText = await response.text();
    let errorData = {
      message: errorText || response.statusText || "Something went wrong.",
    };
    try {
      // If the error response is JSON, parse it for more details
      const jsonError = JSON.parse(errorText);
      errorData = { ...errorData, ...jsonError };
    } catch (parseError) {
      // If it's not JSON, the errorText already contains the message
    }
    throw new Error(
      errorData.message || `HTTP error! status: ${response.status}`
    );
  }

  // For successful responses, check if the response has content.
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  } else {
    const text = await response.text();
    return text || {}; // Return the text if it exists, otherwise an empty object
  }
};
