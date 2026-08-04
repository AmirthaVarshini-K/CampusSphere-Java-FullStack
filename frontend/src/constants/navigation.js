import { APP_ROUTES } from './routes';

export const PUBLIC_NAV_ITEMS = [
  { label: 'Home', path: APP_ROUTES.home },
  { label: 'Features', path: `${APP_ROUTES.home}#features` },
  { label: 'For Institutions', path: `${APP_ROUTES.home}#institutions` },
  { label: 'For Students', path: `${APP_ROUTES.home}#students` }
];

export const DASHBOARD_NAV_ITEMS = [
  { label: 'Overview', path: APP_ROUTES.dashboard, group: 'Workspace' },
  { label: 'Profile', path: `${APP_ROUTES.dashboard}/profile`, group: 'Workspace' },
  { label: 'Security', path: `${APP_ROUTES.dashboard}/security`, group: 'Workspace' },
  { label: 'Activity', path: `${APP_ROUTES.dashboard}/activity`, group: 'Workspace' },
  { label: 'Institution Setup', path: `${APP_ROUTES.dashboard}/institution-setup`, group: 'Operations' },
  { label: 'Events', path: `${APP_ROUTES.dashboard}/events`, group: 'Operations' },
  { label: 'Registrations', path: APP_ROUTES.registrations, group: 'Operations' },
  { label: 'Notifications', path: `${APP_ROUTES.dashboard}/notifications`, group: 'Support' }
];
