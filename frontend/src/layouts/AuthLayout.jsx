import { Outlet } from 'react-router-dom';
import BrandMark from '../components/BrandMark';
import Badge from '../components/Badge';
import Card from '../components/Card';
import MetricCard from '../components/MetricCard';
import Timeline from '../components/Timeline';

const pillars = [
  { label: 'Secure entry', value: 'JWT-ready', detail: 'Login, recovery, and protected routes share one session model.', tone: 'neutral' },
  { label: 'Role routing', value: 'Role aware', detail: 'Administrators, organisers, faculty, and students keep separate paths.', tone: 'neutral' },
  { label: 'Offline-safe', value: 'Public first', detail: 'Landing and auth pages remain visible even when the backend is unavailable.', tone: 'success' }
];

const steps = [
  { title: 'Sign in', description: 'Use email, register number, or employee ID to access the workspace.', icon: 'shield', tone: 'neutral' },
  { title: 'Review profile', description: 'Complete profile details before event and registration workflows unlock.', icon: 'usersSquare', tone: 'neutral' },
  { title: 'Continue safely', description: 'Password reset, session refresh, and protected routes stay consistent.', icon: 'clock', tone: 'success' }
];

export default function AuthLayout() {
  return (
    <div className="auth-shell">
      <section className="auth-shell__panel">
        <div className="auth-shell__glow auth-shell__glow--one" aria-hidden="true" />
        <div className="auth-shell__glow auth-shell__glow--two" aria-hidden="true" />

        <div className="auth-shell__brand">
          <BrandMark />
          <Badge tone="neutral">CampusSphere access</Badge>
        </div>

        <div className="auth-shell__hero">
          <p className="auth-shell__eyebrow">Built for colleges, organisers, and students</p>
          <h1>One signed-in workspace for every event journey.</h1>
          <p>
            CampusSphere keeps authentication, onboarding, and future event workflows aligned so the experience feels like
            part of a single product instead of a detached form stack.
          </p>
        </div>

        <div className="auth-shell__metrics">
          {pillars.map(item => (
            <MetricCard key={item.label} {...item} />
          ))}
        </div>

        <Card className="auth-shell__board" elevated>
          <div className="auth-shell__board-head">
            <div>
              <Badge tone="neutral">Access path</Badge>
              <h2>What happens after sign-in</h2>
            </div>
            <Badge tone="success">Protected</Badge>
          </div>
          <Timeline items={steps} />
        </Card>
      </section>

      <section className="auth-shell__surface">
        <Outlet />
      </section>
    </div>
  );
}
