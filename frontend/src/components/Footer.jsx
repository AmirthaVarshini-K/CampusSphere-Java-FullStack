import { Link } from 'react-router-dom';
import { APP_ROUTES } from '../constants/routes';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer__brand">
        <strong>CampusSphere</strong>
        <p>Built for colleges that need a calm, scalable way to run symposiums, workshops, and student events.</p>
      </div>

      <div className="footer__column">
        <span className="footer__title">Platform</span>
        <div className="footer__links">
          <Link to={APP_ROUTES.home}>Home</Link>
          <Link to={`${APP_ROUTES.home}#features`}>Features</Link>
          <Link to={`${APP_ROUTES.home}#institutions`}>For institutions</Link>
          <Link to={`${APP_ROUTES.home}#students`}>For students</Link>
        </div>
      </div>

      <div className="footer__column">
        <span className="footer__title">Resources</span>
        <div className="footer__links">
          <Link to={APP_ROUTES.login}>Login</Link>
          <Link to={APP_ROUTES.register}>Register</Link>
          <Link to={APP_ROUTES.forgotPassword}>Forgot password</Link>
        </div>
      </div>

      <div className="footer__column">
        <span className="footer__title">Project</span>
        <div className="footer__links">
          <Link to={`${APP_ROUTES.home}#features`}>Architecture</Link>
          <Link to="/404">404</Link>
          <span>Enterprise foundation build</span>
        </div>
      </div>

      <span className="footer__meta">CampusSphere foundation prepared for future authentication, event, and reporting modules.</span>
    </footer>
  );
}
