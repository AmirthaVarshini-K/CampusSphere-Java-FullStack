import { Outlet } from 'react-router-dom';
import BrandMark from '../components/BrandMark';
import Badge from '../components/Badge';
import Card from '../components/Card';
import MetricCard from '../components/MetricCard';
import Timeline from '../components/Timeline';

const pillars = [
  { label: 'Secure entry', value: 'JWT-ready', detail: 'Login, recovery, and protected routes share one session model.', tone: 'neutral' },
  { label: 'Role routing', value: 'Role aware', detail: 'Administrators, organisers, faculty, and students keep separate paths.', tone: 'neutral' },
  { label: 'Offline-safe', value: 'Public first', detail: 'Public pages remain visible when the backend is unavailable.', tone: 'success' }
];

const steps = [
  { title: 'Sign in', description: 'Use email, register number, or employee ID.', icon: 'shield', tone: 'neutral' },
  { title: 'Review profile', description: 'Complete profile details before continuing.', icon: 'usersSquare', tone: 'neutral' },
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
          <p className="auth-shell__eyebrow">CampusSphere access</p>
          <h1>Event & Symposium Management</h1>
          <p>Manage events, registrations, attendance, and certificates.</p>
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
              <h2>After sign-in</h2>
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
