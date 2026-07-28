import Avatar from '../components/Avatar';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import Footer from '../components/Footer';
import Navbar from '../components/Navbar';
import ProfileCard from '../components/ProfileCard';
import RoleBadge from '../components/RoleBadge';
import Sidebar from '../components/Sidebar';
import StatusIndicator from '../components/StatusIndicator';
import { DASHBOARD_NAV_ITEMS } from '../constants/navigation';
import { APP_ROUTES } from '../constants/routes';

const demoUser = {
  firstName: 'Aarav',
  lastName: 'Kumar',
  fullName: 'Aarav Kumar',
  email: 'aarav.kumar@campusphere.edu',
  department: 'Computer Science',
  academicYear: 'III',
  section: 'A',
  phoneNumber: '9876543210',
  profilePictureUrl: '',
  roles: [{ code: 'FACULTY_COORDINATOR' }]
};

const summaryCards = [
  { label: 'Session status', value: 'Preview', detail: 'Role-aware shell and navigation are visible without backend data.' },
  { label: 'Profile state', value: 'Loaded', detail: 'Reusable profile and status cards fit into the dashboard frame.' },
  { label: 'Workflow state', value: 'Empty', detail: 'Honest empty states keep the layout useful before event data exists.' }
];

export default function DashboardPreviewPage() {
  return (
    <div className="dashboard-shell">
      <Sidebar brand="CampusSphere" items={DASHBOARD_NAV_ITEMS} collapsed={false} user={demoUser} onLogout={() => {}} />
      <div className="dashboard-shell__main">
        <Navbar
          variant="dashboard"
          showMenuButton={false}
          user={demoUser}
          onLogout={() => {}}
          pageTitle="Dashboard"
          pageBreadcrumb="CampusSphere / Dashboard"
        />
        <main className="dashboard-shell__content">
          <div className="dashboard-page">
            <section className="dashboard-page__hero">
              <div>
                <Badge tone="neutral">Dashboard preview</Badge>
                <h1>Faculty coordinator workspace</h1>
                <p>
                  This preview route shows how the shell reads when a logged-in user reaches the dashboard. It uses the same
                  reusable layout, cards, and states as the protected flow.
                </p>
              </div>
              <div className="dashboard-page__hero-actions">
                <StatusIndicator tone="success" label="Preview mode" />
                <RoleBadge role="FACULTY_COORDINATOR" />
                <Button as="a" href={APP_ROUTES.home} variant="secondary" size="sm">
                  Back to public site
                </Button>
              </div>
            </section>

            <div className="dashboard-page__grid">
              <Card elevated className="dashboard-page__profile">
                <div className="dashboard-page__section-header">
                  <div>
                    <Badge tone="neutral">Current profile</Badge>
                    <h2>Aarav Kumar</h2>
                    <p className="dashboard-page__muted">aarav.kumar@campusphere.edu</p>
                  </div>
                  <Avatar name="Aarav Kumar" />
                </div>
                <ProfileCard user={demoUser} />
                <div className="dashboard-page__profile-actions">
                  <Button as="a" href={`${APP_ROUTES.home}#features`} variant="secondary" size="sm">
                    Review platform features
                  </Button>
                  <Button as="a" href={APP_ROUTES.profileSetup} size="sm">
                    Open profile setup
                  </Button>
                </div>
              </Card>

              <div className="dashboard-page__stack">
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

                <Card>
                  <div className="section-panel__title">
                    <strong>Active role</strong>
                    <Badge tone="success">Faculty Coordinator</Badge>
                  </div>
                  <p className="dashboard-page__muted">
                    The shell keeps room for approvals, attendance review, and report-oriented workflows without overfilling the first release.
                  </p>
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
                  description="This area will hold event lists, approval queues, and schedule data once those modules are connected."
                />
              </Card>

              <Card elevated className="section-panel">
                <div className="section-panel__title">
                  <strong>Recent activity</strong>
                  <Badge tone="neutral">Preview</Badge>
                </div>
                <EmptyState
                  title="No activity is loaded."
                  description="Use the shell as a stable place for recent changes, notifications, and dashboard-level summaries."
                />
              </Card>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    </div>
  );
}
