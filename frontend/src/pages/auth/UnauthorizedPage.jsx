import { Link } from 'react-router-dom';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import BrandMark from '../../components/BrandMark';
import Card from '../../components/Card';
import { APP_ROUTES } from '../../constants/routes';

export default function UnauthorizedPage() {
  return (
    <div className="state-page">
      <Card className="state-page__card" elevated>
        <BrandMark />
        <Badge tone="neutral">CampusSphere access</Badge>
        <h1>Unauthorized</h1>
        <p>You need to sign in before continuing. Your session may have expired or may not be available yet.</p>
        <div className="state-page__actions">
          <Button as={Link} to={APP_ROUTES.login}>
            Sign in
          </Button>
          <Button as={Link} variant="secondary" to={APP_ROUTES.home}>
            Home
          </Button>
        </div>
      </Card>
    </div>
  );
}
