import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import Avatar from './Avatar';
import Button from './Button';
import RoleBadge from './RoleBadge';
import { classNames } from '../utils/classNames';
import { APP_ROUTES } from '../constants/routes';
import { buildDisplayName, getPrimaryRole } from '../utils/auth';

export default function Navbar({
  variant = 'public',
  onMenuToggle,
  showMenuButton = true,
  publicNavItems = [],
  user,
  onLogout,
  pageTitle,
  pageBreadcrumb
}) {
  const location = useLocation();
  const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname, location.hash]);

  if (variant === 'dashboard') {
    const roleCode = getPrimaryRole(user);
    const routeTitle = location.pathname.startsWith('/dashboard/events')
      ? (location.pathname.includes('/register')
        ? { breadcrumb: 'CampusSphere / Registrations', title: 'Event Registration' }
        : { breadcrumb: 'CampusSphere / Events', title: 'Events' })
      : location.pathname.startsWith('/dashboard/registrations')
        ? { breadcrumb: 'CampusSphere / Registrations', title: 'Registrations' }
        : location.pathname.startsWith('/dashboard/institution-setup')
        ? { breadcrumb: 'CampusSphere / Institution Setup', title: 'Institution Setup' }
        : null;
    const resolvedTitle = routeTitle ?? { breadcrumb: pageBreadcrumb ?? 'CampusSphere workspace', title: pageTitle ?? 'Dashboard' };

    return (
      <header className="navbar navbar--dashboard">
        <div className="navbar__left">
          {showMenuButton && (
            <Button
              variant="secondary"
              size="sm"
              className="navbar__menu-button"
              onClick={onMenuToggle}
              aria-label="Toggle navigation"
            >
              Menu
            </Button>
          )}
          <div className="navbar__title-block">
            <div className="navbar__crumb">{resolvedTitle.breadcrumb}</div>
            <Link to={APP_ROUTES.dashboard} className="navbar__brand">
              {resolvedTitle.title}
            </Link>
          </div>
        </div>
        <div className="navbar__actions navbar__actions--dashboard">
          <div className="navbar__profile">
            <Avatar src={user?.profilePictureUrl} name={user?.fullName} />
            <div className="navbar__profile-copy">
              <strong>{buildDisplayName(user)}</strong>
              <span>{user?.email ?? 'Session restored locally'}</span>
            </div>
            <RoleBadge role={roleCode} />
          </div>
          <Button variant="secondary" size="sm" onClick={onLogout}>
            Logout
          </Button>
        </div>
      </header>
    );
  }

  return (
    <header className={`navbar navbar--public ${isMobileMenuOpen ? 'navbar--open' : ''}`}>
      <div className="navbar__left">
        <Link to={APP_ROUTES.home} className="navbar__brand navbar__brand--public">
          <span className="navbar__mark" aria-hidden="true">
            C
          </span>
          <span>
            CampusSphere
            <small>One platform. Every event.</small>
          </span>
        </Link>
        {showMenuButton && (
          <Button
            variant="secondary"
            size="sm"
            className="navbar__menu-button"
            onClick={() => setMobileMenuOpen(current => !current)}
            aria-label="Toggle navigation"
            aria-expanded={isMobileMenuOpen}
          >
            Menu
          </Button>
        )}
      </div>

      <nav className="navbar__nav" aria-label="Primary">
        {publicNavItems.map(item => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => classNames('navbar__link', isActive && 'navbar__link--active')}
            onClick={() => setMobileMenuOpen(false)}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="navbar__actions">
        <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.login}>
          Login
        </Button>
        <Button as={Link} size="sm" to={APP_ROUTES.register}>
          Register
        </Button>
      </div>

      <div className="navbar__mobile-panel">
        <nav className="navbar__nav" aria-label="Mobile primary">
          {publicNavItems.map(item => (
            <NavLink
              key={`${item.path}-mobile`}
              to={item.path}
              className={({ isActive }) => classNames('navbar__link', isActive && 'navbar__link--active')}
              onClick={() => setMobileMenuOpen(false)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="navbar__actions">
          <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.login} onClick={() => setMobileMenuOpen(false)}>
            Login
          </Button>
          <Button as={Link} size="sm" to={APP_ROUTES.register} onClick={() => setMobileMenuOpen(false)}>
            Register
          </Button>
        </div>
      </div>
    </header>
  );
}
