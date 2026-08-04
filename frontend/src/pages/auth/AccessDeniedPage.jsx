import { Link } from 'react-router-dom';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import BrandMark from '../../components/BrandMark';
import Card from '../../components/Card';
import { APP_ROUTES } from '../../constants/routes';

export default function AccessDeniedPage() {
  return (
    <div className="state-page">
      <Card className="state-page__card" elevated>
        <BrandMark />
        <Badge tone="neutral">CampusSphere permissions</Badge>
        <h1>Access denied</h1>
        <p>The current account does not have permission for this action. Ask an administrator if you believe this is incorrect.</p>
        <div className="state-page__actions">
          <Button as={Link} to={APP_ROUTES.dashboard}>
            Return to dashboard
          </Button>
          <Button as={Link} variant="secondary" to={APP_ROUTES.home}>
            Home
          </Button>
        </div>
      </Card>
    </div>
  );
}
