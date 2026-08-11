import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import Avatar from './Avatar';
import Badge from './Badge';
import Button from './Button';
import BrandMark from './BrandMark';
import Icon from './Icon';
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
  pageBreadcrumb,
  pageDescription,
  unreadCount = 0
}) {
  const location = useLocation();
  const [isMobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);

  useEffect(() => {
    setMobileMenuOpen(false);
    setSearchOpen(false);
  }, [location.pathname, location.hash]);

  if (variant === 'dashboard') {
    const roleCode = getPrimaryRole(user);
    const headline = (() => {
      if (location.pathname.startsWith('/dashboard/events')) {
        return {
          breadcrumb: location.pathname.includes('/register') ? 'CampusSphere / Registrations' : 'CampusSphere / Events',
          title: location.pathname.includes('/register') ? 'Event registration' : 'Events',
          description: location.pathname.includes('/register')
            ? 'Complete the selected registration.'
            : 'Manage event structure, sessions, and publication.'
        };
      }
      if (location.pathname.startsWith('/dashboard/notifications')) {
        return {
          breadcrumb: 'CampusSphere / Notifications',
          title: 'Notifications',
          description: 'Approvals, invitations, and updates.'
        };
      }
      if (location.pathname.startsWith('/dashboard/registrations')) {
        return {
          breadcrumb: 'CampusSphere / Registrations',
          title: 'Registrations',
          description: 'Review participant records and queue states.'
        };
      }
      if (location.pathname.startsWith('/dashboard/institution-setup')) {
        return {
          breadcrumb: 'CampusSphere / Institution setup',
          title: 'Institution setup',
          description: 'Maintain institutional master data.'
        };
      }
      return {
        breadcrumb: pageBreadcrumb ?? 'CampusSphere',
        title: pageTitle ?? 'Dashboard',
        description: pageDescription ?? 'Workspace for the current role.'
      };
    })();

    return (
      <header className="topbar topbar--dashboard">
        <div className="topbar__title">
          {showMenuButton && (
            <Button
              variant="secondary"
              size="sm"
              className="topbar__menu-button"
              onClick={onMenuToggle}
              aria-label="Toggle navigation"
            >
              <Icon name="menu" />
              Menu
            </Button>
          )}
          <div className="topbar__title-copy">
            <span className="topbar__eyebrow">{headline.breadcrumb}</span>
            <strong>{headline.title}</strong>
            <p>{headline.description}</p>
          </div>
        </div>

        <div className="topbar__actions">
          <div className="topbar__search">
            <Icon name="search" size={16} />
            <input
              type="search"
              placeholder="Search registrations, events, or students"
              aria-label="Search"
              onFocus={() => setSearchOpen(true)}
              onBlur={() => setSearchOpen(false)}
            />
            <kbd>Ctrl+K</kbd>
          </div>

          <Button as={Link} variant="secondary" size="sm" className="topbar__chip" to={`${APP_ROUTES.dashboard}/notifications`}>
            <Icon name="bell" size={16} />
            <span>Updates</span>
            <Badge tone="neutral">{unreadCount}</Badge>
          </Button>

          <Button variant="secondary" size="sm" className="topbar__chip" onClick={() => {}}>
            <Icon name="building" size={16} />
            <span>Campus switcher</span>
          </Button>

          <div className="topbar__profile">
            <Avatar src={user?.profilePictureUrl} name={user?.fullName} />
            <div>
              <strong>{buildDisplayName(user)}</strong>
              <span>{user?.email ?? 'Session restored locally'}</span>
            </div>
            <RoleBadge role={roleCode} />
          </div>

          <Button variant="secondary" size="sm" onClick={onLogout}>
            Logout
          </Button>
        </div>

        {searchOpen && <div className="topbar__hint">Press enter to keep the current shell focused.</div>}
      </header>
    );
  }

  return (
    <header className={classNames('topbar', 'topbar--public', isMobileMenuOpen && 'topbar--open')}>
      <div className="topbar__brand">
        <Link to={APP_ROUTES.home} className="topbar__brand-link">
          <BrandMark />
        </Link>
        {showMenuButton && (
          <Button
            variant="secondary"
            size="sm"
            className="topbar__menu-button"
            onClick={() => setMobileMenuOpen(current => !current)}
            aria-label="Toggle navigation"
            aria-expanded={isMobileMenuOpen}
          >
            <Icon name="menu" />
            Menu
          </Button>
        )}
      </div>

      <nav className="topbar__nav" aria-label="Primary">
        {publicNavItems.map(item => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => classNames('topbar__link', isActive && 'topbar__link--active')}
            onClick={() => setMobileMenuOpen(false)}
          >
            <Icon name={item.icon} size={16} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="topbar__actions topbar__actions--public">
        <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.login}>
          Login
        </Button>
        <Button as={Link} size="sm" to={APP_ROUTES.register}>
          Register
        </Button>
      </div>

      <div className="topbar__drawer">
        <nav className="topbar__nav topbar__nav--drawer" aria-label="Mobile primary">
          {publicNavItems.map(item => (
            <NavLink
              key={`${item.path}-mobile`}
              to={item.path}
              className={({ isActive }) => classNames('topbar__link', isActive && 'topbar__link--active')}
              onClick={() => setMobileMenuOpen(false)}
            >
              <Icon name={item.icon} size={16} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="topbar__actions topbar__actions--public">
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
