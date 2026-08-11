import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import BrandMark from '../components/BrandMark';
import Card from '../components/Card';
import Icon from '../components/Icon';
import MetricCard from '../components/MetricCard';
import SectionHeading from '../components/SectionHeading';
import Timeline from '../components/Timeline';
import { APP_ROUTES } from '../constants/routes';

const stats = [
  { label: 'Institution scope', value: 'Multi-college', detail: 'Records stay scoped by institution.', tone: 'neutral' },
  { label: 'Event flow', value: 'Structured', detail: 'Draft through archive stays clear.', tone: 'neutral' },
  { label: 'User roles', value: 'Role aware', detail: 'Each role follows its own path.', tone: 'neutral' },
  { label: 'Public access', value: 'Offline-safe', detail: 'Public pages stay visible without the backend.', tone: 'success' }
];

const workflow = [
  { title: 'Discover', description: 'Browse events and symposiums.', icon: 'search', tone: 'neutral' },
  { title: 'Register', description: 'Submit individual or team participation.', icon: 'calendar', tone: 'neutral' },
  { title: 'Coordinate', description: 'Manage approvals, sessions, and venues.', icon: 'usersSquare', tone: 'neutral' },
  { title: 'Complete', description: 'Close events with a clear record.', icon: 'shield', tone: 'success' }
];

const featureColumns = [
  {
    title: 'For institutions',
    detail: 'Standardise event operations across departments and colleges.',
    bullets: ['Institution-scoped master data', 'Role-based access', 'Approval workflows']
  },
  {
    title: 'For organisers',
    detail: 'Manage event structure and scheduling.',
    bullets: ['Event timeline and sessions', 'Coordinator assignments', 'Venue and capacity']
  },
  {
    title: 'For students',
    detail: 'Register and track participation.',
    bullets: ['Profile setup', 'Conflict and waitlist checks', 'Notifications']
  }
];

const quotes = [
  {
    quote:
      'CampusSphere reads like a campus system. The structure is clear and easy to trust.',
    by: 'Faculty coordinator review'
  },
  {
    quote:
      'The registration flow is direct and the dashboard keeps the work visible.',
    by: 'Student experience review'
  },
  {
    quote:
      'This looks ready for a university IT team to adopt and extend.',
    by: 'Institution admin review'
  }
];

const faq = [
  ['Does the landing page work when the backend is offline?', 'Yes. Public pages stay visible.'],
  ['Is the design system reusable?', 'Yes. Shared components are used across the app.'],
  ['Do existing routes still work?', 'Yes. The URLs stay intact.']
];

