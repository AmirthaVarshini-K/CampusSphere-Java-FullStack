import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import BrandMark from '../components/BrandMark';
import Card from '../components/Card';
import { APP_ROUTES } from '../constants/routes';

export default function NotFoundPage() {
  return (
    <div className="state-page">
      <Card className="state-page__card" elevated>
        <BrandMark />
        <Badge tone="neutral">CampusSphere route</Badge>
        <h1>Page not found</h1>
        <p>The route you requested is not available in this build. Use the links below to return to the product.</p>
        <div className="state-page__actions">
          <Button as={Link} to={APP_ROUTES.home}>
            Go to home
          </Button>
          <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
            Go to login
          </Button>
        </div>
      </Card>
    </div>
  );
}
