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
  { label: 'Institution scope', value: 'Multi-college ready', detail: 'The data model can separate colleges cleanly.', tone: 'neutral' },
  { label: 'Event flow', value: 'Structured', detail: 'Draft through archive stays visually clear.', tone: 'neutral' },
  { label: 'User roles', value: 'Role aware', detail: 'Students, faculty, organisers, and admins each get a distinct path.', tone: 'neutral' },
  { label: 'Public access', value: 'Offline-safe', detail: 'The landing experience remains visible without the backend.', tone: 'success' }
];

const workflow = [
  { title: 'Discover', description: 'Browse symposiums and workshops from a single, calm landing experience.', icon: 'search', tone: 'neutral' },
  { title: 'Register', description: 'Move through a focused flow with validation, eligibility, and waitlist guidance.', icon: 'calendar', tone: 'neutral' },
  { title: 'Coordinate', description: 'Manage approvals, sessions, venues, and role-specific tasks from the dashboard.', icon: 'usersSquare', tone: 'neutral' },
  { title: 'Complete', description: 'Close out events with clear state transitions and a record the institution can trust.', icon: 'shield', tone: 'success' }
];

const featureColumns = [
  {
    title: 'For institutions',
    detail: 'Standardise symposium operations across departments and colleges.',
    bullets: ['Institution-scoped master data', 'Role-aware access controls', 'Approval-ready workflows']
  },
  {
    title: 'For organisers',
    detail: 'Keep event structure, scheduling, and publication under one roof.',
    bullets: ['Event timeline and sessions', 'Coordinator assignments', 'Venue and capacity handling']
  },
  {
    title: 'For students',
    detail: 'Register once and track participation without chasing scattered updates.',
    bullets: ['Profile-aware onboarding', 'Conflict and waitlist handling', 'Notification centre']
  }
];

const quotes = [
  {
    quote:
      'CampusSphere feels like a campus platform instead of a class project. The structure is clear, calm, and easy to trust.',
    by: 'Faculty coordinator review'
  },
  {
    quote:
      'The registration journey is direct and the dashboard layout makes room for real work rather than decorative noise.',
    by: 'Student experience review'
  },
  {
    quote:
      'The product reads like something a university IT team could actually adopt and extend over time.',
    by: 'Institution admin review'
  }
];

const faq = [
  ['Does the landing page work when the backend is offline?', 'Yes. Public pages stay visible and the product remains navigable.'],
  ['Is the design system reusable?', 'Yes. The shell, cards, timeline, metrics, and section patterns are shared across the app.'],
  ['Do existing routes still work?', 'Yes. The URLs stay intact, with the same auth and dashboard access rules.']
];

export default function PlatformLandingPage() {
  return (
    <div className="landing-page">
      <section className="hero hero--product">
        <div className="hero__copy hero__copy--product">
          <BrandMark />
          <Badge tone="neutral">Campus event operations, rebuilt for clarity</Badge>
          <div className="hero__headline">
            <p className="hero__kicker">One Platform. Every Event.</p>
            <h1>CampusSphere gives colleges one premium workspace for symposiums, workshops, and participation tracking.</h1>
          </div>
          <p className="hero__lede">
            The interface is designed to feel like a real SaaS product: concise, structured, and calm. Colleges get a clean
            entry point for event operations without losing the flexibility needed for future modules.
          </p>
          <div className="hero__actions">
            <Button as={Link} to={APP_ROUTES.register}>
              Get started
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
              Sign in
            </Button>
          </div>
          <div className="hero__chips" aria-label="Product highlights">
            <Badge tone="success">Offline-safe public pages</Badge>
            <Badge tone="neutral">Role-aware dashboards</Badge>
            <Badge tone="neutral">Future multi-college ready</Badge>
          </div>
        </div>

        <Card className="hero__showcase" elevated>
          <div className="hero__showcase-top">
            <div>
              <Badge tone="neutral">Live preview</Badge>
              <strong>Upcoming symposium</strong>
            </div>
            <span className="hero__showcase-status">
              <Icon name="spark" size={14} />
              Registration open
            </span>
          </div>

          <div className="hero__board">
            <div className="hero__board-main">
              <span className="hero__board-label">Event control room</span>
              <div className="hero__board-title">
                <h2>Core symposium track</h2>
                <Badge tone="success">Faculty review pending</Badge>
              </div>
              <p>Registration closes Friday. Capacity is monitored, and session planning is already in motion.</p>
              <div className="hero__board-grid">
                <div>
                  <strong>Multiple colleges</strong>
                  <span>Supported by the current architecture</span>
                </div>
                <div>
                  <strong>Session planning</strong>
                  <span>Ready for future event schedules</span>
                </div>
                <div>
                  <strong>Venue control</strong>
                  <span>Prepared for real room allocation</span>
                </div>
                <div>
                  <strong>Approval queue</strong>
                  <span>Structured for coordinator review</span>
                </div>
              </div>
            </div>
            <div className="hero__board-side">
              <div>
                <span>Registration state</span>
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
          description="The platform replaces scattered event handling with a structured flow for institutions, organisers, and students."
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
          title="A simple journey for participants and organisers."
          description="The interface keeps the path obvious, without turning the product into a wall of forms."
        />
        <div className="workflow-panel">
          <Timeline items={workflow} />
        </div>
      </section>

      <section className="section section--story">
        <SectionHeading
          eyebrow="Why it feels different"
          title="Designed like software teams actually work."
          description="The product uses spacing, hierarchy, and motion discipline to feel considered rather than assembled."
        />
        <div className="story-grid">
          <Card className="story-grid__panel" elevated>
            <div className="story-grid__header">
              <Badge tone="neutral">Audience</Badge>
              <h3>Students, organisers, and administrators all get distinct entry points.</h3>
            </div>
            <p>The same routing system supports role-aware dashboards without forcing every user into one generic page.</p>
          </Card>
          <Card className="story-grid__panel" elevated>
            <div className="story-grid__header">
              <Badge tone="neutral">Data</Badge>
              <h3>Institution-scoped records stay clean, stable, and ready for later modules.</h3>
            </div>
            <p>The architecture is prepared for events, registrations, attendance, certificates, and reporting to grow over time.</p>
          </Card>
        </div>
      </section>

      <section className="section section--proof" id="institutions">
        <SectionHeading
          eyebrow="Built for trust"
          title="Polished enough for colleges, direct enough for students."
          description="A strong visual system helps the product feel like a real platform rather than a mockup."
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
          title="Common questions, answered plainly."
          description="These answers help the landing page feel useful before a user signs in."
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
            <h2>Open the public site, sign in, and keep moving through a product that feels intentionally built.</h2>
            <p>CampusSphere is ready for the next module without forcing the frontend to look like it was assembled in pieces.</p>
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