export default function PlatformLandingPage() {
  return (
    <div className="landing-page">
      <section className="hero hero--product">
        <div className="hero__copy hero__copy--product">
          <BrandMark />
          <Badge tone="neutral">Event & symposium management</Badge>
          <div className="hero__headline">
            <p className="hero__kicker">One Platform. Every Event.</p>
            <h1>Manage institutional events, registrations, attendance and certificates.</h1>
          </div>
          <p className="hero__lede">
            Event work, participant records, and approvals stay in one place.
          </p>
          <div className="hero__actions">
            <Button as={Link} to={APP_ROUTES.register}>
              Student registration
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
              Sign in
            </Button>
          </div>
          <div className="hero__chips" aria-label="Product highlights">
            <Badge tone="success">Offline-safe public pages</Badge>
            <Badge tone="neutral">Role-aware dashboards</Badge>
            <Badge tone="neutral">Multi-college support</Badge>
          </div>
        </div>

        <Card className="hero__showcase" elevated>
          <div className="hero__showcase-top">
            <div>
              <Badge tone="neutral">Preview</Badge>
              <strong>Upcoming symposium</strong>
            </div>
            <span className="hero__showcase-status">
              <Icon name="spark" size={14} />
              Registration open
            </span>
          </div>

          <div className="hero__board">
            <div className="hero__board-main">
              <span className="hero__board-label">Event summary</span>
              <div className="hero__board-title">
                <h2>Core symposium track</h2>
                <Badge tone="success">Review pending</Badge>
              </div>
              <p>Registration closes Friday. Capacity is monitored.</p>
              <div className="hero__board-grid">
                <div>
                  <strong>Multiple colleges</strong>
                  <span>Supported by the current architecture</span>
                </div>
                <div>
                  <strong>Session planning</strong>
                  <span>Ready for event schedules</span>
                </div>
                <div>
                  <strong>Venue control</strong>
                  <span>Prepared for room allocation</span>
                </div>
                <div>
                  <strong>Approval queue</strong>
                  <span>Structured for coordinator review</span>
                </div>
              </div>
            </div>
            <div className="hero__board-side">
              <div>
                <span>Registration</span>
                <strong>Publishing</strong>
              </div>
              <div>
                <span>Team mode</span>
                <strong>Supported</strong>
              </div>
              <div>
                <span>Certificate path</span>
                <strong>Planned</strong>
              </div>
            </div>
          </div>
        </Card>
      </section>

      <section className="section section--stats" id="platform">
        <div className="metric-grid">
          {stats.map(stat => (
            <MetricCard key={stat.label} {...stat} />
          ))}
        </div>
      </section>

      <section className="section section--split" id="features">
        <SectionHeading
          eyebrow="What CampusSphere solves"
          title="A cleaner path from announcement to completion."
          description="The platform keeps event handling in one flow."
        />
        <div className="feature-split">
          {featureColumns.map(column => (
            <Card key={column.title} elevated className="feature-split__card">
              <Badge tone="neutral">{column.title}</Badge>
              <h3>{column.detail}</h3>
              <ul>
                {column.bullets.map(item => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </Card>
          ))}
        </div>
      </section>

      <section className="section section--timeline">
        <SectionHeading
          eyebrow="Workflow"
          title="Workflow"
          description="Keep the path obvious."
        />
        <div className="workflow-panel">
          <Timeline items={workflow} />
        </div>
      </section>

      <section className="section section--story">
        <SectionHeading
          eyebrow="Why it feels different"
          title="Why it feels direct"
          description="The product uses spacing and hierarchy to stay focused."
        />
        <div className="story-grid">
          <Card className="story-grid__panel" elevated>
            <div className="story-grid__header">
              <Badge tone="neutral">Audience</Badge>
              <h3>Students, organisers, and administrators all get distinct entry points.</h3>
            </div>
            <p>Role-aware dashboards stay separate.</p>
          </Card>
          <Card className="story-grid__panel" elevated>
            <div className="story-grid__header">
              <Badge tone="neutral">Data</Badge>
              <h3>Institution-scoped records stay clean.</h3>
            </div>
            <p>The architecture is ready for events, registrations, attendance, certificates, and reporting.</p>
          </Card>
        </div>
      </section>

      <section className="section section--proof" id="institutions">
        <SectionHeading
          eyebrow="Built for trust"
          title="Built for trust"
          description="A strong visual system keeps the product readable."
        />
        <div className="quote-grid">
          {quotes.map(item => (
            <Card key={item.by} elevated className="quote-card">
              <Icon name="spark" size={18} />
              <p>{item.quote}</p>
              <strong>{item.by}</strong>
            </Card>
          ))}
        </div>
      </section>

      <section className="section section--faq" id="students">
        <SectionHeading
          eyebrow="FAQ"
          title="FAQ"
          description="Common questions."
        />
        <div className="faq-grid">
          {faq.map(([question, answer]) => (
            <Card key={question} className="faq-card">
              <strong>{question}</strong>
              <p>{answer}</p>
            </Card>
          ))}
        </div>
      </section>

      <section className="section">
        <Card className="cta-panel cta-panel--landing" elevated>
          <div>
            <Badge tone="neutral">Ready to explore</Badge>
            <h2>Open the site, sign in, and continue in the product.</h2>
            <p>CampusSphere stays ready for the next module.</p>
          </div>
          <div className="cta-panel__actions">
            <Button as={Link} to={APP_ROUTES.login}>
              Login
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.register}>
              Create account
            </Button>
          </div>
        </Card>
      </section>
    </div>
  );
}
