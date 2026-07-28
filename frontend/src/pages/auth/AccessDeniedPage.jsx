import { Link } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import { APP_ROUTES } from '../../constants/routes';

export default function AccessDeniedPage() {
  return (
    <AuthCard title="Access denied" eyebrow="CampusSphere permissions" description="Your account does not have permission for this action.">
      <div className="state-message">
        <p>The requested resource is protected by role-based authorization. Please contact your administrator if this feels incorrect.</p>
        <div className="button-row">
          <Button as={Link} to={APP_ROUTES.dashboard}>
            Return to dashboard
          </Button>
          <Button as={Link} variant="secondary" to={APP_ROUTES.home}>
            Home
          </Button>
        </div>
      </div>
    </AuthCard>
  );
}
