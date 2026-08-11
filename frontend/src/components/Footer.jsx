import { Link } from 'react-router-dom';
import { APP_ROUTES } from '../constants/routes';
import BrandMark from './BrandMark';
import Icon from './Icon';

const columns = [
  {
    title: 'Platform',
    links: [
      { label: 'Home', to: APP_ROUTES.home },
      { label: 'Features', to: `${APP_ROUTES.home}#platform` },
      { label: 'For institutions', to: `${APP_ROUTES.home}#institutions` },
      { label: 'For students', to: `${APP_ROUTES.home}#students` }
    ]
  },
  {
    title: 'Account',
    links: [
      { label: 'Login', to: APP_ROUTES.login },
      { label: 'Register', to: APP_ROUTES.register },
      { label: 'Forgot password', to: APP_ROUTES.forgotPassword }
    ]
  },
  {
    title: 'Project',
    links: [
      { label: 'Architecture', to: `${APP_ROUTES.home}#platform` },
      { label: 'Not found', to: '/404' },
      { label: 'Unauthorized', to: APP_ROUTES.unauthorized }
    ]
  }
];

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="site-footer__brand">
        <BrandMark compact />
        <p>CampusSphere manages events, registrations, attendance, and certificates.</p>
      </div>

      {columns.map(column => (
        <div key={column.title} className="site-footer__column">
          <strong>{column.title}</strong>
          <nav>
            {column.links.map(link => (
              <Link key={link.label} to={link.to}>
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
      ))}

      <div className="site-footer__meta">
        <span>
          <Icon name="spark" size={14} />
          Enterprise foundation build
        </span>
        <p>Built for institutional event operations.</p>
      </div>
    </footer>
  );
}
