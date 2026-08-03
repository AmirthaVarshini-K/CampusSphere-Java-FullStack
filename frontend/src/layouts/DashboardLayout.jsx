import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { DASHBOARD_NAV_ITEMS } from '../constants/navigation';
import { useAppShell } from '../context/AppShellContext';
import { useAuth } from '../context/AuthContext';
import { useMediaQuery } from '../hooks/useMediaQuery';
import { getPrimaryRole } from '../utils/auth';

const DASHBOARD_TITLES = {
  '/dashboard': { breadcrumb: 'CampusSphere / Dashboard', title: 'Dashboard' },
  '/dashboard/profile': { breadcrumb: 'CampusSphere / Profile', title: 'Profile' },
  '/dashboard/security': { breadcrumb: 'CampusSphere / Security', title: 'Security' },
  '/dashboard/activity': { breadcrumb: 'CampusSphere / Activity', title: 'Activity' },
  '/dashboard/institution-setup': { breadcrumb: 'CampusSphere / Institution Setup', title: 'Institution Setup' },
  '/dashboard/events': { breadcrumb: 'CampusSphere / Events', title: 'Events' },
  '/dashboard/registrations': { breadcrumb: 'CampusSphere / Registrations', title: 'Registrations' },
  '/dashboard/events/register': { breadcrumb: 'CampusSphere / Registrations', title: 'Event Registration' }
};

export default function DashboardLayout() {
  const { isSidebarOpen, toggleSidebar, closeSidebar } = useAppShell();
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 1024px)');
  const roleCode = getPrimaryRole(user);
  const current = location.pathname.startsWith('/dashboard/events')
    ? (location.pathname.includes('/register') ? DASHBOARD_TITLES['/dashboard/events/register'] : DASHBOARD_TITLES['/dashboard/events'])
    : location.pathname.startsWith('/dashboard/registrations')
      ? DASHBOARD_TITLES['/dashboard/registrations']
    : location.pathname.startsWith('/dashboard/institution-setup')
      ? DASHBOARD_TITLES['/dashboard/institution-setup']
      : DASHBOARD_TITLES[location.pathname] ?? DASHBOARD_TITLES['/dashboard'];
  const isSidebarVisible = isMobile ? isSidebarOpen : true;
  const canSeeInstitutionSetup = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR'].includes(roleCode);
  const canSeeEvents = canSeeInstitutionSetup || roleCode === 'FACULTY_COORDINATOR';
  const canSeeRegistrations = true;
  const sidebarItems = DASHBOARD_NAV_ITEMS.filter(item => {
    if (item.label === 'Institution Setup') {
      return canSeeInstitutionSetup;
    }
    if (item.label === 'Events') {
      return canSeeEvents;
    }
    if (item.label === 'Registrations') {
      return canSeeRegistrations;
    }
    return true;
  });

  async function handleLogout() {
    await signOut();
    navigate('/', { replace: true });
  }

  return (
    <div className="dashboard-shell">
      <Sidebar
        brand="CampusSphere"
        items={sidebarItems}
        collapsed={!isSidebarVisible}
        onNavigate={isMobile ? closeSidebar : undefined}
        onClose={isMobile ? closeSidebar : undefined}
        onLogout={handleLogout}
        user={user}
      />
      {isMobile && isSidebarVisible && <div className="dashboard-shell__overlay" onClick={closeSidebar} aria-hidden="true" />}
      <div className="dashboard-shell__main">
        <Navbar
          variant="dashboard"
          onMenuToggle={toggleSidebar}
          showMenuButton={isMobile}
          user={user}
          onLogout={handleLogout}
          pageTitle={current.title}
          pageBreadcrumb={current.breadcrumb}
        />
        <main className="dashboard-shell__content">
          <Outlet />
        </main>
        <Footer />
      </div>
    </div>
  );
}
