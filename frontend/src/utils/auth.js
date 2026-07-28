import { APP_ROUTES } from '../constants/routes';

export const ROLE_LABELS = {
  SUPER_ADMIN: 'Super Admin',
  INSTITUTION_ADMIN: 'Institution Admin',
  ORGANISER: 'Organiser',
  ADMINISTRATOR: 'Administrator',
  FACULTY_COORDINATOR: 'Faculty Coordinator',
  STUDENT: 'Student'
};

export const ROLE_DESCRIPTIONS = {
  SUPER_ADMIN: 'Complete system access and governance',
  INSTITUTION_ADMIN: 'College-wide administration and oversight',
  ORGANISER: 'Event coordination and operations',
  ADMINISTRATOR: 'System-wide access and configuration',
  FACULTY_COORDINATOR: 'Academic event coordination and approvals',
  STUDENT: 'Event participation and profile management'
};

export function getPrimaryRole(user) {
  return user?.roles?.[0]?.code ?? 'STUDENT';
}

export function getRoleLabel(roleCode) {
  return ROLE_LABELS[roleCode] ?? roleCode ?? 'Student';
}

export function getRoleDescription(roleCode) {
  return ROLE_DESCRIPTIONS[roleCode] ?? 'CampusSphere access role';
}

export function getDashboardRoute() {
  return APP_ROUTES.dashboard;
}

export function buildDisplayName(user) {
  if (!user) {
    return 'CampusSphere user';
  }

  return user.fullName || [user.firstName, user.lastName].filter(Boolean).join(' ') || user.email || 'CampusSphere user';
}
