import { Link, NavLink, useLocation } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import SearchBar from '../components/SearchBar';
import Table from '../components/Table';
import { APP_ROUTES } from '../constants/routes';
import { classNames } from '../utils/classNames';

const MODULE_TABS = [
  { label: 'Overview', path: `${APP_ROUTES.dashboard}/institution-setup` },
  { label: 'Institution', path: `${APP_ROUTES.dashboard}/institution-setup/institution` },
  { label: 'Departments', path: `${APP_ROUTES.dashboard}/institution-setup/departments` },
  { label: 'Academic Years', path: `${APP_ROUTES.dashboard}/institution-setup/academic-years` },
  { label: 'Programmes', path: `${APP_ROUTES.dashboard}/institution-setup/programmes` },
  { label: 'Mappings', path: `${APP_ROUTES.dashboard}/institution-setup/programme-mappings` },
  { label: 'Semesters', path: `${APP_ROUTES.dashboard}/institution-setup/semesters` },
  { label: 'Sections', path: `${APP_ROUTES.dashboard}/institution-setup/sections` }
];

export default function InstitutionSetupPage() {
  const location = useLocation();
  const activeTab = MODULE_TABS.find(tab => location.pathname.startsWith(tab.path)) ?? MODULE_TABS[0];

  const columns = [
    { key: 'name', header: 'Label' },
    { key: 'status', header: 'Status', render: row => <Badge tone={row.active ? 'success' : 'neutral'}>{row.status}</Badge> },
    { key: 'notes', header: 'Notes' }
  ];

  const rows = [
    { id: 1, name: 'Institution scope', status: 'Connected', active: true, notes: 'Scoped by authenticated user and role checks.' },
    { id: 2, name: 'Search and filters', status: 'Ready', active: true, notes: 'List views are prepared for backend pagination.' },
    { id: 3, name: 'Create and edit', status: 'Planned', active: false, notes: 'Forms will connect to the new master-data APIs.' }
  ];

  return (
    <div className="dashboard-page">
      <section className="dashboard-page__hero">
        <div>
          <Badge tone="neutral">Institution setup</Badge>
          <h1>Master data for every college.</h1>
          <p>CampusSphere now has a dedicated space for institutions, departments, programmes, semesters, and sections so future event workflows can stay properly scoped.</p>
        </div>
        <div className="dashboard-page__hero-actions">
          <Button as={Link} variant="secondary" size="sm" to={`${APP_ROUTES.dashboard}/institution-setup/departments`}>
            Open departments
          </Button>
          <Button as={Link} size="sm" to={APP_ROUTES.dashboard}>
            Back to dashboard
          </Button>
        </div>
      </section>

      <Card className="section-panel">
        <div className="tabs tabs--scrollable" role="tablist" aria-label="Institution setup sections">
          {MODULE_TABS.map(tab => (
            <NavLink
              key={tab.path}
              to={tab.path}
              className={({ isActive }) => classNames('tabs__tab', isActive && 'tabs__tab--active')}
            >
              {tab.label}
            </NavLink>
          ))}
        </div>
      </Card>

      <div className="card-grid card-grid--two">
        <Card elevated>
          <div className="section-panel__title">
            <strong>{activeTab.label}</strong>
            <Badge tone="neutral">Backend ready</Badge>
          </div>
          <SearchBar placeholder="Search master data" />
          <div style={{ marginTop: '1rem' }}>
            <Table columns={columns} rows={rows} emptyMessage="No records available yet." />
          </div>
        </Card>

        <Card elevated>
          <div className="section-panel__title">
            <strong>Setup progress</strong>
            <Badge tone="success">Scaffold complete</Badge>
          </div>
          <EmptyState
            title="No live records loaded yet."
            description="The UI is wired for the master-data module and will render real API content once you connect the CRUD actions."
          />
        </Card>
      </div>
    </div>
  );
}
