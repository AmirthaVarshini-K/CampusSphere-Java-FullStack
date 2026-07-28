import { Outlet } from 'react-router-dom';
import Badge from '../components/Badge';
import Card from '../components/Card';
import StatusIndicator from '../components/StatusIndicator';

const highlights = [
  {
    title: 'JWT-ready authentication pipeline',
    detail: 'Structured login, logout, session refresh, and password recovery flows share one API layer.'
  },
  {
    title: 'Role-aware access control',
    detail: 'Students, faculty coordinators, organisers, and administrators can use different dashboards later.'
  },
  {
    title: 'Offline-safe public pages',
    detail: 'Landing, login, and recovery screens remain visible even when the backend is unavailable.'
  }
];

const accessNotes = [
  'Email, register number, or employee ID sign-in',
  'Password recovery with user-friendly error states',
  'Clean mobile forms with visible focus states'
];

export default function AuthLayout() {
  return (
    <div className="auth-layout">
      <section className="auth-layout__intro">
        <Badge tone="neutral">CampusSphere Access</Badge>
        <h1>Secure entry points for every role in the platform.</h1>
        <p>
          CampusSphere keeps sign-in, registration, password recovery, and profile setup in one consistent experience so the
          authentication layer feels like part of the product rather than a detached form set.
        </p>
        <div className="auth-layout__highlights">
          {highlights.map(item => (
            <Card key={item.title} className="auth-layout__highlight">
              <strong>{item.title}</strong>
              <span>{item.detail}</span>
            </Card>
          ))}
        </div>
        <div className="auth-layout__panel-card">
          <StatusIndicator tone="success" label="Backend-ready architecture" />
          <ul className="feature-list">
            {accessNotes.map(item => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </div>
      </section>

      <div className="auth-layout__panel">
        <Outlet />
      </div>
    </div>
  );
}
