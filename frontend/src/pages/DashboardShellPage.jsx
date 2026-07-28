import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import ProfileCard from '../components/ProfileCard';
import RoleBadge from '../components/RoleBadge';
import StatusIndicator from '../components/StatusIndicator';
import { APP_ROUTES } from '../constants/routes';
import { useAuth } from '../context/AuthContext';
import { buildDisplayName, getPrimaryRole, getRoleDescription, getRoleLabel } from '../utils/auth';
import { getApiErrorMessage } from '../utils/apiErrors';

const summaryCards = [
  { label: 'Session status', value: 'Protected', tone: 'success', detail: 'JWT-backed access is active for this workspace.' },
  { label: 'Role routing', value: 'Ready', tone: 'neutral', detail: 'The shell is prepared for admin, faculty, organiser, and student views.' },
  { label: 'Backend state', value: 'API-first', tone: 'neutral', detail: 'Data panels fall back to honest empty states when the server is offline.' }
];

const activityNotes = [
  'No upcoming registrations are loaded yet.',
  'Event catalogue data will appear here after the backend modules are connected.',
  'Recent activity is intentionally empty until real user actions are available.'
];

export default function DashboardShellPage() {
  const { user: sessionUser, refreshCurrentUser } = useAuth();
  const [profile, setProfile] = useState(sessionUser);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let mounted = true;
    const initialUser = sessionUser;

    async function loadProfile() {
      setLoading(true);
      setErrorMessage('');
      try {
        const freshProfile = await refreshCurrentUser();
        if (mounted) {
          setProfile(freshProfile ?? initialUser);
        }
      } catch (error) {
        if (mounted) {
          setProfile(initialUser);
          setErrorMessage(getApiErrorMessage(error, 'Unable to refresh the profile from the backend.'));
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadProfile();

    return () => {
      mounted = false;
    };
  }, [refreshCurrentUser, sessionUser]);

  const currentProfile = profile ?? sessionUser;
  const roleCode = getPrimaryRole(currentProfile);
  const roleLabel = getRoleLabel(roleCode);
  const completion = useMemo(() => calculateCompletion(currentProfile), [currentProfile]);
  const isPrivileged = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR', 'ORGANISER', 'FACULTY_COORDINATOR'].includes(roleCode);

  return (
    <div className="dashboard-page">
      <section className="dashboard-page__hero">
        <div>
          <Badge tone="neutral">Dashboard</Badge>
          <h1>{roleLabel} workspace</h1>
          <p>{getRoleDescription(roleCode)}. This shell stays honest about the backend state while still feeling like a real product.</p>
        </div>
        <div className="dashboard-page__hero-actions">
          <StatusIndicator tone={errorMessage ? 'warning' : 'success'} label={errorMessage ? 'Backend unavailable' : 'Protected session active'} />
          <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.home}>
            Open public site
          </Button>
          <RoleBadge role={roleCode} />
        </div>
      </section>

      {errorMessage && <ErrorState title="Backend unavailable" description={errorMessage} onRetry={() => window.location.reload()} />}

      <div className="dashboard-page__grid">
        <Card elevated className="dashboard-page__profile">
          <div className="dashboard-page__section-header">
            <div>
              <Badge tone="neutral">Current profile</Badge>
              <h2>{buildDisplayName(currentProfile)}</h2>
              <p className="dashboard-page__muted">{currentProfile?.email ?? 'Session restored locally'}</p>
            </div>
            <RoleBadge role={roleCode} />
          </div>

          {loading ? <LoadingSkeleton lines={5} /> : <ProfileCard user={currentProfile} />}

          <div className="dashboard-page__profile-actions">
            <Button as={Link} to={APP_ROUTES.profileSetup} variant="secondary" size="sm">
              Complete profile
            </Button>
            <Button as={Link} to={`${APP_ROUTES.home}#security`} size="sm">
              Review security model
            </Button>
          </div>
        </Card>

        <div className="dashboard-page__stack">
          <Card>
            <div className="dashboard-page__section-header">
              <div>
                <Badge tone="neutral">Profile completion</Badge>
                <h2>{completion}%</h2>
              </div>
            </div>
            <p className="dashboard-page__muted">
              The shell reads the current user endpoint when it is available and falls back to cached session data when the backend is offline.
            </p>
          </Card>

          <Card>
            <div className="dashboard-page__section-header">
              <div>
                <Badge tone="neutral">Workspace summary</Badge>
                <h2>Ready state</h2>
              </div>
            </div>
            <div className="dashboard-page__stats">
              {summaryCards.map(stat => (
                <div key={stat.label} className="dashboard-page__stat">
                  <strong>{stat.value}</strong>
                  <span>{stat.label}</span>
                  <p className="dashboard-page__muted">{stat.detail}</p>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>

      <div className="card-grid card-grid--two">
        <Card elevated className="section-panel">
          <div className="section-panel__title">
            <strong>Upcoming events</strong>
            <Badge tone="neutral">Empty state</Badge>
          </div>
          <EmptyState
            title="No upcoming registrations yet."
            description="Explore events once the event catalogue is available and participant workflows are connected."
          />
        </Card>

        <Card elevated className="section-panel">
          <div className="section-panel__title">
            <strong>Recent activity</strong>
            <Badge tone="neutral">Honest placeholder</Badge>
          </div>
          <div className="dashboard-page__quick-actions">
            {activityNotes.map(note => (
              <div key={note} className="workflow-step">
                <span className="workflow-step__index">•</span>
                <div className="workflow-step__copy">
                  <strong>{note}</strong>
                  <span>Shown until live backend events are available.</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      <div className="card-grid card-grid--two">
        <Card className="section-panel">
          <div className="section-panel__title">
            <strong>Quick actions</strong>
            <Badge tone="neutral">Navigation</Badge>
          </div>
          <div className="dashboard-page__links">
            <Button as={Link} variant="secondary" to={APP_ROUTES.profileSetup}>
              Complete profile
            </Button>
            <Button as={Link} variant="secondary" to={`${APP_ROUTES.home}#features`}>
              Review platform features
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
              Switch account
            </Button>
          </div>
        </Card>

        <Card className="section-panel">
          <div className="section-panel__title">
            <strong>Account health</strong>
            <Badge tone={isPrivileged ? 'success' : 'neutral'}>Role aware</Badge>
          </div>
          <ul className="dashboard-page__list">
            <li>Login and logout flows are ready for live backend integration.</li>
            <li>Password recovery, refresh token, and protected route logic stay in place.</li>
            <li>Role-specific dashboard areas can expand without redesigning the shell.</li>
          </ul>
        </Card>
      </div>
    </div>
  );
}

function calculateCompletion(profile) {
  if (!profile) {
    return 0;
  }

  const fields = ['firstName', 'lastName', 'email', 'department', 'academicYear', 'section', 'phoneNumber', 'profilePictureUrl'];
  const filled = fields.filter(field => String(profile[field] ?? '').trim().length > 0).length;
  return Math.round((filled / fields.length) * 100);
}
