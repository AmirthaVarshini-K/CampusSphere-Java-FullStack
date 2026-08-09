import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import MetricCard from '../components/MetricCard';
import ProfileCard from '../components/ProfileCard';
import RoleBadge from '../components/RoleBadge';
import SectionHeading from '../components/SectionHeading';
import Timeline from '../components/Timeline';
import { APP_ROUTES } from '../constants/routes';
import { useAuth } from '../context/AuthContext';
import { analyticsApi } from '../services/analyticsApi';
import { buildDisplayName, getPrimaryRole, getRoleDescription, getRoleLabel } from '../utils/auth';
import { getApiErrorMessage } from '../utils/apiErrors';

const overviewMetrics = [
  { label: 'Workspace', value: 'Protected', detail: 'JWT and role checks stay in place across the shell.', tone: 'neutral' },
  { label: 'Session', value: 'Active', detail: 'The current profile is restored into the dashboard.', tone: 'success' },
  { label: 'Role scope', value: 'Aware', detail: 'Navigation and actions adapt to the signed-in role.', tone: 'neutral' },
  { label: 'Data feed', value: 'Honest', detail: 'Empty states remain visible when the backend has no live data.', tone: 'warning' }
];

const timelineItems = [
  { title: 'Morning overview', description: 'Review active events, queues, and unresolved tasks in one scan.', icon: 'clock', tone: 'neutral', meta: 'Today' },
  { title: 'Coordinator review', description: 'Pending approvals and publication steps sit in the same workspace.', icon: 'usersSquare', tone: 'neutral', meta: 'Pending' },
  { title: 'Workspace status', description: 'The shell remains useful even when the backend returns no live data.', icon: 'shield', tone: 'success', meta: 'Ready' }
];

const quickActions = [
  ['Open events', `${APP_ROUTES.dashboard}/events`],
  ['Review registrations', `${APP_ROUTES.dashboard}/registrations`],
  ['Open notifications', `${APP_ROUTES.dashboard}/notifications`],
  ['Complete profile', APP_ROUTES.profileSetup]
];

