import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import Icon from '../components/Icon';
import Input from '../components/Input';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Pagination from '../components/Pagination';
import SectionHeading from '../components/SectionHeading';
import SearchBar from '../components/SearchBar';
import Select from '../components/Select';
import Table from '../components/Table';
import Tabs from '../components/Tabs';
import Timeline from '../components/Timeline';
import { Toast } from '../components/Toast';
import { APP_ROUTES } from '../constants/routes';
import { useAuth } from '../context/AuthContext';
import useToastQueue from '../hooks/useToastQueue';
import { analyticsApi } from '../services/analyticsApi';
import { getApiErrorMessage } from '../utils/apiErrors';
import { getPrimaryRole, getRoleDescription, getRoleLabel } from '../utils/auth';
import { classNames } from '../utils/classNames';

const PAGE_SIZE = 10;

const EVENT_STATUS_OPTIONS = [
  ['', 'Any status'],
  ['DRAFT', 'Draft'],
  ['PENDING_APPROVAL', 'Pending approval'],
  ['PUBLISHED', 'Published'],
  ['REGISTRATION_OPEN', 'Registration open'],
  ['REGISTRATION_CLOSED', 'Registration closed'],
  ['ONGOING', 'Ongoing'],
  ['COMPLETED', 'Completed'],
  ['CANCELLED', 'Cancelled'],
  ['ARCHIVED', 'Archived']
];

const REGISTRATION_STATUS_OPTIONS = [
  ['', 'Any status'],
  ['PENDING', 'Pending'],
  ['APPROVED', 'Approved'],
  ['WAITLISTED', 'Waitlisted'],
  ['REJECTED', 'Rejected'],
  ['CANCELLED', 'Cancelled']
];

const ATTENDANCE_STATUS_OPTIONS = [
  ['', 'Any status'],
  ['PRESENT', 'Present'],
  ['ABSENT', 'Absent'],
  ['LATE', 'Late'],
  ['EXCUSED', 'Excused']
];

const CERTIFICATE_STATUS_OPTIONS = [
  ['', 'Any status'],
  ['DRAFT', 'Draft'],
  ['ISSUED', 'Issued'],
  ['REVOKED', 'Revoked'],
  ['ARCHIVED', 'Archived']
];

const CERTIFICATE_TYPE_OPTIONS = [
  ['', 'Any type'],
  ['PARTICIPATION', 'Participation'],
  ['WINNER', 'Winner'],
  ['ORGANIZER', 'Organizer'],
  ['VOLUNTEER', 'Volunteer'],
  ['JUDGE', 'Judge'],
  ['FACULTY_COORDINATOR', 'Faculty coordinator']
];

const EVENT_REPORT_COLUMNS = [
  { key: 'eventTitle', header: 'Event' },
  { key: 'status', header: 'Status', render: row => <Badge tone={eventTone(row.status)}>{prettyLabel(row.status)}</Badge> },
  { key: 'mode', header: 'Mode', render: row => prettyLabel(row.mode) },
  { key: 'registrations', header: 'Registrations' },
  { key: 'attendanceCount', header: 'Attendance' },
  { key: 'capacityUtilization', header: 'Capacity', render: row => `${row.capacityUtilization ?? 0}%` }
];

const REGISTRATION_REPORT_COLUMNS = [
  { key: 'participantName', header: 'Participant' },
  { key: 'eventTitle', header: 'Event' },
  { key: 'status', header: 'Status', render: row => <Badge tone={registrationTone(row.status)}>{prettyLabel(row.status)}</Badge> },
  { key: 'registrationDate', header: 'Registered', render: row => formatDateTime(row.registrationDate) },
  { key: 'waitlistPosition', header: 'Waitlist', render: row => row.waitlistPosition ?? '—' }
];

const ATTENDANCE_REPORT_COLUMNS = [
  { key: 'participantName', header: 'Participant' },
  { key: 'eventTitle', header: 'Event' },
  { key: 'attendanceStatus', header: 'Status', render: row => <Badge tone={attendanceTone(row.attendanceStatus)}>{prettyLabel(row.attendanceStatus)}</Badge> },
  { key: 'attendanceMethod', header: 'Method', render: row => prettyLabel(row.attendanceMethod) },
  { key: 'checkInTime', header: 'Check-in', render: row => formatDateTime(row.checkInTime) }
];

