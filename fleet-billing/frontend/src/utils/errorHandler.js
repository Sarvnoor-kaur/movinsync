/** Extract a user-friendly message from an Axios error response */
export function getErrorMessage(error) {
  if (!error) return 'An unexpected error occurred.';
  const status = error.response?.status;
  const data = error.response?.data;

  // Prefer backend message
  if (data?.message) return data.message;
  if (data?.error) return data.error;
  if (typeof data === 'string' && data.length < 200) return data;

  switch (status) {
    case 400: return 'Please check the entered data.';
    case 401: return 'Your session has expired. Please login again.';
    case 403: return 'You do not have permission to perform this action.';
    case 404: return 'The requested resource was not found.';
    case 409: return 'An operation with this data already exists.';
    case 500: return 'Something went wrong on the server. Please try again.';
    default:  return error.message || 'An unexpected error occurred.';
  }
}
