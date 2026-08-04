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
  listWaitlist(params) {
    return apiClient.get('/registrations/waitlist', buildQuery(params));
  },
  listMyWaitlist() {
    return apiClient.get('/registrations/me/waitlist');
  },
  getEventRegistrationContext(eventId) {
    return apiClient.get(`/events/${eventId}/registration-context`);
  },
  getEventRegistrationForm(eventId) {
    return apiClient.get(`/events/${eventId}/registration-form`);
  },
  previewRegistration(eventId, payload) {
    return apiClient.post(`/events/${eventId}/registration-preview`, payload);
  },
  registerForEvent(eventId, payload) {
    return apiClient.post(`/events/${eventId}/register`, payload);
  },
  createTeam(eventId, payload) {
    return apiClient.post(`/events/${eventId}/teams`, payload);
  },
  listMyTeams() {
    return apiClient.get('/teams/me');
  },
  listMyTeamInvitations() {
    return apiClient.get('/team-invitations/me');
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
  listNotifications(params) {
    return apiClient.get('/notifications', buildQuery(params));
  },
  getUnreadNotificationCount() {
    return apiClient.get('/notifications/unread-count');
  },
  markAllNotificationsRead() {
    return apiClient.post('/notifications/mark-all-read');
  },
  markNotificationRead(id) {
    return apiClient.patch(`/notifications/${id}/read`);
  },
  promoteWaitlistEntry(registrationId) {
    return apiClient.post(`/registrations/${registrationId}/promote`);
  },
  listTeamInvitations(teamId) {
    return apiClient.get(`/teams/${teamId}/invitations`);
  },
  updateTeam(teamId, payload) {
    return apiClient.put(`/teams/${teamId}`, payload);
  },
  cancelInvitation(invitationId) {
    return apiClient.post(`/team-invitations/${invitationId}/cancel`);
  },
  removeTeamMember(teamId, memberId) {
    return apiClient.post(`/teams/${teamId}/members/${memberId}/remove`);
  },
  deleteTeam(teamId) {
    return apiClient.post(`/teams/${teamId}/delete`);
  }
};
