export function isNetworkError(error) {
  return !error?.response || error?.code === 'ERR_NETWORK' || error?.message === 'Network Error';
}

export function extractValidationMessages(error) {
  const errors = error?.response?.data?.errors;
  if (!errors || typeof errors !== 'object') {
    return [];
  }

  return Object.values(errors)
    .flatMap(value => (Array.isArray(value) ? value : [value]))
    .filter(Boolean);
}

export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (isNetworkError(error)) {
    return 'CampusSphere cannot reach the backend right now. Please try again once the server is available.';
  }

  return fallback;
}
