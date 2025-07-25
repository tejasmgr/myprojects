// src/utils/api.js
// This file sets up a basic API client using fetch.
// You could replace this with Axios for more advanced features like interceptors.

console.log(`${process.env.REACT_APP_API_URL}`)
const API_BASE_URL = `${process.env.REACT_APP_API_URL}/api`; // Your backend API base URL

// Helper function to make authenticated API requests
export const authenticatedFetch = async (url, options = {}, returnRaw = false) => {
  const token = localStorage.getItem("token");

  const headers = {
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  if (returnRaw) return response;

  if (!response.ok) {
    const errorText = await response.text();
    let errorData = { message: errorText || response.statusText || "Something went wrong." };
    try {
      const jsonError = JSON.parse(errorText);
      errorData = { ...errorData, ...jsonError };
    } catch (_) {}
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }

  const contentType = response.headers.get("content-type");
  if (contentType?.includes("application/json")) {
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  } else if (contentType?.includes("application/pdf") || contentType?.startsWith("image/")) {
    return response;
  } else {
    const text = await response.text();
    return text || {};
  }
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

  // ✅ If the response is successful, return as usual
  if (response.ok) {
    // Automatically parse JSON only if expected, else return raw response
    const contentType = response.headers.get("content-type");
    if (contentType?.includes("application/json")) {
      return response.json();
    } else {
      return response;
    }
  }

  // ❌ For failed responses: Try to extract useful error message
  let errorMessage = `HTTP error! status: ${response.status}`;
  console.log(errorMessage)
  console.log(response)
  let errorCode = null;

  try {
    const errorText = await response.text();
    const jsonError = JSON.parse(errorText);
    errorMessage = jsonError.message || errorMessage;
    errorCode = jsonError.code || null;
  } catch {
    // Ignore JSON parsing error, fallback to status text
    errorMessage = response.statusText || errorMessage;
  }

  // Throw custom error object with optional code
  const error = new Error(errorMessage);
  error.status = response.status;
  error.code = errorCode;
  throw error;
};