const CERTIFICATE_REPORT_COLUMNS = [
  { key: 'recipientName', header: 'Recipient' },
  { key: 'eventTitle', header: 'Event' },
  { key: 'certificateType', header: 'Type', render: row => prettyLabel(row.certificateType) },
  { key: 'certificateStatus', header: 'Status', render: row => <Badge tone={certificateTone(row.certificateStatus, row.revoked)}>{row.revoked ? 'Revoked' : prettyLabel(row.certificateStatus)}</Badge> },
  { key: 'generatedAt', header: 'Generated', render: row => formatInstant(row.generatedAt) }
];

const DEPARTMENT_REPORT_COLUMNS = [
  { key: 'departmentName', header: 'Department' },
  { key: 'registrations', header: 'Registrations' },
  { key: 'attendanceRate', header: 'Attendance rate', render: row => `${row.attendanceRate ?? 0}%` },
  { key: 'certificatesIssued', header: 'Certificates' }
];

const TAB_SETS = {
  ADMINISTRATOR: [
    { key: 'overview', label: 'Overview' },
    { key: 'events', label: 'Events', reportType: 'EVENTS', title: 'Event performance', description: 'Compare participation, capacity, and event health.', columns: EVENT_REPORT_COLUMNS },
    { key: 'registrations', label: 'Registrations', reportType: 'REGISTRATIONS', title: 'Registration analysis', description: 'Review approvals, waitlists, and participation volume.', columns: REGISTRATION_REPORT_COLUMNS },
    { key: 'attendance', label: 'Attendance', reportType: 'ATTENDANCE', title: 'Attendance analysis', description: 'Measure check-ins and attendance status distribution.', columns: ATTENDANCE_REPORT_COLUMNS },
    { key: 'certificates', label: 'Certificates', reportType: 'CERTIFICATES', title: 'Certificate analysis', description: 'Track issued, verified, and revoked certificates.', columns: CERTIFICATE_REPORT_COLUMNS },
    { key: 'departments', label: 'Departments', reportType: 'DEPARTMENTS', title: 'Department insights', description: 'Institution participation grouped by department.', columns: DEPARTMENT_REPORT_COLUMNS }
  ],
  FACULTY_COORDINATOR: [
    { key: 'overview', label: 'Overview' },
    { key: 'events', label: 'Managed events', reportType: 'EVENTS', title: 'Managed events', description: 'Focus on the events assigned to your account.', columns: EVENT_REPORT_COLUMNS },
    { key: 'registrations', label: 'Registrations', reportType: 'REGISTRATIONS', title: 'Event registrations', description: 'Review registrations across your managed events.', columns: REGISTRATION_REPORT_COLUMNS },
    { key: 'attendance', label: 'Attendance', reportType: 'ATTENDANCE', title: 'Attendance', description: 'Monitor check-ins and live participation.', columns: ATTENDANCE_REPORT_COLUMNS },
    { key: 'certificates', label: 'Certificates', reportType: 'CERTIFICATES', title: 'Certificates', description: 'Review certificate readiness and issued records.', columns: CERTIFICATE_REPORT_COLUMNS }
  ],
  STUDENT: [{ key: 'overview', label: 'My activity' }]
};

