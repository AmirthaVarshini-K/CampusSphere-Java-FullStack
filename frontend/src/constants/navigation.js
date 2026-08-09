import { APP_ROUTES } from './routes';

export const PUBLIC_NAV_ITEMS = [
  { label: 'Home', path: APP_ROUTES.home, icon: 'home' },
  { label: 'Platform', path: `${APP_ROUTES.home}#platform`, icon: 'grid' },
  { label: 'For Institutions', path: `${APP_ROUTES.home}#institutions`, icon: 'building' },
  { label: 'For Students', path: `${APP_ROUTES.home}#students`, icon: 'users' }
];

export const DASHBOARD_NAV_ITEMS = [
  { label: 'Overview', path: APP_ROUTES.dashboard, group: 'Workspace', icon: 'home' },
  { label: 'Profile', path: `${APP_ROUTES.dashboard}/profile`, group: 'Workspace', icon: 'usersSquare' },
  { label: 'Security', path: `${APP_ROUTES.dashboard}/security`, group: 'Workspace', icon: 'shield' },
  { label: 'Activity', path: `${APP_ROUTES.dashboard}/activity`, group: 'Workspace', icon: 'pulse' },
  { label: 'Institution Setup', path: `${APP_ROUTES.dashboard}/institution-setup`, group: 'Operations', icon: 'building' },
  { label: 'Events', path: `${APP_ROUTES.dashboard}/events`, group: 'Operations', icon: 'calendar' },
  { label: 'Registrations', path: APP_ROUTES.registrations, group: 'Operations', icon: 'users' },
  { label: 'Attendance', path: APP_ROUTES.attendance, group: 'Operations', icon: 'clock' },
  { label: 'Analytics', path: APP_ROUTES.analytics, group: 'Insights', icon: 'pulse' },
  { label: 'Reports', path: APP_ROUTES.reports, group: 'Insights', icon: 'fileText' },
  { label: 'Certificates', path: APP_ROUTES.certificates, group: 'Operations', icon: 'award' },
  { label: 'Notifications', path: `${APP_ROUTES.dashboard}/notifications`, group: 'Support', icon: 'bell' }
];
