import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import Dialog from '../components/Dialog';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import FilterPanel from '../components/FilterPanel';
import FormSection from '../components/FormSection';
import Input from '../components/Input';
import LoadingButton from '../components/LoadingButton';
import LoadingSkeleton from '../components/LoadingSkeleton';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import SearchBar from '../components/SearchBar';
import Select from '../components/Select';
import Table from '../components/Table';
import Tabs from '../components/Tabs';
import Textarea from '../components/Textarea';
import { SuccessBanner } from '../components/Banner';
import { Toast } from '../components/Toast';
import ValidationMessages from '../components/ValidationMessages';
import { APP_ROUTES } from '../constants/routes';
import { attendanceApi } from '../services/attendanceApi';
import { eventApi } from '../services/eventApi';
import { useAuth } from '../context/AuthContext';
import useToastQueue from '../hooks/useToastQueue';
import { extractValidationMessages, getApiErrorMessage, isNetworkError } from '../utils/apiErrors';
import { buildDisplayName, getPrimaryRole } from '../utils/auth';
import { classNames } from '../utils/classNames';

const ATTENDANCE_TABS = [
  { key: 'overview', label: 'Overview', path: APP_ROUTES.attendance },
  { key: 'scanner', label: 'Scanner', path: APP_ROUTES.attendanceScanner },
  { key: 'history', label: 'History', path: APP_ROUTES.attendanceHistory },
  { key: 'reports', label: 'Reports', path: APP_ROUTES.attendanceReports }
];

const ATTENDANCE_STATUS_OPTIONS = [
  ['PRESENT', 'Present'],
  ['ABSENT', 'Absent'],
  ['LATE', 'Late'],
  ['EXCUSED', 'Excused'],
  ['CANCELLED', 'Cancelled']
];

const CHECK_IN_METHOD_OPTIONS = [
  ['QR', 'QR'],
  ['MANUAL', 'Manual']
];

function unwrap(response) {
  return response?.data?.data ?? null;
}

function unwrapPage(response) {
  const data = unwrap(response);
  return {
    content: data?.content ?? [],
    page: (data?.page ?? 0) + 1,
    size: data?.size ?? 20,
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    first: Boolean(data?.first),
    last: Boolean(data?.last)
  };
}

function formatDateTime(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium' }).format(new Date(value));
}