function prettyLabel(value) {
  if (!value) {
    return '—';
  }
  return value
    .toString()
    .toLowerCase()
    .split('_')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function eventTone(status) {
  switch (status) {
    case 'PUBLISHED':
    case 'REGISTRATION_OPEN':
    case 'ONGOING':
      return 'success';
    case 'PENDING_APPROVAL':
    case 'REGISTRATION_CLOSED':
      return 'warning';
    case 'CANCELLED':
    case 'ARCHIVED':
      return 'neutral';
    default:
      return 'neutral';
  }
}

function registrationTone(status) {
  switch (status) {
    case 'APPROVED':
      return 'success';
    case 'WAITLISTED':
      return 'warning';
    case 'REJECTED':
    case 'CANCELLED':
      return 'danger';
    default:
      return 'neutral';
  }
}

function attendanceTone(status) {
  switch (status) {
    case 'PRESENT':
      return 'success';
    case 'LATE':
      return 'warning';
    case 'ABSENT':
      return 'danger';
    default:
      return 'neutral';
  }
}

function certificateTone(status, revoked) {
  if (revoked || status === 'REVOKED') {
    return 'danger';
  }
  if (status === 'ISSUED') {
    return 'success';
  }
  if (status === 'DRAFT') {
    return 'neutral';
  }
  return 'warning';
}

function unwrap(response) {
  return response?.data?.data ?? null;
}

function unwrapPage(response) {
  const data = unwrap(response);
  return {
    content: data?.content ?? [],
    page: data?.page ?? 0,
    size: data?.size ?? PAGE_SIZE,
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    first: Boolean(data?.first),
    last: Boolean(data?.last)
  };
}

function formatNumber(value) {
  return new Intl.NumberFormat('en-IN').format(Number(value ?? 0));
}

function formatPercent(value) {
  return `${Math.round(Number(value ?? 0))}%`;
}

function formatDateTime(value) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '-';
  }
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
}

function formatInstant(value) {
  return formatDateTime(value);
}

function formatDateInput(value) {
  return value ? String(value).slice(0, 10) : '';
}

function buildFilters(tabKey) {
  return {
    search: '',
    startDate: '',
    endDate: '',
    status: '',
    mode: '',
    attendanceStatus: '',
    certificateType: '',
    sort: tabKey === 'events' ? 'startDateTime' : tabKey === 'registrations' ? 'registrationDate' : tabKey === 'attendance' ? 'checkInTime' : tabKey === 'certificates' ? 'generatedAt' : 'name',
    direction: 'desc',
    page: 1,
    size: PAGE_SIZE
  };
}

function buildQuery(tabKey, filters) {
  const payload = {
    search: filters.search || undefined,
    startDate: filters.startDate || undefined,
    endDate: filters.endDate || undefined,
    status: filters.status || undefined,
    mode: filters.mode || undefined,
    attendanceStatus: filters.attendanceStatus || undefined,
    certificateType: filters.certificateType || undefined,
    sort: filters.sort || undefined,
    direction: filters.direction || undefined,
    page: Math.max(0, (filters.page ?? 1) - 1),
    size: filters.size ?? PAGE_SIZE
  };

  if (tabKey !== 'events') {
    delete payload.mode;
  }
  if (tabKey !== 'attendance') {
    delete payload.attendanceStatus;
  }
  if (tabKey !== 'certificates') {
    delete payload.certificateType;
  }

  return payload;
}

function getOverviewRequest(roleCode) {
  if (roleCode === 'STUDENT') {
    return analyticsApi.getMyInsights();
  }
  if (roleCode === 'FACULTY_COORDINATOR') {
    return analyticsApi.getCoordinatorInsights();
  }
  return analyticsApi.getOverview();
}

function getWorkspaceTabs(roleCode) {
  return TAB_SETS[roleCode] ?? TAB_SETS.ADMINISTRATOR;
}

