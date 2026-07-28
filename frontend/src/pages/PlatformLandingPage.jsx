import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import StatusIndicator from '../components/StatusIndicator';
import { APP_ROUTES } from '../constants/routes';

const platformProofPoints = [
  {
    title: 'Colleges',
    description: 'One operational backbone for multiple institutions without redesigning the user journey.'
  },
  {
    title: 'Organisers',
    description: 'Clear approval steps, event publishing, and attendance tracking when the event catalogue arrives.'
  },
  {
    title: 'Faculty coordinators',
    description: 'Structured oversight for approvals, certificates, reports, and symposium governance.'
  },
  {
    title: 'Students',
    description: 'Simple discovery, registration, participation tracking, and certificate access in one place.'
  }
];

const featureGroups = [
  {
    title: 'Event discovery and registration',
    icon: '01',
    bullets: ['Browse symposiums by category, date, or host college.', 'Register once and keep the participant record clean.', 'Prepare the UI for duplicate checks and form validation.']
  },
  {
    title: 'Institution and organiser control',
    icon: '02',
    bullets: ['Review and approve events from a role-specific workspace.', 'Keep organiser and faculty responsibilities separated.', 'Reserve room for approval queues and publication states.']
  },
  {
    title: 'Participant management',
    icon: '03',
    bullets: ['Track teams, submissions, and event participation.', 'Keep profile completion visible before registration tasks.', 'Avoid duplicate records and unclear ownership.']
  },
  {
    title: 'Notifications and updates',
    icon: '04',
    bullets: ['Show schedule changes, registration updates, and status changes.', 'Keep communication in the product instead of scattered messages.', 'Prepare for email and in-app notices later.']
  },
  {
    title: 'Attendance and certificates',
    icon: '05',
    bullets: ['Make attendance capture and certificate issuance easier to follow.', 'Leave space for post-event results and document workflows.', 'Support clean status states for completion and verification.']
  },
  {
    title: 'Multi-college coordination',
    icon: '06',
    bullets: ['Keep the architecture tenancy-ready without overcomplicating the first release.', 'Separate public pages, auth, dashboards, and future college modules.', 'Scale data and permissions without redesigning the interface.']
  }
];

const audienceCards = [
  {
    title: 'Students',
    detail: 'Find opportunities, register quickly, track your participation, and come back to one account for updates and certificates.'
  },
  {
    title: 'Organisers',
    detail: 'Manage event intake, review approvals, monitor participation, and keep the event timeline clear without juggling disconnected tools.'
  },
  {
    title: 'Institutions',
    detail: 'Standardise symposium operations across departments and colleges with a structure that can grow into a multi-tenant platform.'
  }
];

const workflowPairs = [
  {
    heading: 'Participant flow',
    steps: [
      ['Discover', 'See upcoming events in one place.'],
      ['Register', 'Complete one clean submission.'],
      ['Participate', 'Join sessions and track status.'],
      ['Track', 'Review attendance and updates.'],
      ['Receive results', 'Collect certificates and outcomes.']
    ]
  },
  {
    heading: 'Organiser flow',
    steps: [
      ['Create', 'Prepare symposium details and visibility.'],
      ['Approve', 'Review requests and participant entries.'],
      ['Publish', 'Share updates to the right audience.'],
      ['Manage', 'Monitor attendance and support tasks.'],
      ['Complete', 'Close the event with outcomes and reports.']
    ]
  }
];

