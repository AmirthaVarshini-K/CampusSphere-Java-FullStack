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
  const validationMessages = extractValidationMessages(error);
  if (validationMessages.length > 0 || error?.response?.data?.message === 'Validation failed.') {
    return 'Please fix the highlighted fields below.';
  }

  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (isNetworkError(error)) {
    return 'CampusSphere services are currently unavailable. Please try again shortly.';
  }

  return fallback;
}
