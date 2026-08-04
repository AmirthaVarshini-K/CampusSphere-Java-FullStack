import { Link } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import BrandMark from '../components/BrandMark';
import Card from '../components/Card';
import MetricCard from '../components/MetricCard';
import SectionHeading from '../components/SectionHeading';
import Timeline from '../components/Timeline';
import { APP_ROUTES } from '../constants/routes';

const metrics = [
  { label: 'Preview mode', value: 'Active', detail: 'Shows how the authenticated workspace reads.', tone: 'neutral' },
  { label: 'Role', value: 'Faculty', detail: 'Demo state for the shell and navigation.', tone: 'neutral' },
  { label: 'State', value: 'Ready', detail: 'No backend data is required to view this route.', tone: 'success' }
];

const timeline = [
  { title: 'Role-aware shell', description: 'Sidebar, topbar, and page hierarchy use the same design language.', icon: 'grid', tone: 'neutral' },
  { title: 'Product-style layout', description: 'The route uses content blocks instead of a generic card grid.', icon: 'calendar', tone: 'neutral' },
  { title: 'Open access', description: 'Anyone can inspect the demo without signing in.', icon: 'shield', tone: 'success' }
];

export default function DashboardPreviewPage() {
  return (
    <div className="dashboard-preview-shell">
      <section className="dashboard-preview-shell__hero">
        <Card elevated>
          <BrandMark />
          <Badge tone="neutral">Dashboard preview</Badge>
          <h1>Faculty coordinator workspace</h1>
          <p>This route shows the authenticated shell without needing a live account. It mirrors the new product language.</p>
          <div className="dashboard-home__hero-actions">
            <Button as={Link} to={APP_ROUTES.home}>
              Back to public site
            </Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.login}>
              Open sign in
            </Button>
          </div>
        </Card>

        <Card elevated>
          <SectionHeading eyebrow="Preview state" title="What the shell is ready for" />
          <Timeline items={timeline} />
        </Card>
      </section>

      <section className="dashboard-home__metrics">
        {metrics.map(metric => (
          <MetricCard key={metric.label} {...metric} />
        ))}
      </section>
    </div>
  );
}
