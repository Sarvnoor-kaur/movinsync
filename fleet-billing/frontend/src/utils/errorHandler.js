/** Extract a user-friendly message from an Axios error response */
export function getErrorMessage(error) {
  if (!error) return 'An unexpected error occurred.';
  const status = error.response?.status;
  const data = error.response?.data;
  const correlationId = data?.correlationId || error.response?.headers?.['x-correlation-id'];

  let baseMsg = '';

  // Check structured field errors
  if (data?.fieldErrors && typeof data.fieldErrors === 'object' && Object.keys(data.fieldErrors).length > 0) {
    baseMsg = Object.entries(data.fieldErrors)
      .map(([field, msg]) => `${field}: ${msg}`)
      .join(' | ');
  } else if (data?.message) {
    baseMsg = data.message;
  } else if (data?.error) {
    baseMsg = data.error;
  } else if (typeof data === 'string' && data.length < 200) {
    baseMsg = data;
  } else {
    switch (status) {
      case 400: baseMsg = 'Please check the entered data.'; break;
      case 401: baseMsg = 'Your session has expired. Please login again.'; break;
      case 403: baseMsg = 'You do not have permission to perform this action.'; break;
      case 404: baseMsg = 'The requested resource was not found.'; break;
      case 409: baseMsg = 'An operation with this data already exists.'; break;
      case 500: baseMsg = 'Something went wrong on the server. Please try again.'; break;
      default:  baseMsg = error.message || 'An unexpected error occurred.'; break;
    }
  }

  return baseMsg;
}

/** Get structured error payload including correlation ID for detailed UI display */
export function getDetailedError(error) {
  const data = error?.response?.data;
  const status = error?.response?.status;
  const correlationId = data?.correlationId || error?.response?.headers?.['x-correlation-id'];
  
  return {
    message: getErrorMessage(error),
    status,
    correlationId,
    fieldErrors: data?.fieldErrors || null,
  };
}