export default function DashboardShellPage() {
  const { user: sessionUser, refreshCurrentUser } = useAuth();
  const [profile, setProfile] = useState(sessionUser);
  const [refreshing, setRefreshing] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [analyticsSnapshot, setAnalyticsSnapshot] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(true);
  const [analyticsError, setAnalyticsError] = useState('');
  const currentProfile = profile ?? sessionUser;
  const roleCode = getPrimaryRole(currentProfile);
  const roleLabel = getRoleLabel(roleCode);
  const completion = useMemo(() => calculateCompletion(currentProfile), [currentProfile]);
  const roleDescription = getRoleDescription(roleCode);
  const displayProfile = currentProfile ?? {
    fullName: 'Current session',
    email: 'Profile is loading',
    roles: [{ code: roleCode }],
    status: 'Loading',
    profilePictureUrl: null
  };

  useEffect(() => {
    let mounted = true;

    async function loadProfile() {
      setErrorMessage('');
      try {
        const freshProfile = await refreshCurrentUser();
        if (mounted) {
          setProfile(freshProfile ?? sessionUser);
        }
      } catch (error) {
        if (mounted) {
          setProfile(sessionUser);
          setErrorMessage(getApiErrorMessage(error, 'Unable to refresh the current profile right now.'));
        }
      } finally {
        if (mounted) {
          setRefreshing(false);
        }
      }
    }

    loadProfile();

    return () => {
      mounted = false;
    };
  }, [refreshCurrentUser, sessionUser]);

  useEffect(() => {
    let mounted = true;

    async function loadAnalytics() {
      if (!currentProfile) {
        if (mounted) {
          setAnalyticsLoading(false);
        }
        return;
      }

      setAnalyticsLoading(true);
      setAnalyticsError('');
      try {
        const response = await analyticsApi.getOverview();
        if (mounted) {
          setAnalyticsSnapshot(response?.data?.data ?? null);
        }
      } catch (error) {
        if (mounted) {
          setAnalyticsSnapshot(null);
          setAnalyticsError(getApiErrorMessage(error, 'Unable to load live analytics right now.'));
        }
      } finally {
        if (mounted) {
          setAnalyticsLoading(false);
        }
      }
    }

    loadAnalytics();

    return () => {
      mounted = false;
    };
  }, [currentProfile, roleCode]);

  return (
    <div className="dashboard-home">
      <section className="dashboard-home__hero">
        <div className="dashboard-home__hero-copy">
          <Badge tone="neutral">Dashboard</Badge>
          <h1>{roleLabel} workspace</h1>
          <p>{roleDescription}. This is the control centre for your current session, with honest empty states and clear actions.</p>
          <div className="dashboard-home__hero-actions">
            <Button as={Link} to={`${APP_ROUTES.dashboard}/events`}>
              Open events
            </Button>
            <Button as={Link} variant="secondary" to={`${APP_ROUTES.dashboard}/notifications`}>
              View updates
            </Button>
          </div>
        </div>

        <Card elevated className="dashboard-home__hero-panel">
          <div className="dashboard-home__hero-panel-head">
            <div>
              <Badge tone="neutral">Workspace health</Badge>
              <strong>Protected session active</strong>
            </div>
            {refreshing && <Badge tone="neutral">Refreshing profile</Badge>}
            <RoleBadge role={roleCode} />
          </div>
          <div className="dashboard-home__identity">
            <ProfileCard user={displayProfile} />
          </div>
        </Card>
      </section>

      {errorMessage && <ErrorState title="Backend unavailable" description={errorMessage} onRetry={() => window.location.reload()} />}

      <section className="dashboard-home__metrics">
        {overviewMetrics.map(metric => (
          <MetricCard key={metric.label} {...metric} />
        ))}
      </section>

      <section className="dashboard-home__analytics">
        <SectionHeading
          eyebrow="Live analytics"
          title="Real database activity at a glance."
          description="These cards are populated from the current role scope and remain honest when the backend has little or no activity."
          action={analyticsError ? <Badge tone="warning">Using fallback summary</Badge> : analyticsLoading ? <Badge tone="neutral">Loading live data</Badge> : <Badge tone="success">{analyticsSnapshot?.scopeLabel ?? 'Current scope'}</Badge>}
        />
        <div className="dashboard-home__analytics-grid">
          {(analyticsSnapshot?.metrics?.length ? analyticsSnapshot.metrics.slice(0, 4) : [
            { label: 'Events', value: '—', detail: 'Waiting for the database to return live scope data.', tone: 'neutral' },
            { label: 'Registrations', value: '—', detail: 'Waiting for the database to return live scope data.', tone: 'neutral' },
            { label: 'Attendance', value: '—', detail: 'Waiting for the database to return live scope data.', tone: 'neutral' },
            { label: 'Certificates', value: '—', detail: 'Waiting for the database to return live scope data.', tone: 'neutral' }
          ]).map(metric => (
            <MetricCard key={metric.key ?? metric.label} {...metric} />
          ))}
        </div>
      </section>

      <section className="dashboard-home__grid">
        <Card elevated className="dashboard-home__board dashboard-home__board--wide">
          <SectionHeading
            eyebrow="Today"
            title="A compact overview of the current workspace."
            description="Use this space for activity, upcoming work, and the status of the signed-in role."
          />
          <div className="dashboard-home__board-grid">
            <div className="dashboard-home__stack">
              <div className="dashboard-home__callout">
                <span>Profile completion</span>
                <strong>{completion}%</strong>
                <p>The profile reads from the current session and remains honest when the backend is offline.</p>
              </div>
              <div className="dashboard-home__callout dashboard-home__callout--soft">
                <span>Role scope</span>
                <strong>{roleLabel}</strong>
                <p>{roleDescription}</p>
              </div>
            </div>
            <Timeline items={timelineItems} />
          </div>
        </Card>

        <div className="dashboard-home__column">
          <Card className="dashboard-home__board">
            <SectionHeading eyebrow="Quick actions" title="Move straight to the right surface." />
            <div className="dashboard-home__action-list">
              {quickActions.map(([label, to]) => (
                <Button key={label} as={Link} variant="secondary" to={to}>
                  {label}
                </Button>
              ))}
            </div>
          </Card>

          <Card className="dashboard-home__board">
            <SectionHeading eyebrow="Upcoming" title="No live activity yet." description="The empty state stays useful until the backend has real items to show." />
            <EmptyState
              title="No upcoming registrations yet."
              description="As event data arrives, this area will surface live items rather than fake counts."
              actionLabel="Browse events"
              onAction={() => window.location.assign(`${APP_ROUTES.dashboard}/events`)}
            />
          </Card>
        </div>
      </section>

      <section className="dashboard-home__grid dashboard-home__grid--secondary">
        <Card className="dashboard-home__board">
          <SectionHeading eyebrow="Role state" title="What this workspace is ready for." />
          <ul className="dashboard-home__list">
            <li>Login, logout, and refresh flows are in place.</li>
            <li>Protected routes and institution-aware sections stay intact.</li>
            <li>Future event, registration, and master-data pages can slot into the shell cleanly.</li>
          </ul>
        </Card>

        <Card className="dashboard-home__board">
          <SectionHeading eyebrow="Session" title="Current profile" />
          <div className="dashboard-home__profile-copy">
            <strong>{buildDisplayName(currentProfile)}</strong>
            <span>{currentProfile?.email ?? 'Session restored locally'}</span>
          </div>
          <div className="dashboard-home__profile-actions">
            <Button as={Link} variant="secondary" to={APP_ROUTES.profileSetup} size="sm">
              Complete profile
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.home} size="sm">
              Open public site
            </Button>
          </div>
        </Card>
      </section>
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

