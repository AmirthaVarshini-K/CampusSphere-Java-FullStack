import apiClient from './apiClient';

function buildQuery(params = {}) {
  return {
    params
  };
}

export const eventApi = {
  listCategories(params) {
    return apiClient.get('/event-categories', buildQuery(params));
  },
  getCategory(id) {
    return apiClient.get(`/event-categories/${id}`);
  },
  createCategory(payload) {
    return apiClient.post('/event-categories', payload);
  },
  updateCategory(id, payload) {
    return apiClient.put(`/event-categories/${id}`, payload);
  },
  updateCategoryStatus(id, active) {
    return apiClient.patch(`/event-categories/${id}/status`, null, buildQuery({ active }));
  },

  listTypes(params) {
    return apiClient.get('/event-types', buildQuery(params));
  },
  getType(id) {
    return apiClient.get(`/event-types/${id}`);
  },
  createType(payload) {
    return apiClient.post('/event-types', payload);
  },
  updateType(id, payload) {
    return apiClient.put(`/event-types/${id}`, payload);
  },
  updateTypeStatus(id, active) {
    return apiClient.patch(`/event-types/${id}/status`, null, buildQuery({ active }));
  },

  listVenues(params) {
    return apiClient.get('/venues', buildQuery(params));
  },
  getVenue(id) {
    return apiClient.get(`/venues/${id}`);
  },
  createVenue(payload) {
    return apiClient.post('/venues', payload);
  },
  updateVenue(id, payload) {
    return apiClient.put(`/venues/${id}`, payload);
  },
  updateVenueStatus(id, active) {
    return apiClient.patch(`/venues/${id}/status`, null, buildQuery({ active }));
  },

  listEvents(params) {
    return apiClient.get('/events', buildQuery(params));
  },
  getEvent(id) {
    return apiClient.get(`/events/${id}`);
  },
  createEvent(payload) {
    return apiClient.post('/events', payload);
  },
  updateEvent(id, payload) {
    return apiClient.put(`/events/${id}`, payload);
  },
  updateEventStatus(id, payload) {
    return apiClient.patch(`/events/${id}/status`, payload);
  },
  getEventOverview(id) {
    return apiClient.get(`/events/${id}/overview`);
  },

  listSessions(eventId) {
    return apiClient.get(`/events/${eventId}/sessions`);
  },
  createSession(eventId, payload) {
    return apiClient.post(`/events/${eventId}/sessions`, payload);
  },
  updateSession(eventId, sessionId, payload) {
    return apiClient.put(`/events/${eventId}/sessions/${sessionId}`, payload);
  },
  updateSessionStatus(eventId, sessionId, active) {
    return apiClient.patch(`/events/${eventId}/sessions/${sessionId}/status`, null, buildQuery({ active }));
  },

  listCoordinators(eventId) {
    return apiClient.get(`/events/${eventId}/coordinators`);
  },
  assignCoordinator(eventId, payload) {
    return apiClient.post(`/events/${eventId}/coordinators`, payload);
  },
  updateCoordinator(eventId, coordinatorId, payload) {
    return apiClient.put(`/events/${eventId}/coordinators/${coordinatorId}`, payload);
  },
  removeCoordinator(eventId, coordinatorId) {
    return apiClient.delete(`/events/${eventId}/coordinators/${coordinatorId}`);
  },

  listEligibilityRules(eventId) {
    return apiClient.get(`/events/${eventId}/eligibility-rules`);
  },
  createEligibilityRule(eventId, payload) {
    return apiClient.post(`/events/${eventId}/eligibility-rules`, payload);
  },
  updateEligibilityRule(eventId, ruleId, payload) {
    return apiClient.put(`/events/${eventId}/eligibility-rules/${ruleId}`, payload);
  },
  updateEligibilityRuleStatus(eventId, ruleId, active) {
    return apiClient.patch(`/events/${eventId}/eligibility-rules/${ruleId}/status`, null, buildQuery({ active }));
  },

  getRegistrationConfig(eventId) {
    return apiClient.get(`/events/${eventId}/registration-config`);
  },
  saveRegistrationConfig(eventId, payload) {
    return apiClient.put(`/events/${eventId}/registration-config`, payload);
  }
};
