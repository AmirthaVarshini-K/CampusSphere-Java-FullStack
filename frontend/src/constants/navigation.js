import { APP_ROUTES } from './routes';

export const PUBLIC_NAV_ITEMS = [
  { label: 'Home', path: APP_ROUTES.home },
  { label: 'Features', path: `${APP_ROUTES.home}#features` },
  { label: 'For Institutions', path: `${APP_ROUTES.home}#institutions` },
  { label: 'For Students', path: `${APP_ROUTES.home}#students` }
];

export const DASHBOARD_NAV_ITEMS = [
  { label: 'Overview', path: APP_ROUTES.dashboard },
  { label: 'Profile', path: `${APP_ROUTES.dashboard}/profile` },
  { label: 'Security', path: `${APP_ROUTES.dashboard}/security` },
  { label: 'Activity', path: `${APP_ROUTES.dashboard}/activity` },
  { label: 'Institution Setup', path: `${APP_ROUTES.dashboard}/institution-setup` }
];
