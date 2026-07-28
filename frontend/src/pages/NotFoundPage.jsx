import { Link } from 'react-router-dom';
import AuthCard from '../components/AuthCard';
import Button from '../components/Button';
import { APP_ROUTES } from '../constants/routes';

export default function NotFoundPage() {
  return (
    <div className="not-found">
      <AuthCard title="Page not found" eyebrow="CampusSphere route" description="The route you requested is not available in this CampusSphere build.">
        <div className="state-message">
          <p>The page may have moved or the URL may be incorrect. Try the homepage or return to sign in.</p>
          <div className="button-row">
            <Button as={Link} to={APP_ROUTES.home}>
              Go to home
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
              Go to login
            </Button>
          </div>
        </div>
      </AuthCard>
    </div>
  );
}
