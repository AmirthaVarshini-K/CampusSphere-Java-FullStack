import { Link } from 'react-router-dom';
import Button from '../components/Button';
import Card from '../components/Card';
import Input from '../components/Input';
import { APP_ROUTES } from '../constants/routes';

export default function AuthenticationLandingPage() {
  return (
    <div className="auth-page">
      <div className="auth-page__header">
        <h2>Authentication foundation</h2>
        <p>JWT, password encryption, protected APIs, and role-based authorization will integrate here.</p>
      </div>
      <div className="auth-form">
        <Input id="email" label="Work email" type="email" placeholder="admin@campusphere.edu" />
        <Input id="password" label="Password" type="password" placeholder="Enter password" />
        <Button fullWidth>Sign in</Button>
      </div>
      <Card className="auth-page__note">
        <p>This shell is intentionally non-functional until the security module is introduced.</p>
        <Link to={APP_ROUTES.home}>Return to overview</Link>
      </Card>
    </div>
  );
}
