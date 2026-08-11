import { Link, NavLink } from 'react-router-dom';
import Avatar from './Avatar';
import Badge from './Badge';
import Button from './Button';
import BrandMark from './BrandMark';
import Icon from './Icon';
import RoleBadge from './RoleBadge';
import { classNames } from '../utils/classNames';
import { APP_ROUTES } from '../constants/routes';
import { buildDisplayName, getPrimaryRole, getRoleDescription } from '../utils/auth';

export default function Sidebar({ brand, items, collapsed = false, onNavigate, onClose, onLogout, user }) {
  const roleCode = getPrimaryRole(user);
  const groups = items.reduce((acc, item) => {
    const group = item.group ?? 'Main';
    let entry = acc.find(current => current.group === group);
    if (!entry) {
      entry = { group, items: [] };
      acc.push(entry);
    }
    entry.items.push(item);
    return acc;
  }, []);

  return (
    <aside className={classNames('app-sidebar', collapsed && 'app-sidebar--collapsed')}>
      <div className="app-sidebar__brand">
        <Link to={APP_ROUTES.dashboard} className="app-sidebar__brand-link" onClick={onNavigate}>
          <BrandMark compact={collapsed} />
        </Link>
        {onClose && (
          <Button variant="secondary" size="sm" className="app-sidebar__close" onClick={onClose} aria-label="Close navigation">
            <Icon name="chevronDown" />
          </Button>
        )}
      </div>

      <div className="app-sidebar__identity">
        <Avatar src={user?.profilePictureUrl} name={buildDisplayName(user)} />
        <div>
          <strong>{buildDisplayName(user)}</strong>
          <span>{user?.email ?? 'Connected session'}</span>
        </div>
      </div>

      <div className="app-sidebar__role">
        <Badge tone="neutral">{brand}</Badge>
        <RoleBadge role={roleCode} />
        <p>{getRoleDescription(roleCode)}</p>
      </div>

      <nav className="app-sidebar__nav" aria-label="Primary">
        {groups.map(group => (
          <section key={group.group} className="app-sidebar__group">
            <span className="app-sidebar__group-label">{group.group}</span>
            <div className="app-sidebar__links">
              {group.items.map(item => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) => classNames('app-sidebar__link', isActive && 'app-sidebar__link--active')}
                  onClick={onNavigate}
                >
                  <Icon name={item.icon} size={17} />
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </div>
          </section>
        ))}
      </nav>

      <div className="app-sidebar__panel">
        <strong>Protected access</strong>
        <p>All restricted actions remain behind JWT, institution scope, and role checks.</p>
      </div>

      <div className="app-sidebar__footer">
        <Button variant="secondary" size="sm" className="app-sidebar__logout" onClick={onLogout}>
          <Icon name="logout" size={16} />
          Logout
        </Button>
      </div>
    </aside>
  );
}
