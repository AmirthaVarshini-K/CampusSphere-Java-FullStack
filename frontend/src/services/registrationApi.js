import apiClient from './apiClient';

function buildQuery(params = {}) {
  return {
    params
  };
}

export const registrationApi = {
  getDashboard() {
    return apiClient.get('/registrations/dashboard');
  },
  listRegistrations(params) {
    return apiClient.get('/registrations', buildQuery(params));
  },
  listMyRegistrations(params) {
    return apiClient.get('/registrations/me', buildQuery(params));
  },
  getEventRegistrationContext(eventId) {
    return apiClient.get(`/events/${eventId}/registration-context`);
  },
  getEventRegistrationForm(eventId) {
    return apiClient.get(`/events/${eventId}/registration-form`);
  },
  registerForEvent(eventId, payload) {
    return apiClient.post(`/events/${eventId}/register`, payload);
  },
  createTeam(eventId, payload) {
    return apiClient.post(`/events/${eventId}/teams`, payload);
  },
  listTeams(eventId) {
    return apiClient.get(`/events/${eventId}/teams`);
  },
  listTeamMembers(teamId) {
    return apiClient.get(`/teams/${teamId}/members`);
  },
  inviteTeamMember(teamId, payload) {
    return apiClient.post(`/teams/${teamId}/invitations`, payload);
  },
  acceptInvitation(invitationId) {
    return apiClient.post(`/team-invitations/${invitationId}/accept`);
  },
  rejectInvitation(invitationId) {
    return apiClient.post(`/team-invitations/${invitationId}/reject`);
  },
  transferTeamOwnership(teamId, payload) {
    return apiClient.put(`/teams/${teamId}/transfer`, payload);
  },
  leaveTeam(teamId) {
    return apiClient.post(`/teams/${teamId}/leave`);
  },
  decideRegistration(registrationId, payload) {
    return apiClient.post(`/registrations/${registrationId}/decision`, payload);
  },
  cancelRegistration(registrationId) {
    return apiClient.post(`/registrations/${registrationId}/cancel`);
  },
  listNotifications() {
    return apiClient.get('/notifications');
  },
  markNotificationRead(id) {
    return apiClient.patch(`/notifications/${id}/read`);
  }
};
