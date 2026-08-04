import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { DASHBOARD_NAV_ITEMS } from '../constants/navigation';
import { useAppShell } from '../context/AppShellContext';
import { useAuth } from '../context/AuthContext';
import { useMediaQuery } from '../hooks/useMediaQuery';
import { registrationApi } from '../services/registrationApi';
import { getPrimaryRole } from '../utils/auth';

const DASHBOARD_TITLES = {
  '/dashboard': { breadcrumb: 'CampusSphere / Dashboard', title: 'Dashboard', description: 'A calm workspace for the currently signed-in role.' },
  '/dashboard/profile': { breadcrumb: 'CampusSphere / Profile', title: 'Profile', description: 'Review and complete the account details tied to your session.' },
  '/dashboard/security': { breadcrumb: 'CampusSphere / Security', title: 'Security', description: 'Password and session controls prepared for the current build.' },
  '/dashboard/activity': { breadcrumb: 'CampusSphere / Activity', title: 'Activity', description: 'Recent changes, logins, and workspace actions appear here.' },
  '/dashboard/institution-setup': { breadcrumb: 'CampusSphere / Institution Setup', title: 'Institution Setup', description: 'Manage the master data that shapes the rest of the platform.' },
  '/dashboard/events': { breadcrumb: 'CampusSphere / Events', title: 'Events', description: 'Organise symposiums, venues, sessions, and publication states.' },
  '/dashboard/notifications': { breadcrumb: 'CampusSphere / Notifications', title: 'Notifications', description: 'Track approvals, invitations, and workspace updates in one place.' },
  '/dashboard/registrations': { breadcrumb: 'CampusSphere / Registrations', title: 'Registrations', description: 'Review participant records, waitlists, and team workflows.' },
  '/dashboard/attendance': { breadcrumb: 'CampusSphere / Attendance', title: 'Attendance', description: 'Check in participants, review history, and prepare reports.' },
  '/dashboard/attendance/scanner': { breadcrumb: 'CampusSphere / Attendance', title: 'Attendance Scanner', description: 'Scan QR tokens or record manual attendance for active sessions.' },
  '/dashboard/attendance/history': { breadcrumb: 'CampusSphere / Attendance', title: 'Attendance History', description: 'Search attendance records and review the latest audit trail.' },
  '/dashboard/attendance/reports': { breadcrumb: 'CampusSphere / Attendance', title: 'Attendance Reports', description: 'Export attendance summaries and certificate readiness views.' },
  '/dashboard/events/register': { breadcrumb: 'CampusSphere / Registrations', title: 'Event Registration', description: 'Complete a focused registration flow for the selected event.' }
};

export default function DashboardLayout() {
  const { isSidebarOpen, toggleSidebar, closeSidebar } = useAppShell();
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 1024px)');
  const roleCode = getPrimaryRole(user);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadUnreadCount() {
      try {
        const response = await registrationApi.getUnreadNotificationCount();
        if (!active) {
          return;
        }
        setUnreadCount(Number(response?.data?.data ?? 0));
      } catch {
        if (active) {
          setUnreadCount(0);
        }
      }
    }

    if (user) {
      loadUnreadCount();
    } else {
      setUnreadCount(0);
    }

    return () => {
      active = false;
    };
  }, [user]);
  const current = location.pathname.startsWith('/dashboard/events')
    ? (location.pathname.includes('/register') ? DASHBOARD_TITLES['/dashboard/events/register'] : DASHBOARD_TITLES['/dashboard/events'])
    : location.pathname.startsWith('/dashboard/notifications')
      ? DASHBOARD_TITLES['/dashboard/notifications']
    : location.pathname.startsWith('/dashboard/attendance')
      ? (location.pathname.includes('/scanner')
        ? DASHBOARD_TITLES['/dashboard/attendance/scanner']
        : location.pathname.includes('/history')
          ? DASHBOARD_TITLES['/dashboard/attendance/history']
          : location.pathname.includes('/reports')
            ? DASHBOARD_TITLES['/dashboard/attendance/reports']
            : DASHBOARD_TITLES['/dashboard/attendance'])
    : location.pathname.startsWith('/dashboard/registrations')
      ? DASHBOARD_TITLES['/dashboard/registrations']
    : location.pathname.startsWith('/dashboard/institution-setup')
      ? DASHBOARD_TITLES['/dashboard/institution-setup']
      : DASHBOARD_TITLES[location.pathname] ?? DASHBOARD_TITLES['/dashboard'];
  const isSidebarVisible = isMobile ? isSidebarOpen : true;
  const canSeeInstitutionSetup = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR'].includes(roleCode);
  const canSeeEvents = canSeeInstitutionSetup || roleCode === 'FACULTY_COORDINATOR';
  const canSeeRegistrations = true;
  const canSeeAttendance = true;
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
    if (item.label === 'Attendance') {
      return canSeeAttendance;
    }
    return true;
  });

  async function handleLogout() {
    await signOut();
    navigate('/', { replace: true });
  }

  return (
    <div className="app-shell dashboard-shell">
      <Sidebar
        brand="CampusSphere"
        items={sidebarItems}
        collapsed={!isSidebarVisible}
        onNavigate={isMobile ? closeSidebar : undefined}
        onClose={isMobile ? closeSidebar : undefined}
        onLogout={handleLogout}
        user={user}
      />
      {isMobile && isSidebarVisible && <div className="app-shell__overlay dashboard-shell__overlay" onClick={closeSidebar} aria-hidden="true" />}
      <div className="dashboard-shell__main">
        <Navbar
          variant="dashboard"
          onMenuToggle={toggleSidebar}
          showMenuButton={isMobile}
          user={user}
          onLogout={handleLogout}
          pageTitle={current.title}
          pageBreadcrumb={current.breadcrumb}
          pageDescription={current.description}
          unreadCount={unreadCount}
        />
        <main className="app-shell__content dashboard-shell__content">
          <Outlet />
        </main>
        <Footer />
      </div>
    </div>
  );
}