function formatStatus(value) {
  if (!value) {
    return 'Unknown';
  }
  return value
    .toString()
    .toLowerCase()
    .split('_')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function toneForStatus(status) {
  switch (status) {
    case 'PRESENT':
    case 'APPROVED':
    case 'READY':
      return 'success';
    case 'LATE':
    case 'EXCUSED':
    case 'WAITLISTED':
      return 'warning';
    case 'ABSENT':
    case 'CANCELLED':
    case 'REJECTED':
      return 'neutral';
    default:
      return 'neutral';
  }
}

function parseTab(pathname) {
  if (pathname.endsWith('/scanner')) {
    return 'scanner';
  }
  if (pathname.endsWith('/history')) {
    return 'history';
  }
  if (pathname.endsWith('/reports')) {
    return 'reports';
  }
  return 'overview';
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(url);
}

export default function AttendanceManagementPage() {
  const { user } = useAuth();
  const roleCode = getPrimaryRole(user);
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = parseTab(location.pathname);
  const { toasts, pushToast } = useToastQueue();
  const cameraRef = useRef(null);
  const cameraStreamRef = useRef(null);

  const [events, setEvents] = useState([]);
  const [eventsLoading, setEventsLoading] = useState(true);
  const [eventsError, setEventsError] = useState('');
  const [eventSessions, setEventSessions] = useState([]);
  const [eventSessionsLoading, setEventSessionsLoading] = useState(false);

  const selectedEventId = searchParams.get('eventId') ?? '';

  const [dashboard, setDashboard] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(true);
  const [dashboardError, setDashboardError] = useState('');

  const [sessions, setSessions] = useState([]);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [sessionsError, setSessionsError] = useState('');

  const [historyPage, setHistoryPage] = useState({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0 });
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState('');
  const [historySearch, setHistorySearch] = useState('');
  const [historyStatus, setHistoryStatus] = useState('');
  const [historyPageNumber, setHistoryPageNumber] = useState(1);

  const [report, setReport] = useState(null);
  const [reportLoading, setReportLoading] = useState(true);
  const [reportError, setReportError] = useState('');
  const [reportExporting, setReportExporting] = useState(false);

  const [sessionModalOpen, setSessionModalOpen] = useState(false);
  const [sessionActionLoading, setSessionActionLoading] = useState(false);
  const [sessionForm, setSessionForm] = useState({
    eventId: selectedEventId,
    eventSessionId: '',
    sessionTitle: '',
    remarks: ''
  });
  const [sessionErrors, setSessionErrors] = useState([]);

  const [scannerToken, setScannerToken] = useState('');
  const [validationResult, setValidationResult] = useState(null);
  const [validationLoading, setValidationLoading] = useState(false);
  const [checkInLoading, setCheckInLoading] = useState(false);
  const [checkInStatus, setCheckInStatus] = useState('');
  const [cameraEnabled, setCameraEnabled] = useState(false);

  const [manualForm, setManualForm] = useState({
    attendanceSessionId: '',
    registrationId: '',
    attendanceStatus: 'PRESENT',
    attendanceMethod: 'MANUAL',
    remarks: ''
  });
  const [manualLoading, setManualLoading] = useState(false);
  const [manualErrors, setManualErrors] = useState([]);

  const [currentAction, setCurrentAction] = useState(null);
  const [confirmation, setConfirmation] = useState(null);

  const eventOptions = useMemo(() => events.map(event => ({ value: String(event.id), label: `${event.title}${event.eventCode ? ` · ${event.eventCode}` : ''}` })), [events]);
  const sessionOptions = useMemo(() => sessions.map(session => ({ value: String(session.id), label: `${session.sessionTitle} · ${formatStatus(session.status)}` })), [sessions]);

  const selectedEvent = useMemo(() => events.find(event => String(event.id) === selectedEventId) ?? null, [events, selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadEvents() {
      setEventsLoading(true);
      setEventsError('');
      try {
        const response = await eventApi.listEvents({ page: 0, size: 20 });
        if (!active) {
          return;
        }
        setEvents(unwrapPage(response).content);
      } catch (error) {
        if (!active) {
          return;
        }
        setEventsError(getApiErrorMessage(error, 'CampusSphere attendance services are temporarily unavailable.'));
      } finally {
        if (active) {
          setEventsLoading(false);
        }
      }
    }

    loadEvents();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!selectedEventId) {
      setSessionForm(current => ({ ...current, eventId: '' }));
      return;
    }
    setSessionForm(current => ({ ...current, eventId: selectedEventId }));
  }, [selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadEventSessions() {
      if (!selectedEventId) {
        setEventSessions([]);
        return;
      }
      setEventSessionsLoading(true);
      try {
        const response = await eventApi.listSessions(selectedEventId);
        if (!active) {
          return;
        }
        setEventSessions(unwrap(response) ?? []);
      } catch {
        if (active) {
          setEventSessions([]);
        }
      } finally {
        if (active) {
          setEventSessionsLoading(false);
        }
      }
    }

    loadEventSessions();

    return () => {
      active = false;
    };
  }, [selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      if (activeTab === 'history' || activeTab === 'reports' || activeTab === 'overview' || activeTab === 'scanner') {
        setDashboardLoading(true);
        setDashboardError('');
        try {
          const response = await attendanceApi.getDashboard(selectedEventId ? { eventId: selectedEventId } : {});
          if (!active) {
            return;
          }
          setDashboard(unwrap(response));
        } catch (error) {
          if (!active) {
            return;
          }
          setDashboardError(getApiErrorMessage(error, 'Unable to load the attendance dashboard.'));
        } finally {
          if (active) {
            setDashboardLoading(false);
          }
        }
      }
    }

    loadDashboard();

    return () => {
      active = false;
    };
  }, [activeTab, selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadSessions() {
      setSessionsLoading(true);
      setSessionsError('');
      try {
        const response = await attendanceApi.listSessions(selectedEventId ? { eventId: selectedEventId } : {});
        if (!active) {
          return;
        }
        setSessions(unwrap(response) ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        setSessionsError(getApiErrorMessage(error, 'Unable to load attendance sessions.'));
      } finally {
        if (active) {
          setSessionsLoading(false);
        }
      }
    }

    if (activeTab === 'overview' || activeTab === 'scanner') {
      loadSessions();
    } else {
      setSessionsLoading(false);
    }

    return () => {
      active = false;
    };
  }, [activeTab, selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadHistory() {
      if (activeTab !== 'history') {
        return;
      }
      setHistoryLoading(true);
      setHistoryError('');
      try {
        const response = await attendanceApi.listHistory({
          eventId: selectedEventId || undefined,
          search: historySearch || undefined,
          status: historyStatus || undefined,
          page: historyPageNumber - 1,
          size: 10
        });
        if (!active) {
          return;
        }
        setHistoryPage(unwrapPage(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setHistoryError(getApiErrorMessage(error, 'Unable to load attendance history.'));
      } finally {
        if (active) {
          setHistoryLoading(false);
        }
      }
    }

    loadHistory();

    return () => {
      active = false;
    };
  }, [activeTab, historyPageNumber, historySearch, historyStatus, selectedEventId]);

  useEffect(() => {
    let active = true;

    async function loadReport() {
      if (activeTab !== 'reports') {
        return;
      }
      setReportLoading(true);
      setReportError('');
      try {
        const response = await attendanceApi.getReport(selectedEventId ? { eventId: selectedEventId } : {});
        if (!active) {
          return;
        }
        setReport(unwrap(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setReportError(getApiErrorMessage(error, 'Unable to load attendance reports.'));
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
  }, [activeTab, selectedEventId]);

  useEffect(() => {
    if (!cameraEnabled) {
      cameraStreamRef.current?.getTracks().forEach(track => track.stop());
      cameraStreamRef.current = null;
      return;
    }

    let active = true;

    async function startCamera() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
        if (!active) {
          stream.getTracks().forEach(track => track.stop());
          return;
        }
        cameraStreamRef.current = stream;
        if (cameraRef.current) {
          cameraRef.current.srcObject = stream;
          cameraRef.current.play().catch(() => undefined);
        }
      } catch (error) {
        if (active) {
          pushToast(getApiErrorMessage(error, 'Camera preview could not be started.'), 'warning');
          setCameraEnabled(false);
        }
      }
    }

    if (navigator.mediaDevices?.getUserMedia) {
      startCamera();
    } else {
      pushToast('This browser does not support live camera preview.', 'warning');
      setCameraEnabled(false);
    }

    return () => {
      active = false;
    };
  }, [cameraEnabled, pushToast]);

  const summaryCards = dashboard
    ? [
        { label: 'Participants', value: dashboard.totalParticipants, detail: 'Tracked across the active scope.', tone: 'neutral' },
        { label: 'Present', value: dashboard.present, detail: 'Attendance recorded as present.', tone: 'success' },
        { label: 'Late', value: dashboard.late, detail: 'Participants checked in after the scheduled window.', tone: 'warning' },
        { label: 'Attendance %', value: `${dashboard.attendancePercentage}%`, detail: `Certificate threshold: ${dashboard.certificateThreshold}%`, tone: 'neutral' }
      ]
    : [];

  async function handleTabChange(tabKey) {
    const tab = ATTENDANCE_TABS.find(item => item.key === tabKey);
    if (!tab) {
      return;
    }
    navigate(tab.path + (selectedEventId ? `?eventId=${selectedEventId}` : ''));
  }

  function setEventFilter(nextEventId) {
    const params = new URLSearchParams(searchParams);
    if (nextEventId) {
      params.set('eventId', nextEventId);
    } else {
      params.delete('eventId');
    }
    setSearchParams(params, { replace: true });
  }

  async function handleOpenSession(event) {
    event.preventDefault();
    if (!sessionForm.eventId) {
      setSessionErrors(['Select an event before opening the attendance session.']);
      return;
    }
    setSessionActionLoading(true);
    setSessionErrors([]);
    try {
      await attendanceApi.openSession({
        eventId: Number(sessionForm.eventId),
        eventSessionId: sessionForm.eventSessionId ? Number(sessionForm.eventSessionId) : null,
        sessionTitle: sessionForm.sessionTitle,
        remarks: sessionForm.remarks
      });
      pushToast('Attendance session opened successfully.', 'success');
      setSessionModalOpen(false);
      setSessionForm(current => ({ ...current, sessionTitle: '', eventSessionId: '', remarks: '' }));
    } catch (error) {
      setSessionErrors(extractValidationMessages(error));
      pushToast(getApiErrorMessage(error, 'Unable to open the attendance session.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setSessionActionLoading(false);
    }
  }

  async function handleValidateToken(event) {
    event.preventDefault();
    setValidationLoading(true);
    setValidationResult(null);
    setCheckInStatus('');
    try {
      const response = await attendanceApi.validateQrToken({
        token: scannerToken,
        attendanceSessionId: manualForm.attendanceSessionId ? Number(manualForm.attendanceSessionId) : undefined,
        eventId: selectedEventId ? Number(selectedEventId) : undefined
      });
      const result = unwrap(response);
      setValidationResult(result);
      setCheckInStatus(result?.message ?? 'QR token validated.');
    } catch (error) {
      setCheckInStatus(getApiErrorMessage(error, 'Unable to validate the QR token.'));
      pushToast(getApiErrorMessage(error, 'Unable to validate the QR token.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setValidationLoading(false);
    }
  }

  async function handleCheckIn(event) {
    event.preventDefault();
    if (!manualForm.attendanceSessionId) {
      setCheckInStatus('Select an attendance session before recording check-in.');
      return;
    }
    setCheckInLoading(true);
    try {
      const response = await attendanceApi.checkIn({
        token: scannerToken,
        attendanceSessionId: Number(manualForm.attendanceSessionId),
        attendanceStatus: manualForm.attendanceStatus,
        attendanceMethod: manualForm.attendanceMethod,
        remarks: manualForm.remarks
      });
      const result = unwrap(response);
      setValidationResult(result?.record ?? validationResult);
      setCheckInStatus(result?.message ?? 'Attendance recorded.');
      pushToast('Attendance recorded successfully.', 'success');
    } catch (error) {
      setCheckInStatus(getApiErrorMessage(error, 'Unable to record attendance.'));
      pushToast(getApiErrorMessage(error, 'Unable to record attendance.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setCheckInLoading(false);
    }
  }

  async function handleManualAttendance(event) {
    event.preventDefault();
    if (!manualForm.attendanceSessionId || !manualForm.registrationId) {
      setManualErrors(['Select an attendance session and enter a registration ID.']);
      return;
    }
    setManualLoading(true);
    setManualErrors([]);
    try {
      await attendanceApi.markManualAttendance({
        attendanceSessionId: Number(manualForm.attendanceSessionId),
        registrationId: Number(manualForm.registrationId),
        attendanceStatus: manualForm.attendanceStatus,
        remarks: manualForm.remarks
      });
      pushToast('Manual attendance saved successfully.', 'success');
      setManualForm(current => ({ ...current, registrationId: '', remarks: '' }));
    } catch (error) {
      setManualErrors(extractValidationMessages(error));
      pushToast(getApiErrorMessage(error, 'Unable to save manual attendance.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setManualLoading(false);
    }
  }

  async function handleExport(format) {
    setReportExporting(true);
    try {
      const response = await attendanceApi.exportReport(selectedEventId ? { eventId: selectedEventId } : {}, format);
      downloadBlob(response.data, `campussphere-attendance-report.${format === 'xls' ? 'xls' : 'csv'}`);
      pushToast(`Attendance report exported as ${format.toUpperCase()}.`, 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to export the attendance report.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setReportExporting(false);
    }
  }

  async function handleCloseSession(sessionId) {
    setCurrentAction({ type: 'close-session', sessionId });
    try {
      await attendanceApi.closeSession(sessionId);
      pushToast('Attendance session closed successfully.', 'success');
      setSessions(current => current.map(item => (item.id === sessionId ? { ...item, status: 'CLOSED' } : item)));
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to close the attendance session.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setCurrentAction(null);
    }
  }

  async function handleInvalidateToken(tokenId) {
    setCurrentAction({ type: 'invalidate-token', tokenId });
    try {
      await attendanceApi.invalidateToken(tokenId);
      pushToast('QR token invalidated successfully.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to invalidate the QR token.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setCurrentAction(null);
    }
  }

  const selectedSession = sessions.find(session => String(session.id) === manualForm.attendanceSessionId) ?? null;

  const canManageAttendance = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR', 'FACULTY_COORDINATOR'].includes(roleCode);

  async function confirmCurrentAction() {
    if (!confirmation) {
      return;
    }
    const next = confirmation;
    setConfirmation(null);
    if (next.type === 'close-session') {
      await handleCloseSession(next.sessionId);
    }
    if (next.type === 'invalidate-token') {
      await handleInvalidateToken(next.tokenId);
    }
  }

  return (
    <div className="attendance-workspace">
      <section className="attendance-hero">
        <div className="attendance-hero__copy">
          <Badge tone="neutral">Attendance</Badge>
          <h1>Check in participants with clarity, speed, and a clean audit trail.</h1>
          <p>
            Scan QR tokens, record manual attendance, review history, and export reports. The data
            shown here stays scoped to the authenticated institution and role.
          </p>
          <div className="attendance-hero__actions">
            <Button onClick={() => handleTabChange('scanner')}>Open scanner</Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.attendanceHistory}>View history</Button>
            <Button as={Link} variant="secondary" to={APP_ROUTES.attendanceReports}>Open reports</Button>
          </div>
        </div>
        <Card elevated className="attendance-hero__panel">
          <div className="attendance-hero__panel-head">
            <div>
              <span className="attendance-hero__eyebrow">Current scope</span>
              <strong>{selectedEvent ? selectedEvent.title : 'Institution wide'}</strong>
            </div>
            <Badge tone={dashboard?.liveCheckIns ? 'success' : 'neutral'}>{dashboard?.liveCheckIns ?? 0} live</Badge>
          </div>
          <div className="attendance-hero__panel-copy">
            <p>{selectedEvent ? `Attendance data is filtered to ${selectedEvent.title}.` : 'No event is selected, so the current institution scope is shown.'}</p>
            <div className="attendance-hero__selector">
              <Select id="attendance-event-scope" label="Event scope" value={selectedEventId} onChange={event => setEventFilter(event.target.value)}>
                <option value="">Institution wide</option>
                {eventOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </Select>
            </div>
          </div>
        </Card>
      </section>

      {eventsError && <ErrorState title="Event selection unavailable" description={eventsError} onRetry={() => window.location.reload()} />}

      <Tabs items={ATTENDANCE_TABS} activeKey={activeTab} onChange={handleTabChange} />

      <section className="attendance-summary">
        {dashboardLoading ? (
          <LoadingSkeleton lines={2} />
        ) : dashboardError ? (
          <ErrorState title="Attendance dashboard unavailable" description={dashboardError} onRetry={() => window.location.reload()} />
        ) : summaryCards.length ? (
          summaryCards.map(card => (
            <article key={card.label} className={classNames('attendance-metric', `attendance-metric--${card.tone}`)}>
              <span>{card.label}</span>
              <strong>{card.value}</strong>
              <p>{card.detail}</p>
            </article>
          ))
        ) : (
          <EmptyState
            title="No attendance data yet"
            description="Open an attendance session and record check-ins to populate the dashboard."
            actionLabel={canManageAttendance ? 'Open session' : undefined}
            onAction={canManageAttendance ? () => setSessionModalOpen(true) : undefined}
          />
        )}
      </section>

      {activeTab === 'overview' && (
        <div className="attendance-grid">
          <Card elevated className="attendance-panel attendance-panel--wide">
            <div className="attendance-panel__header">
              <div>
                <h2>Attendance sessions</h2>
                <p>Open and close sessions tied to a specific event or session window.</p>
              </div>
              {canManageAttendance && (
                <Button size="sm" onClick={() => setSessionModalOpen(true)}>
                  Open session
                </Button>
              )}
            </div>
            {sessionsLoading ? (
              <LoadingSkeleton lines={5} />
            ) : sessionsError ? (
              <ErrorState title="Sessions unavailable" description={sessionsError} onRetry={() => window.location.reload()} />
            ) : sessions.length ? (
              <Table
                rows={sessions}
                emptyMessage="No attendance sessions are open yet."
                columns={[
                  {
                    key: 'sessionTitle',
                    header: 'Session',
                    render: row => (
                      <div className="attendance-cell">
                        <strong>{row.sessionTitle}</strong>
                        <span>{row.eventTitle}</span>
                      </div>
                    )
                  },
                  { key: 'status', header: 'Status', render: row => <Badge tone={toneForStatus(row.status)}>{formatStatus(row.status)}</Badge> },
                  { key: 'recordCount', header: 'Records', render: row => row.recordCount },
                  { key: 'completion', header: 'Completion', render: row => `${row.completionPercentage}%` },
                  { key: 'openedAt', header: 'Opened', render: row => formatDateTime(row.openedAt) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <div className="attendance-row-actions">
                        {canManageAttendance && row.status !== 'CLOSED' && (
                          <Button size="sm" variant="secondary" onClick={() => setConfirmation({ type: 'close-session', sessionId: row.id, title: 'Close attendance session', description: `Closing ${row.sessionTitle} will stop further check-ins for this session.` })} disabled={currentAction?.sessionId === row.id}>
                            {currentAction?.sessionId === row.id ? 'Closing...' : 'Close'}
                          </Button>
                        )}
                        {row.qrTokenId && canManageAttendance && (
                          <Button size="sm" variant="secondary" onClick={() => setConfirmation({ type: 'invalidate-token', tokenId: row.qrTokenId, title: 'Invalidate QR token', description: 'This token will stop working for future scans.' })} disabled={currentAction?.tokenId === row.qrTokenId}>
                            Invalidate QR
                          </Button>
                        )}
                      </div>
                    )
                  }
                ]}
              />
            ) : (
              <EmptyState
                title="No attendance sessions yet"
                description="Open a session when the event starts and attendance should be tracked."
                actionLabel={canManageAttendance ? 'Open session' : undefined}
                onAction={canManageAttendance ? () => setSessionModalOpen(true) : undefined}
              />
            )}
          </Card>

          <Card elevated className="attendance-panel">
            <div className="attendance-panel__header">
              <div>
                <h2>Recent scans</h2>
                <p>Latest attendance updates recorded through QR or manual entry.</p>
              </div>
            </div>
            {dashboardLoading ? (
              <LoadingSkeleton lines={5} />
            ) : dashboard?.recentScans?.length ? (
              <div className="attendance-list">
                {dashboard.recentScans.map(scan => (
                  <div key={scan.id} className="attendance-list__item">
                    <div>
                      <strong>{scan.participantName}</strong>
                      <p>{scan.eventTitle} · {scan.attendanceSessionTitle}</p>
                    </div>
                    <Badge tone={toneForStatus(scan.attendanceStatus)}>{formatStatus(scan.attendanceStatus)}</Badge>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState title="No recent scans" description="Attendance will appear here once participants are checked in." />
            )}
          </Card>
        </div>
      )}

      {activeTab === 'scanner' && (
        <div className="attendance-grid attendance-grid--scanner">
          <Card elevated className="attendance-panel">
            <div className="attendance-panel__header">
              <div>
                <h2>QR scanner</h2>
                <p>Use the camera preview or paste a QR token to validate and record attendance.</p>
              </div>
              <Button variant="secondary" size="sm" onClick={() => setCameraEnabled(value => !value)}>
                {cameraEnabled ? 'Stop camera' : 'Start camera'}
              </Button>
            </div>
            <div className="attendance-scanner">
              <div className="attendance-scanner__preview">
                <video ref={cameraRef} autoPlay playsInline muted className="attendance-scanner__video" />
                {!cameraEnabled && (
                  <div className="attendance-scanner__placeholder">
                    <span>Camera preview</span>
                    <p>The browser camera can be enabled for live QR scanning. Manual token entry stays available below.</p>
                  </div>
                )}
              </div>
              <FormSection title="Manual QR input" description="Paste a token to validate or record the scan immediately.">
                <form className="attendance-stack" onSubmit={handleValidateToken}>
                  <Input id="qr-token" label="QR token" value={scannerToken} onChange={event => setScannerToken(event.target.value)} placeholder="Paste the scanned QR token" />
                  <div className="attendance-inline-fields">
                    <Select
                      id="scanner-session"
                      label="Attendance session"
                      value={manualForm.attendanceSessionId}
                      onChange={event => setManualForm(current => ({ ...current, attendanceSessionId: event.target.value }))}
                    >
                      <option value="">Select session</option>
                      {sessionOptions.map(option => (
                        <option key={option.value} value={option.value}>{option.label}</option>
                      ))}
                    </Select>
                    <Select
                      id="scanner-status"
                      label="Status"
                      value={manualForm.attendanceStatus}
                      onChange={event => setManualForm(current => ({ ...current, attendanceStatus: event.target.value }))}
                    >
                      {ATTENDANCE_STATUS_OPTIONS.map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                      ))}
                    </Select>
                  </div>
                  <Textarea id="scanner-remarks" label="Remarks" value={manualForm.remarks} onChange={event => setManualForm(current => ({ ...current, remarks: event.target.value }))} rows={3} />
                  <div className="attendance-actions">
                    <LoadingButton loading={validationLoading} type="submit">Validate QR</LoadingButton>
                    <LoadingButton loading={checkInLoading} variant="secondary" onClick={handleCheckIn} disabled={!scannerToken || !manualForm.attendanceSessionId}>Record attendance</LoadingButton>
                  </div>
                </form>
              </FormSection>
            </div>
          </Card>

          <Card elevated className="attendance-panel">
            <div className="attendance-panel__header">
              <div>
                <h2>Validation result</h2>
                <p>Clear feedback appears here instead of raw API output.</p>
              </div>
            </div>
            {checkInStatus && <SuccessBanner message={checkInStatus} />}
            {validationResult ? (
              <div className="attendance-participant">
                <div className="attendance-participant__header">
                  <strong>{validationResult.participantName}</strong>
                  <Badge tone={validationResult.valid ? 'success' : 'warning'}>{validationResult.valid ? 'Valid' : 'Review'}</Badge>
                </div>
                <dl className="attendance-participant__details">
                  <div><dt>Event</dt><dd>{validationResult.eventTitle}</dd></div>
                  <div><dt>Registration</dt><dd>{validationResult.registrationId ?? '-'}</dd></div>
                  <div><dt>Status</dt><dd>{validationResult.message}</dd></div>
                  <div><dt>Scan</dt><dd>{validationResult.alreadyMarked ? 'Already recorded' : validationResult.used ? 'Used' : validationResult.expired ? 'Expired' : 'Ready'}</dd></div>
                </dl>
              </div>
            ) : (
              <EmptyState title="No validation yet" description="Validate a QR token to see the participant details and eligibility result." />
            )}
          </Card>

          <Card elevated className="attendance-panel">
            <div className="attendance-panel__header">
              <div>
                <h2>Manual attendance</h2>
                <p>Search by registration and record present, late, absent, or excused attendance.</p>
              </div>
            </div>
            <ValidationMessages messages={manualErrors} />
            <form className="attendance-stack" onSubmit={handleManualAttendance}>
              <Select
                id="manual-session"
                label="Attendance session"
                value={manualForm.attendanceSessionId}
                onChange={event => setManualForm(current => ({ ...current, attendanceSessionId: event.target.value }))}
              >
                <option value="">Select session</option>
                {sessionOptions.map(option => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </Select>
              <Input id="manual-registration" label="Registration ID" value={manualForm.registrationId} onChange={event => setManualForm(current => ({ ...current, registrationId: event.target.value }))} />
              <Select
                id="manual-status"
                label="Attendance status"
                value={manualForm.attendanceStatus}
                onChange={event => setManualForm(current => ({ ...current, attendanceStatus: event.target.value }))}
              >
                {ATTENDANCE_STATUS_OPTIONS.map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </Select>
              <Select
                id="manual-method"
                label="Method"
                value={manualForm.attendanceMethod}
                onChange={event => setManualForm(current => ({ ...current, attendanceMethod: event.target.value }))}
              >
                {CHECK_IN_METHOD_OPTIONS.map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </Select>
              <Textarea id="manual-remarks" label="Remarks" value={manualForm.remarks} onChange={event => setManualForm(current => ({ ...current, remarks: event.target.value }))} rows={3} />
              <LoadingButton loading={manualLoading} type="submit">Save manual attendance</LoadingButton>
            </form>
            {selectedSession && (
              <div className="attendance-note">
                Attendance will be recorded against <strong>{selectedSession.sessionTitle}</strong>.
              </div>
            )}
          </Card>
        </div>
      )}

      {activeTab === 'history' && (
        <div className="attendance-grid attendance-grid--history">
          <FilterPanel title="Attendance filters">
            <div className="attendance-stack">
              <SearchBar value={historySearch} onChange={setHistorySearch} placeholder="Search by participant or registration" />
              <Select id="history-status" label="Status" value={historyStatus} onChange={event => setHistoryStatus(event.target.value)}>
                <option value="">All statuses</option>
                {ATTENDANCE_STATUS_OPTIONS.map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </Select>
              <Button onClick={() => setHistoryPageNumber(1)}>Apply filters</Button>
            </div>
          </FilterPanel>

          <Card elevated className="attendance-panel attendance-panel--wide">
            <div className="attendance-panel__header">
              <div>
                <h2>Attendance history</h2>
                <p>Review attendance records for the current scope.</p>
              </div>
            </div>
            {historyLoading ? (
              <LoadingSkeleton lines={6} />
            ) : historyError ? (
              <ErrorState title="History unavailable" description={historyError} onRetry={() => window.location.reload()} />
            ) : historyPage.content.length ? (
              <>
                <Table
                  rows={historyPage.content}
                  emptyMessage="No attendance records matched the filters."
                  columns={[
                    { key: 'participantName', header: 'Participant', render: row => <strong>{row.participantName}</strong> },
                    { key: 'registrationNumber', header: 'Registration', render: row => row.registrationNumber },
                    { key: 'sessionTitle', header: 'Session', render: row => row.attendanceSessionTitle },
                    { key: 'status', header: 'Status', render: row => <Badge tone={toneForStatus(row.attendanceStatus)}>{formatStatus(row.attendanceStatus)}</Badge> },
                    { key: 'method', header: 'Method', render: row => formatStatus(row.attendanceMethod) },
                    { key: 'checkInTime', header: 'Check-in', render: row => formatDateTime(row.checkInTime) }
                  ]}
                />
                <Pagination
                  page={historyPage.page}
                  totalPages={Math.max(historyPage.totalPages, 1)}
                  onPageChange={setHistoryPageNumber}
                />
              </>
            ) : (
              <EmptyState title="No attendance history yet" description="Records will appear here as participants are checked in." />
            )}
          </Card>
        </div>
      )}

      {activeTab === 'reports' && (
        <div className="attendance-grid attendance-grid--reports">
          <Card elevated className="attendance-panel attendance-panel--wide">
            <div className="attendance-panel__header">
              <div>
                <h2>Attendance report</h2>
                <p>Summary numbers and export actions for the current event scope.</p>
              </div>
              <div className="attendance-actions">
                <LoadingButton loading={reportExporting} variant="secondary" onClick={() => handleExport('csv')}>Export CSV</LoadingButton>
                <LoadingButton loading={reportExporting} variant="secondary" onClick={() => handleExport('xls')}>Export Excel</LoadingButton>
              </div>
            </div>
            {reportLoading ? (
              <LoadingSkeleton lines={5} />
            ) : reportError ? (
              <ErrorState title="Reports unavailable" description={reportError} onRetry={() => window.location.reload()} />
            ) : report ? (
              <>
                <div className="attendance-report__metrics">
                  <article className="attendance-report__metric">
                    <span>Total records</span>
                    <strong>{report.totalRecords}</strong>
                  </article>
                  <article className="attendance-report__metric">
                    <span>Present</span>
                    <strong>{report.present}</strong>
                  </article>
                  <article className="attendance-report__metric">
                    <span>Late</span>
                    <strong>{report.late}</strong>
                  </article>
                  <article className="attendance-report__metric">
                    <span>Attendance %</span>
                    <strong>{report.attendancePercentage}%</strong>
                  </article>
                </div>
                <div className="attendance-report__meta">
                  <strong>{report.eventTitle}</strong>
                  <span>{report.attendanceSessionTitle ?? 'All sessions'} · {formatDate(selectedEvent?.startDateTime)}</span>
                </div>
                {report.rows?.length ? (
                  <Table
                    rows={report.rows}
                    emptyMessage="No rows available for the selected report."
                    columns={[
                      { key: 'participantName', header: 'Participant', render: row => <strong>{row.participantName}</strong> },
                      { key: 'registrationNumber', header: 'Registration', render: row => row.registrationNumber },
                      { key: 'status', header: 'Status', render: row => <Badge tone={toneForStatus(row.attendanceStatus)}>{formatStatus(row.attendanceStatus)}</Badge> },
                      { key: 'method', header: 'Method', render: row => formatStatus(row.attendanceMethod) },
                      { key: 'certificate', header: 'Certificate', render: row => (row.certificateReady ? 'Ready' : 'Not ready') }
                    ]}
                  />
                ) : (
                  <EmptyState title="No report rows yet" description="The report becomes useful once attendance is recorded for the selected scope." />
                )}
              </>
            ) : (
              <EmptyState title="No report available" description="Open an attendance session and record scans to generate a report." />
            )}
          </Card>
        </div>
      )}

      <Modal
        open={sessionModalOpen}
        title="Open attendance session"
        description="Create a session for QR scanning or manual marking."
        onClose={() => setSessionModalOpen(false)}
        onSubmit={handleOpenSession}
        submitLabel="Open session"
        loading={sessionActionLoading}
      >
        <ValidationMessages messages={sessionErrors} />
        <Select id="session-event" label="Event" value={sessionForm.eventId} onChange={event => setSessionForm(current => ({ ...current, eventId: event.target.value }))}>
          <option value="">Select event</option>
          {eventOptions.map(option => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </Select>
        <Select
          id="session-event-session"
          label="Event schedule session"
          value={sessionForm.eventSessionId}
          onChange={event => setSessionForm(current => ({ ...current, eventSessionId: event.target.value }))}
          helperText={eventSessionsLoading ? 'Loading schedule sessions...' : 'Optional, but recommended when the event has distinct schedules.'}
        >
          <option value="">No linked event session</option>
          {eventSessions.map(item => (
            <option key={item.id} value={item.id}>{item.title}</option>
          ))}
        </Select>
        <Input id="session-title" label="Session title" value={sessionForm.sessionTitle} onChange={event => setSessionForm(current => ({ ...current, sessionTitle: event.target.value }))} />
        <Textarea id="session-remarks" label="Remarks" value={sessionForm.remarks} onChange={event => setSessionForm(current => ({ ...current, remarks: event.target.value }))} rows={3} />
      </Modal>

      <Dialog
        open={Boolean(confirmation)}
        title={confirmation?.title ?? 'Confirm action'}
        description={confirmation?.description ?? ''}
        confirmLabel="Confirm"
        onClose={() => setConfirmation(null)}
        onConfirm={confirmCurrentAction}
      />

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
