import { Link, NavLink } from 'react-router-dom';
import Avatar from './Avatar';
import Button from './Button';
import RoleBadge from './RoleBadge';
import { classNames } from '../utils/classNames';
import { APP_ROUTES } from '../constants/routes';
import { buildDisplayName, getPrimaryRole, getRoleDescription } from '../utils/auth';

export default function Sidebar({ brand, items, collapsed = false, onNavigate, onClose, onLogout, user }) {
  const roleCode = getPrimaryRole(user);

  return (
    <aside className={classNames('sidebar', collapsed && 'sidebar--collapsed')}>
      <div className="sidebar__header">
        <div className="sidebar__header-top">
          <Link to={APP_ROUTES.dashboard} className="sidebar__brand" onClick={onNavigate}>
            <span className="sidebar__logo" aria-hidden="true">
              C
            </span>
            <span className="sidebar__brand-copy">
              <strong>{brand}</strong>
              <small>{getRoleDescription(roleCode)}</small>
            </span>
          </Link>
          {onClose && (
            <Button variant="secondary" size="sm" className="navbar__mobile-close" onClick={onClose} aria-label="Close navigation">
              Close
            </Button>
          )}
        </div>

        <div className="sidebar__profile">
          <Avatar src={user?.profilePictureUrl} name={buildDisplayName(user)} />
          <div>
            <strong>{buildDisplayName(user)}</strong>
            <p>{user?.email ?? 'Connected session'}</p>
          </div>
        </div>

        <RoleBadge role={roleCode} />
      </div>

      <nav className="sidebar__nav" aria-label="Primary">
        {items.map(item => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => classNames('sidebar__link', isActive && 'sidebar__link--active')}
            onClick={onNavigate}
          >
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar__note">
        <span>Protected workspace</span>
        <small>All restricted actions remain behind JWT and role checks.</small>
      </div>

      <div className="sidebar__footer">
        <Button variant="secondary" size="sm" className="sidebar__logout" onClick={onLogout}>
          Logout
        </Button>
      </div>
    </aside>
  );
}