export default function PlatformLandingPage() {
  return (
    <div className="landing">
      <section className="hero">
        <div className="hero__copy">
          <div className="hero__brand">
            <span className="hero__mark" aria-hidden="true">
              C
            </span>
            <div>
              <Badge tone="neutral">CampusSphere</Badge>
              <p className="hero__tagline">One Platform. Every Event.</p>
            </div>
          </div>
          <div className="hero__eyebrow section__meta">Multi-college event and symposium management</div>
          <h1>Run college events with a cleaner process for registration, approvals, attendance, and certificates.</h1>
          <p>
            CampusSphere gives colleges a structured front door for symposiums and workshops, with authentication and dashboard
            foundations that are already ready for the backend.
          </p>
          <div className="hero__actions">
            <Button as={Link} to={APP_ROUTES.login}>
              Login
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.register}>
              Get started
            </Button>
          </div>
          <div className="hero__status">
            <StatusIndicator tone="success" label="Frontend remains usable while the backend is offline" />
          </div>
        </div>

        <Card className="hero__panel" elevated>
          <div className="hero__panel-header">
            <Badge tone="neutral">Preview snapshot</Badge>
            <span>Structured event workspace</span>
          </div>
          <div className="hero-preview">
            <div className="hero-preview__top">
              <div className="hero-preview__title">
                <strong>Upcoming symposium</strong>
                <Badge tone="success">Registration open</Badge>
              </div>
              <span className="hero-preview__meta">
                <span className="chip chip--active">4 colleges connected</span>
                <span className="chip chip--active">12 sessions scheduled</span>
                <span className="chip chip--active">Faculty review pending</span>
              </span>
            </div>

            <div className="hero-preview__list" aria-label="Dashboard preview">
              <div className="hero-preview__item">
                <div>
                  <strong>Core symposium track</strong>
                  <span>Registration window closes on Friday</span>
                </div>
                <Badge tone="neutral">Publishing</Badge>
              </div>
              <div className="hero-preview__item">
                <div>
                  <strong>Student registrations</strong>
                  <span>Profile completion is required before final submission</span>
                </div>
                <Badge tone="neutral">Profile ready</Badge>
              </div>
              <div className="hero-preview__item">
                <div>
                  <strong>Organiser dashboard</strong>
                  <span>Approvals, attendance, and certificates stay in one workflow</span>
                </div>
                <Badge tone="success">Configured</Badge>
              </div>
            </div>

            <div className="hero-preview__foot">
              <div className="hero-preview__foot-card">
                <strong>Participating institutions</strong>
                <span>Department and college-specific views</span>
              </div>
              <div className="hero-preview__foot-card">
                <strong>Completion signal</strong>
                <span>Clear state for each user role</span>
              </div>
            </div>
          </div>
        </Card>
      </section>

      <section className="section">
        <Card elevated className="highlight-strip">
          <div className="highlight-strip__copy">
            <Badge tone="neutral">Built for trust</Badge>
            <h2>CampusSphere connects colleges, organisers, faculty coordinators, students, and event administrators in one workflow.</h2>
            <p>
              The first release focuses on the product foundation: public pages, authentication, dashboard structure, and a
              future-ready route architecture that can absorb later college modules without looking stitched together.
            </p>
          </div>
          <div className="chip-grid" aria-label="Platform roles">
            {platformProofPoints.map(item => (
              <span key={item.title} className="chip chip--active">
                {item.title}
              </span>
            ))}
          </div>
        </Card>
      </section>

      <section className="section" id="features">
        <div className="section__heading">
          <Badge tone="neutral">Problem and solution</Badge>
          <h2>Replace scattered event handling with a single structured platform.</h2>
          <p>
            Colleges usually juggle notices, registrations, approvals, attendance, and certificate tracking across disconnected
            tools. CampusSphere keeps that process visible and organised.
          </p>
        </div>
        <div className="two-up">
          <Card className="section-panel">
            <div className="section-panel__title">
              <strong>Problems CampusSphere addresses</strong>
              <Badge tone="neutral">Current pain points</Badge>
            </div>
            <ul className="feature-list">
              <li>Scattered event announcements and unclear registration flows.</li>
              <li>Duplicate participant records and manual spreadsheet tracking.</li>
              <li>Approval workflows that are hard to follow and harder to audit.</li>
              <li>Certificate and attendance data spread across different places.</li>
            </ul>
          </Card>
          <Card className="section-panel">
            <div className="section-panel__title">
              <strong>How the platform responds</strong>
              <Badge tone="success">Product direction</Badge>
            </div>
            <ul className="feature-list">
              <li>A single authenticated entry point for every role.</li>
              <li>Structured dashboards with role-aware navigation.</li>
              <li>Reusable states for approvals, empty views, and backend fallback.</li>
              <li>A multi-college architecture ready for future growth.</li>
            </ul>
          </Card>
        </div>
      </section>

      <section className="section" id="institutions">
        <div className="section__heading section__heading--compact">
          <Badge tone="neutral">Feature groups</Badge>
          <h2>Meaningful product areas, not filler cards.</h2>
          <p>
            The interface is organised around the work colleges actually perform, so each visible section feels specific and
            useful.
          </p>
        </div>
        <div className="card-grid">
          {featureGroups.map(feature => (
            <Card key={feature.title} className="feature-card" elevated>
              <div className="feature-card__head">
                <span className="feature-card__icon" aria-hidden="true">
                  {feature.icon}
                </span>
                <div>
                  <h3>{feature.title}</h3>
                </div>
              </div>
              <ul className="feature-card__list">
                {feature.bullets.map(item => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </Card>
          ))}
        </div>
      </section>

      <section className="section" id="students">
        <div className="section__heading">
          <Badge tone="neutral">Audience view</Badge>
          <h2>Designed for students, organisers, and institutions.</h2>
          <p>Each audience sees the parts of CampusSphere that matter to their responsibilities.</p>
        </div>
        <div className="audience-grid">
          {audienceCards.map(card => (
            <Card key={card.title} className="audience-card">
              <div className="audience-card__head">
                <h3>{card.title}</h3>
                <Badge tone="neutral">Role specific</Badge>
              </div>
              <div className="audience-card__body">
                <p>{card.detail}</p>
              </div>
            </Card>
          ))}
        </div>
      </section>

      <section className="section">
        <div className="section__heading">
          <Badge tone="neutral">Workflow</Badge>
          <h2>Simple paths for participants and organisers.</h2>
          <p>Keep the journey clear from discovery through completion without forcing every role into the same process.</p>
        </div>
        <div className="workflow-grid">
          {workflowPairs.map(group => (
            <Card key={group.heading} className="workflow-card">
              <div className="section-panel__title">
                <strong>{group.heading}</strong>
                <Badge tone="neutral">Sequential flow</Badge>
              </div>
              <div className="workflow-card__steps">
                {group.steps.map(([step, detail], index) => (
                  <div key={step} className="workflow-step">
                    <span className="workflow-step__index">{index + 1}</span>
                    <div className="workflow-step__copy">
                      <strong>{step}</strong>
                      <span>{detail}</span>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </section>

      <section className="section">
        <Card className="cta-panel" elevated>
          <div>
            <Badge tone="neutral">Ready to test the foundation</Badge>
            <h2>Open the platform, inspect the auth flows, and continue building on a cleaner base.</h2>
            <p>
              CampusSphere is already usable for frontend and authentication validation while the backend modules continue to
              evolve.
            </p>
          </div>
          <div className="button-row">
            <Button as={Link} to={APP_ROUTES.login}>
              Login
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.register}>
              Register
            </Button>
          </div>
        </Card>
      </section>
    </div>
  );
}
