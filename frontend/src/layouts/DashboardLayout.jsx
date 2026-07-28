import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { DASHBOARD_NAV_ITEMS } from '../constants/navigation';
import { useAppShell } from '../context/AppShellContext';
import { useAuth } from '../context/AuthContext';
import { useMediaQuery } from '../hooks/useMediaQuery';

const DASHBOARD_TITLES = {
  '/dashboard': { breadcrumb: 'CampusSphere / Dashboard', title: 'Dashboard' },
  '/dashboard/profile': { breadcrumb: 'CampusSphere / Profile', title: 'Profile' },
  '/dashboard/security': { breadcrumb: 'CampusSphere / Security', title: 'Security' },
  '/dashboard/activity': { breadcrumb: 'CampusSphere / Activity', title: 'Activity' }
};

export default function DashboardLayout() {
  const { isSidebarOpen, toggleSidebar, closeSidebar } = useAppShell();
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 1024px)');
  const current = DASHBOARD_TITLES[location.pathname] ?? DASHBOARD_TITLES['/dashboard'];
  const isSidebarVisible = isMobile ? isSidebarOpen : true;

  async function handleLogout() {
    await signOut();
    navigate('/', { replace: true });
  }

  return (
    <div className="dashboard-shell">
      <Sidebar
        brand="CampusSphere"
        items={DASHBOARD_NAV_ITEMS}
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
