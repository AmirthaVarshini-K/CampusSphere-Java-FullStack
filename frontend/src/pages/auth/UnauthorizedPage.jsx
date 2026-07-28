import { Link } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import { APP_ROUTES } from '../../constants/routes';

export default function UnauthorizedPage() {
  return (
    <AuthCard title="Unauthorized" eyebrow="CampusSphere access" description="You need to sign in before continuing.">
      <div className="state-message">
        <p>Your session is missing, expired, or has not been restored yet. Sign in again to continue.</p>
        <div className="button-row">
          <Button as={Link} to={APP_ROUTES.login}>
            Sign in
          </Button>
          <Button as={Link} variant="secondary" to={APP_ROUTES.home}>
            Home
          </Button>
        </div>
      </div>
    </AuthCard>
  );
}