function TrendChart({ points = [] }) {
  const width = 640;
  const height = 220;
  if (!points.length || points.every(point => Number(point.value ?? 0) === 0)) {
    return (
      <EmptyState
        title="Not enough activity yet"
        description="This chart becomes useful once the database contains more registrations, attendance records, or certificates."
      />
    );
  }

  const max = Math.max(...points.map(point => Number(point.value ?? 0)), 1);
  const step = points.length > 1 ? width / (points.length - 1) : width;
  const linePoints = points
    .map((point, index) => {
      const value = Number(point.value ?? 0);
      const x = index * step;
      const y = height - (value / max) * (height - 24) - 12;
      return `${x},${y}`;
    })
    .join(' ');

  return (
    <div className="analytics-chart analytics-chart--trend">
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Registration trend chart">
        <defs>
          <linearGradient id="analyticsTrendFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor="rgba(68, 85, 214, 0.28)" />
            <stop offset="100%" stopColor="rgba(68, 85, 214, 0)" />
          </linearGradient>
        </defs>
        <polyline points={linePoints} fill="none" stroke="var(--color-indigo)" strokeWidth="3" strokeLinejoin="round" strokeLinecap="round" />
        <polyline points={`0,${height - 12} ${linePoints} ${width},${height - 12}`} fill="url(#analyticsTrendFill)" stroke="none" />
        {points.map((point, index) => {
          const value = Number(point.value ?? 0);
          const x = index * step;
          const y = height - (value / max) * (height - 24) - 12;
          return (
            <g key={`${point.label}-${index}`}>
              <circle cx={x} cy={y} r="5" fill="var(--color-background)" stroke="var(--color-indigo)" strokeWidth="3">
                <title>
                  {point.label}: {formatNumber(value)}
                </title>
              </circle>
              <text x={x} y={height - 2} textAnchor="middle" className="analytics-chart__label">
                {point.label}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

function DistributionChart({ points = [] }) {
  if (!points.length || points.every(point => Number(point.value ?? 0) === 0)) {
    return (
      <EmptyState
        title="No distribution data yet"
        description="This view fills in automatically when the current scope has registrations, attendance, or certificate records."
      />
    );
  }

  const total = points.reduce((sum, point) => sum + Number(point.value ?? 0), 0);
  let offset = 0;
  const segments = points.map(point => {
    const size = (Number(point.value ?? 0) / total) * 100;
    const segment = `${point.tone === 'danger' ? '#ef4444' : point.tone === 'warning' ? '#f59e0b' : point.tone === 'success' ? '#10b981' : '#64748b'} ${offset}% ${offset + size}%`;
    offset += size;
    return segment;
  });

  return (
    <div className="analytics-chart analytics-chart--donut">
      <div
        className="analytics-chart__donut"
        style={{ background: `conic-gradient(${segments.join(', ')})` }}
        aria-label="Distribution chart"
        role="img"
      >
        <div>
          <strong>{formatNumber(total)}</strong>
          <span>Total</span>
        </div>
      </div>
      <div className="analytics-chart__legend">
        {points.map(point => (
          <div key={point.label} className="analytics-chart__legend-item">
            <span className={`analytics-chart__swatch analytics-chart__swatch--${point.tone ?? 'neutral'}`} />
            <div>
              <strong>{point.label}</strong>
              <span>{formatNumber(point.value)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function MetricStrip({ metrics = [] }) {
  return (
    <section className="analytics-metrics" aria-label="Summary metrics">
      {metrics.length ? (
        metrics.map(metric => (
          <article key={metric.key} className={classNames('metric-card', `metric-card--${metric.tone ?? 'neutral'}`)}>
            <span className="metric-card__label">{metric.label}</span>
            <strong className="metric-card__value">{typeof metric.value === 'number' ? formatNumber(metric.value) : metric.value}</strong>
            {metric.detail && <p className="metric-card__detail">{metric.detail}</p>}
          </article>
        ))
      ) : (
        <>
          <article className="metric-card"><span className="metric-card__label">Loading</span><strong className="metric-card__value">—</strong><p className="metric-card__detail">Analytics are being loaded from the database.</p></article>
          <article className="metric-card"><span className="metric-card__label">Loading</span><strong className="metric-card__value">—</strong><p className="metric-card__detail">Analytics are being loaded from the database.</p></article>
          <article className="metric-card"><span className="metric-card__label">Loading</span><strong className="metric-card__value">—</strong><p className="metric-card__detail">Analytics are being loaded from the database.</p></article>
          <article className="metric-card"><span className="metric-card__label">Loading</span><strong className="metric-card__value">—</strong><p className="metric-card__detail">Analytics are being loaded from the database.</p></article>
        </>
      )}
    </section>
  );
}

function ActivityList({ items = [] }) {
  if (!items.length) {
    return (
      <EmptyState
        title="No recent activity"
        description="The current scope does not have enough events, registrations, attendance, or certificate activity to show a meaningful timeline yet."
      />
    );
  }

  return <Timeline items={items.map(item => ({ ...item, icon: item.icon ?? 'pulse' }))} />;
}

export default function AnalyticsPage() {
  const { user } = useAuth();
  const { toasts, pushToast } = useToastQueue();
  const location = useLocation();
  const roleCode = getPrimaryRole(user);
  const roleLabel = getRoleLabel(roleCode);
  const roleDescription = getRoleDescription(roleCode);
  const tabs = useMemo(() => getWorkspaceTabs(roleCode), [roleCode]);
  const isStudent = roleCode === 'STUDENT';
  const defaultTab = useMemo(() => {
    if (location.pathname.startsWith(APP_ROUTES.reports) && !isStudent) {
      return tabs[1]?.key ?? 'overview';
    }
    return 'overview';
  }, [isStudent, location.pathname, tabs]);

  const [activeTab, setActiveTab] = useState(defaultTab);
  const [overview, setOverview] = useState(null);
  const [overviewLoading, setOverviewLoading] = useState(true);
  const [overviewError, setOverviewError] = useState('');
  const [report, setReport] = useState(null);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState('');
  const [draftFilters, setDraftFilters] = useState(() => buildFilters(defaultTab));
  const [activeFilters, setActiveFilters] = useState(() => buildFilters(defaultTab));

  useEffect(() => {
    setActiveTab(defaultTab);
    setDraftFilters(buildFilters(defaultTab));
    setActiveFilters(buildFilters(defaultTab));
  }, [defaultTab]);

  useEffect(() => {
    let active = true;

    async function loadOverview() {
      setOverviewLoading(true);
      setOverviewError('');
      try {
        const response = await getOverviewRequest(roleCode);
        if (!active) {
          return;
        }
        setOverview(unwrap(response));
      } catch (error) {
        if (active) {
          setOverviewError(getApiErrorMessage(error, 'Unable to load analytics right now.'));
        }
      } finally {
        if (active) {
          setOverviewLoading(false);
        }
      }
    }

    loadOverview();
    return () => {
      active = false;
    };
  }, [roleCode]);

  useEffect(() => {
    if (isStudent || activeTab === 'overview') {
      return;
    }

    let active = true;
    const tabConfig = tabs.find(item => item.key === activeTab);
    if (!tabConfig?.reportType) {
      setReport(null);
      return;
    }

    async function loadReport() {
      setReportLoading(true);
      setReportError('');
      try {
        const response = await fetchReport(tabConfig.reportType, buildQuery(activeTab, activeFilters));
        if (!active) {
          return;
        }
        setReport({
          ...unwrap(response),
          page: unwrapPage(response)
        });
      } catch (error) {
        if (active) {
          setReportError(getApiErrorMessage(error, 'Unable to load the selected report.'));
        }
      } finally {
        if (active) {
          setReportLoading(false);
        }
      }
    }

    loadReport();
    return () => {
      active = false;
    };
  }, [activeFilters, activeTab, isStudent, tabs]);

  const currentData = useMemo(() => {
    if (isStudent || activeTab === 'overview') {
      return overview;
    }
    return report;
  }, [activeTab, isStudent, overview, report]);

  const selectedTab = tabs.find(item => item.key === activeTab) ?? tabs[0];
  const canExport = Boolean(selectedTab?.reportType && currentData?.page);
  const title = isStudent ? 'My activity' : activeTab === 'overview' ? 'Overview' : selectedTab?.title ?? 'Reports';
  const description = isStudent ? 'Personal participation, attendance, and certificate insights.' : selectedTab?.description ?? 'Real, institution-scoped analytics from CampusSphere records.';

  function changeTab(tabKey) {
    setActiveTab(tabKey);
    if (tabKey === 'overview' || isStudent) {
      return;
    }
    const nextFilters = buildFilters(tabKey);
    setDraftFilters(nextFilters);
    setActiveFilters(nextFilters);
  }

  function updateDraft(name, value) {
    setDraftFilters(current => ({ ...current, [name]: value }));
  }

  function applyFilters() {
    setActiveFilters({ ...draftFilters, page: 1 });
  }

  function clearFilters() {
    const next = buildFilters(activeTab);
    setDraftFilters(next);
    setActiveFilters(next);
  }

  async function handleExport() {
    if (!selectedTab?.reportType) {
      return;
    }

    try {
      const response = await analyticsApi.exportReport(selectedTab.reportType, buildQuery(activeTab, activeFilters));
      const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `${selectedTab.reportType.toLowerCase()}-analytics.csv`;
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      window.URL.revokeObjectURL(url);
      pushToast('CSV export downloaded.');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to export the report right now.'), 'error');
    }
  }

  const topItems = currentData?.topItems ?? [];
  const insights = currentData?.insights ?? [];
  const activity = currentData?.activity ?? [];
  const metrics = currentData?.metrics ?? [];
  const trend = currentData?.trend ?? [];
  const distribution = currentData?.distribution ?? [];
  const emptyStateMessage = currentData?.emptyStateMessage;
  const page = report?.page ?? null;
  const reportRows = page?.content ?? [];
  const reportColumns = selectedTab?.columns ?? [];
  const currentReportTitle = selectedTab?.title ?? title;

  return (
    <div className="analytics-workspace page-stack">
      <section className="analytics-hero">
        <div className="analytics-hero__copy">
          <Badge tone="neutral">Analytics</Badge>
          <h1>{isStudent ? 'My activity dashboard' : roleLabel + ' analytics'}</h1>
          <p>{description} {roleDescription ? `${roleDescription}.` : ''}</p>
          <div className="analytics-hero__actions">
            <Button onClick={() => window.location.reload()}>
              <Icon name="refresh" size={16} />
              Refresh
            </Button>
            {!isStudent && selectedTab?.reportType && (
              <Button variant="secondary" onClick={handleExport} disabled={reportLoading}>
                <Icon name="download" size={16} />
                Export CSV
              </Button>
            )}
            <Button as={Link} variant="secondary" to={APP_ROUTES.dashboard}>
              Back to dashboard
            </Button>
          </div>
        </div>

        <Card elevated className="analytics-hero__panel">
          <div className="analytics-hero__panel-row">
            <div>
              <Badge tone="neutral">Scope</Badge>
              <strong>{currentData?.scopeLabel ?? user?.institution?.institutionName ?? 'Current session'}</strong>
            </div>
            <Badge tone={overviewLoading || reportLoading ? 'warning' : 'success'}>
              {overviewLoading || reportLoading ? 'Refreshing' : 'Live'}
            </Badge>
          </div>
          <div className="analytics-hero__panel-grid">
            <div>
              <span>Role</span>
              <strong>{currentData?.roleLabel ?? roleLabel}</strong>
            </div>
            <div>
              <span>Generated</span>
              <strong>{currentData?.generatedAt ? formatDateTime(currentData.generatedAt) : 'Just now'}</strong>
            </div>
          </div>
          <p>{emptyStateMessage ?? 'All metrics come from the current CampusSphere database scope.'}</p>
        </Card>
      </section>

      {overviewError && !overviewLoading && <ErrorState title="Analytics unavailable" description={overviewError} onRetry={() => window.location.reload()} />}

      <MetricStrip metrics={metrics} />

      <section className="analytics-grid">
        <Card elevated className="analytics-panel">
          <SectionHeading eyebrow="Trend" title="Activity over time" description="The chart uses the active filtered records." />
          {overviewLoading && !overview ? <LoadingSkeleton lines={4} /> : <TrendChart points={trend} />}
        </Card>

        <Card elevated className="analytics-panel">
          <SectionHeading eyebrow="Distribution" title="Status breakdown" description="A compact view of how the current scope is distributed." />
          {overviewLoading && !overview ? <LoadingSkeleton lines={4} /> : <DistributionChart points={distribution} />}
        </Card>
      </section>

      <section className="analytics-grid analytics-grid--secondary">
        <Card elevated className="analytics-panel">
          <SectionHeading eyebrow="Insights" title="What stands out" description="These notes are derived from the current records, not fabricated placeholders." />
          {insights.length ? (
            <div className="analytics-insights">
              {insights.map(insight => (
                <article key={insight.title} className="analytics-insight">
                  <Badge tone={insight.tone === 'warning' ? 'warning' : insight.tone === 'success' ? 'success' : insight.tone === 'danger' ? 'warning' : 'neutral'}>{prettyLabel(insight.tone)}</Badge>
                  <strong>{insight.title}</strong>
                  <p>{insight.description}</p>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState
              title="Not enough activity yet to calculate this insight."
              description="As registrations, attendance, and certificate records accumulate, this section will populate automatically."
            />
          )}
        </Card>

        <Card elevated className="analytics-panel">
          <SectionHeading eyebrow="Activity" title="Recent database activity" description="A concise, role-aware timeline built from live records." />
          {activity.length ? <ActivityList items={activity} /> : <EmptyState title="No recent activity" description="Recent records will appear here once the selected scope has live data." />}
        </Card>
      </section>

      <Card elevated className="analytics-report">
        <div className="analytics-report__header">
          <SectionHeading
            eyebrow={isStudent ? 'Personal insight' : 'Reports'}
            title={currentReportTitle}
            description={selectedTab?.description ?? 'Choose a report section to inspect a live filtered dataset.'}
          />
          {!isStudent && selectedTab?.reportType && (
            <Button variant="secondary" onClick={handleExport} disabled={reportLoading || !page?.content?.length}>
              <Icon name="download" size={16} />
              Export CSV
            </Button>
          )}
        </div>

        {!isStudent && tabs.length > 1 && <Tabs items={tabs} activeKey={activeTab} onChange={changeTab} />}

        {!isStudent && selectedTab?.reportType && (
          <div className="analytics-filters">
            <SearchBar
              value={draftFilters.search}
              onChange={value => updateDraft('search', value)}
              placeholder="Search the current report"
            />
            <Input
              id="analytics-start-date"
              label="Start date"
              type="date"
              value={formatDateInput(draftFilters.startDate)}
              onChange={event => updateDraft('startDate', event.target.value)}
            />
            <Input
              id="analytics-end-date"
              label="End date"
              type="date"
              value={formatDateInput(draftFilters.endDate)}
              onChange={event => updateDraft('endDate', event.target.value)}
            />
            {activeTab === 'events' && (
              <Select id="analytics-mode" label="Mode" value={draftFilters.mode} onChange={event => updateDraft('mode', event.target.value)}>
                <option value="">Any mode</option>
                <option value="OFFLINE">Offline</option>
                <option value="ONLINE">Online</option>
                <option value="HYBRID">Hybrid</option>
              </Select>
            )}
            {activeTab === 'events' && (
              <Select id="analytics-status" label="Status" value={draftFilters.status} onChange={event => updateDraft('status', event.target.value)}>
                {EVENT_STATUS_OPTIONS.map(([value, label]) => <option key={value || 'any'} value={value}>{label}</option>)}
              </Select>
            )}
            {activeTab === 'registrations' && (
              <Select id="analytics-status" label="Status" value={draftFilters.status} onChange={event => updateDraft('status', event.target.value)}>
                {REGISTRATION_STATUS_OPTIONS.map(([value, label]) => <option key={value || 'any'} value={value}>{label}</option>)}
              </Select>
            )}
            {activeTab === 'attendance' && (
              <Select id="analytics-attendance-status" label="Attendance" value={draftFilters.attendanceStatus} onChange={event => updateDraft('attendanceStatus', event.target.value)}>
                {ATTENDANCE_STATUS_OPTIONS.map(([value, label]) => <option key={value || 'any'} value={value}>{label}</option>)}
              </Select>
            )}
            {activeTab === 'certificates' && (
              <>
                <Select id="analytics-certificate-status" label="Status" value={draftFilters.status} onChange={event => updateDraft('status', event.target.value)}>
                  {CERTIFICATE_STATUS_OPTIONS.map(([value, label]) => <option key={value || 'any'} value={value}>{label}</option>)}
                </Select>
                <Select id="analytics-certificate-type" label="Type" value={draftFilters.certificateType} onChange={event => updateDraft('certificateType', event.target.value)}>
                  {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value || 'any'} value={value}>{label}</option>)}
                </Select>
              </>
            )}
            <Select id="analytics-sort" label="Sort" value={draftFilters.sort} onChange={event => updateDraft('sort', event.target.value)}>
              {sortOptionsForTab(activeTab).map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="analytics-direction" label="Direction" value={draftFilters.direction} onChange={event => updateDraft('direction', event.target.value)}>
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </Select>
            <div className="analytics-filters__actions">
              <Button variant="secondary" onClick={clearFilters}>Clear filters</Button>
              <Button onClick={applyFilters}>Apply filters</Button>
            </div>
          </div>
        )}

        {selectedTab?.reportType && reportLoading && !page ? <LoadingSkeleton lines={7} /> : null}
        {selectedTab?.reportType && reportError ? <ErrorState title="Report unavailable" description={reportError} onRetry={applyFilters} /> : null}

        {!isStudent && activeTab !== 'overview' && page && !reportError && (
          <div className="analytics-report__table">
            <div className="analytics-report__meta">
              <Badge tone="neutral">{formatNumber(page.totalElements)} records</Badge>
              <Badge tone="neutral">{page.totalPages} pages</Badge>
              {reportLoading && <Badge tone="warning">Refreshing</Badge>}
            </div>
            <Table columns={reportColumns} rows={reportRows} emptyMessage={currentData?.emptyStateMessage ?? 'No rows matched the current filters.'} />
            <Pagination page={page.page + 1} totalPages={page.totalPages} onPageChange={nextPage => setActiveFilters(current => ({ ...current, page: nextPage }))} />
          </div>
        )}

        {(!isStudent && activeTab === 'overview') && topItems.length > 0 && (
          <div className="analytics-report__table">
            <SectionHeading eyebrow="Top items" title="Highest activity in the current scope" description="These rows are ordered from the current overview data." />
            <Table columns={EVENT_REPORT_COLUMNS} rows={topItems} emptyMessage="No top items are available yet." />
          </div>
        )}

        {isStudent && (
          <div className="analytics-report__table">
            <SectionHeading eyebrow="Personal timeline" title="Recent registered events" description="Your personal records, shown without exposing any other participant's data." />
            <Table columns={EVENT_REPORT_COLUMNS} rows={topItems} emptyMessage="No personal activity is available yet." />
          </div>
        )}
      </Card>

      {toasts.map(toast => (
        <Toast key={toast.id} message={toast.message} tone={toast.tone} />
      ))}
    </div>
  );
}

function fetchReport(reportType, query) {
  switch (reportType) {
    case 'EVENTS':
      return analyticsApi.listEvents(query);
    case 'REGISTRATIONS':
      return analyticsApi.listRegistrations(query);
    case 'ATTENDANCE':
      return analyticsApi.listAttendance(query);
    case 'CERTIFICATES':
      return analyticsApi.listCertificates(query);
    case 'DEPARTMENTS':
      return analyticsApi.listDepartments(query);
    default:
      return analyticsApi.listEvents(query);
  }
}

function sortOptionsForTab(tabKey) {
  switch (tabKey) {
    case 'events':
      return [
        { value: 'startDateTime', label: 'Start date' },
        { value: 'title', label: 'Title' },
        { value: 'capacity', label: 'Capacity' },
        { value: 'attendance', label: 'Attendance' },
        { value: 'certificates', label: 'Certificates' },
        { value: 'status', label: 'Status' }
      ];
    case 'registrations':
      return [
        { value: 'registrationDate', label: 'Registration date' },
        { value: 'participant', label: 'Participant' },
        { value: 'event', label: 'Event' },
        { value: 'status', label: 'Status' },
        { value: 'waitlist', label: 'Waitlist' }
      ];
    case 'attendance':
      return [
        { value: 'checkInTime', label: 'Check-in time' },
        { value: 'participant', label: 'Participant' },
        { value: 'event', label: 'Event' },
        { value: 'status', label: 'Status' }
      ];
    case 'certificates':
      return [
        { value: 'generatedAt', label: 'Generated at' },
        { value: 'recipient', label: 'Recipient' },
        { value: 'event', label: 'Event' },
        { value: 'status', label: 'Status' }
      ];
    case 'departments':
      return [
        { value: 'registrations', label: 'Registrations' },
        { value: 'attendance', label: 'Attendance' },
        { value: 'certificates', label: 'Certificates' },
        { value: 'name', label: 'Name' }
      ];
    default:
      return [{ value: 'registrationDate', label: 'Registration date' }];
  }
}


