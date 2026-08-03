import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
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
import { ErrorBanner, SuccessBanner } from '../components/Banner';
import { Toast } from '../components/Toast';
import ValidationMessages from '../components/ValidationMessages';
import { APP_ROUTES } from '../constants/routes';
import { registrationApi } from '../services/registrationApi';
import { useAuth } from '../context/AuthContext';
import useToastQueue from '../hooks/useToastQueue';
import { extractValidationMessages, getApiErrorMessage, isNetworkError } from '../utils/apiErrors';
import { buildDisplayName, getPrimaryRole } from '../utils/auth';
import { classNames } from '../utils/classNames';

const WORKSPACE_TABS = [
  { key: 'overview', label: 'Overview' },
  { key: 'registrations', label: 'All registrations' },
  { key: 'mine', label: 'My registrations' },
  { key: 'teams', label: 'Teams' },
  { key: 'notifications', label: 'Notifications' }
];

const REGISTRATION_STATUS_OPTIONS = [
  ['', 'All statuses'],
  ['PENDING', 'Pending'],
  ['APPROVED', 'Approved'],
  ['REJECTED', 'Rejected'],
  ['WAITLISTED', 'Waitlisted'],
  ['CANCELLED', 'Cancelled']
];

const REGISTRATION_TYPE_OPTIONS = [
  ['INDIVIDUAL', 'Individual'],
  ['TEAM', 'Team']
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
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium'
  }).format(new Date(value));
}

function toneForStatus(status) {
  switch (status) {
    case 'APPROVED':
      return 'success';
    case 'PENDING':
    case 'WAITLISTED':
      return 'warning';
    case 'REJECTED':
    case 'CANCELLED':
      return 'neutral';
    default:
      return 'neutral';
  }
}

function friendlyStatus(status) {
  if (!status) {
    return 'Unknown';
  }
  return status
    .toString()
    .toLowerCase()
    .split('_')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function parseRoute(pathname) {
  const segments = pathname.replace(/^\/+|\/+$/g, '').split('/');
  const eventIndex = segments.indexOf('events');
  if (eventIndex >= 0 && /^\d+$/.test(segments[eventIndex + 1] ?? '') && segments[eventIndex + 2] === 'register') {
    return { view: 'event-register', eventId: segments[eventIndex + 1] };
  }
  if (segments.includes('registrations')) {
    return { view: 'workspace' };
  }
  return { view: 'workspace' };
}

function SummaryCard({ title, value, detail }) {
  return (
    <Card elevated className="workspace-stat">
      <span>{title}</span>
      <strong>{value}</strong>
      <p>{detail}</p>
    </Card>
  );
}

function StatusBadge({ status }) {
  return <Badge tone={toneForStatus(status)}>{friendlyStatus(status)}</Badge>;
}

function buildValidationMap(messages = []) {
  return messages.reduce((acc, message) => {
    acc.general.push(message);
    return acc;
  }, { general: [] });
}

export default function RegistrationManagementPage() {
  const { user } = useAuth();
  const roleCode = getPrimaryRole(user);
  const location = useLocation();
  const { eventId } = useParams();
  const route = parseRoute(location.pathname);
  const { toasts, pushToast } = useToastQueue();

  const [dashboard, setDashboard] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(true);
  const [dashboardError, setDashboardError] = useState('');

  const [registrationSearch, setRegistrationSearch] = useState('');
  const [registrationStatus, setRegistrationStatus] = useState('');
  const [registrationPage, setRegistrationPage] = useState(1);
  const [registrationList, setRegistrationList] = useState({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 1 });
  const [registrationsLoading, setRegistrationsLoading] = useState(true);
  const [registrationsError, setRegistrationsError] = useState('');
  const [selectedIds, setSelectedIds] = useState([]);

  const [mySearch, setMySearch] = useState('');
  const [myStatus, setMyStatus] = useState('');
  const [myPage, setMyPage] = useState(1);
  const [myRegistrations, setMyRegistrations] = useState({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 1 });
  const [myLoading, setMyLoading] = useState(true);
  const [myError, setMyError] = useState('');

  const [notifications, setNotifications] = useState([]);
  const [notificationsLoading, setNotificationsLoading] = useState(true);
  const [notificationsError, setNotificationsError] = useState('');

  const [teams, setTeams] = useState([]);
  const [teamsLoading, setTeamsLoading] = useState(true);
  const [teamsError, setTeamsError] = useState('');

  const [formPayload, setFormPayload] = useState({
    registrationType: 'INDIVIDUAL',
    teamName: '',
    teamCode: '',
    remarks: ''
  });
  const [formContext, setFormContext] = useState(null);
  const [eventForm, setEventForm] = useState(null);
  const [eventFormLoading, setEventFormLoading] = useState(Boolean(eventId));
  const [eventFormError, setEventFormError] = useState('');
  const [registrationSubmitting, setRegistrationSubmitting] = useState(false);
  const [registrationSuccess, setRegistrationSuccess] = useState('');
  const [registrationErrors, setRegistrationErrors] = useState([]);

  const [createTeamOpen, setCreateTeamOpen] = useState(false);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [teamActionLoading, setTeamActionLoading] = useState(false);
  const [teamForm, setTeamForm] = useState({ teamName: '', teamCode: '' });
  const [inviteForm, setInviteForm] = useState({ invitedUserId: '', message: '' });
  const [activeTeamId, setActiveTeamId] = useState(null);
  const [teamFormErrors, setTeamFormErrors] = useState([]);
  const [inviteErrors, setInviteErrors] = useState([]);

  const isAdmin = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR', 'FACULTY_COORDINATOR', 'ORGANISER'].includes(roleCode);
  const workspaceHasBackend = Boolean(dashboard || registrationList.content.length || myRegistrations.content.length || notifications.length || teams.length);

  const pageTitle = useMemo(() => {
    if (route.view === 'event-register' && eventForm?.context?.eventTitle) {
      return `Register: ${eventForm.context.eventTitle}`;
    }
    return 'Registrations';
  }, [eventForm?.context?.eventTitle, route.view]);

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      setDashboardLoading(true);
      setDashboardError('');
      try {
        const response = await registrationApi.getDashboard();
        if (!active) {
          return;
        }
        setDashboard(unwrap(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setDashboardError(getApiErrorMessage(error, 'Unable to load the registration workspace right now.'));
      } finally {
        if (active) {
          setDashboardLoading(false);
        }
      }
    }

    if (route.view === 'workspace') {
      loadDashboard();
    } else {
      setDashboardLoading(false);
    }

    return () => {
      active = false;
    };
  }, [route.view]);

  useEffect(() => {
    let active = true;

    async function loadRegistrations() {
      if (route.view !== 'workspace') {
        return;
      }
      setRegistrationsLoading(true);
      setRegistrationsError('');
      try {
        const response = await registrationApi.listRegistrations({
          search: registrationSearch || undefined,
          status: registrationStatus || undefined,
          page: registrationPage - 1,
          size: 10
        });
        if (!active) {
          return;
        }
        setRegistrationList(unwrapPage(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setRegistrationsError(getApiErrorMessage(error, 'Unable to load registrations.'));
      } finally {
        if (active) {
          setRegistrationsLoading(false);
        }
      }
    }

    loadRegistrations();
    return () => {
      active = false;
    };
  }, [registrationPage, registrationSearch, registrationStatus, route.view]);

  useEffect(() => {
    let active = true;

    async function loadMyRegistrations() {
      if (route.view !== 'workspace') {
        return;
      }
      setMyLoading(true);
      setMyError('');
      try {
        const response = await registrationApi.listMyRegistrations({
          search: mySearch || undefined,
          status: myStatus || undefined,
          page: myPage - 1,
          size: 10
        });
        if (!active) {
          return;
        }
        setMyRegistrations(unwrapPage(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setMyError(getApiErrorMessage(error, 'Unable to load your registrations.'));
      } finally {
        if (active) {
          setMyLoading(false);
        }
      }
    }

    loadMyRegistrations();
    return () => {
      active = false;
    };
  }, [myPage, mySearch, myStatus, route.view]);

  useEffect(() => {
    let active = true;

    async function loadNotifications() {
      if (route.view !== 'workspace') {
        return;
      }
      setNotificationsLoading(true);
      setNotificationsError('');
      try {
        const response = await registrationApi.listNotifications();
        if (!active) {
          return;
        }
        setNotifications(unwrap(response) ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        setNotificationsError(getApiErrorMessage(error, 'Unable to load notifications.'));
      } finally {
        if (active) {
          setNotificationsLoading(false);
        }
      }
    }

    loadNotifications();
    return () => {
      active = false;
    };
  }, [route.view]);

  useEffect(() => {
    let active = true;

    async function loadEventForm() {
      if (route.view !== 'event-register' || !eventId) {
        setEventFormLoading(false);
        return;
      }
      setEventFormLoading(true);
      setEventFormError('');
      try {
        const response = await registrationApi.getEventRegistrationForm(eventId);
        if (!active) {
          return;
        }
        const data = unwrap(response);
        setEventForm(data);
        setFormContext(data?.context ?? null);
        setTeams(data?.teams ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        setEventFormError(getApiErrorMessage(error, 'Unable to load the registration form right now.'));
      } finally {
        if (active) {
          setEventFormLoading(false);
        }
      }
    }

    loadEventForm();
    return () => {
      active = false;
    };
  }, [eventId, route.view]);

  async function refreshTeams() {
    if (!eventId) {
      return;
    }
    try {
      const response = await registrationApi.listTeams(eventId);
      setTeams(unwrap(response) ?? []);
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to refresh teams.'), 'error');
    }
  }

  function resetFilters() {
    setRegistrationSearch('');
    setRegistrationStatus('');
    setRegistrationPage(1);
  }

  function resetMyFilters() {
    setMySearch('');
    setMyStatus('');
    setMyPage(1);
  }

  async function submitEventRegistration(event) {
    event.preventDefault();
    setRegistrationSubmitting(true);
    setRegistrationErrors([]);
    setRegistrationSuccess('');

    try {
      const payload = {
        registrationType: formPayload.registrationType,
        teamName: formPayload.registrationType === 'TEAM' ? formPayload.teamName : null,
        teamCode: formPayload.registrationType === 'TEAM' ? formPayload.teamCode : null,
        remarks: formPayload.remarks || null
      };
      const response = await registrationApi.registerForEvent(eventId, payload);
      const data = unwrap(response);
      setRegistrationSuccess(response?.data?.message ?? 'Registration submitted successfully.');
      pushToast(response?.data?.message ?? 'Registration submitted successfully.');
      if (data?.eventId) {
        setEventForm(prev => prev ? { ...prev, registrations: [...(prev.registrations ?? []), data] } : prev);
      }
      setFormPayload({ registrationType: 'INDIVIDUAL', teamName: '', teamCode: '', remarks: '' });
      await refreshTeams();
    } catch (error) {
      const messages = extractValidationMessages(error);
      setRegistrationErrors(messages.length ? messages : [getApiErrorMessage(error, 'Unable to submit registration.')]);
    } finally {
      setRegistrationSubmitting(false);
    }
  }

  async function submitCreateTeam(event) {
    event.preventDefault();
    setTeamActionLoading(true);
    setTeamFormErrors([]);
    try {
      await registrationApi.createTeam(eventId, teamForm);
      pushToast('Team created successfully.');
      setCreateTeamOpen(false);
      setTeamForm({ teamName: '', teamCode: '' });
      await refreshTeams();
    } catch (error) {
      const messages = extractValidationMessages(error);
      setTeamFormErrors(messages.length ? messages : [getApiErrorMessage(error, 'Unable to create the team.')]);
    } finally {
      setTeamActionLoading(false);
    }
  }

  async function submitInvite(event) {
    event.preventDefault();
    if (!activeTeamId) {
      return;
    }
    setTeamActionLoading(true);
    setInviteErrors([]);
    try {
      await registrationApi.inviteTeamMember(activeTeamId, {
        invitedUserId: Number(inviteForm.invitedUserId),
        message: inviteForm.message || null
      });
      pushToast('Invitation sent successfully.');
      setInviteOpen(false);
      setInviteForm({ invitedUserId: '', message: '' });
    } catch (error) {
      const messages = extractValidationMessages(error);
      setInviteErrors(messages.length ? messages : [getApiErrorMessage(error, 'Unable to send the invitation.')]);
    } finally {
      setTeamActionLoading(false);
    }
  }

  async function handleMarkRead(id) {
    try {
      await registrationApi.markNotificationRead(id);
      const response = await registrationApi.listNotifications();
      setNotifications(unwrap(response) ?? []);
      pushToast('Notification marked as read.');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the notification.'), 'error');
    }
  }

  async function handleCancelRegistration(id) {
    try {
      await registrationApi.cancelRegistration(id);
      pushToast('Registration cancelled successfully.');
      const response = await registrationApi.listMyRegistrations({
        search: mySearch || undefined,
        status: myStatus || undefined,
        page: myPage - 1,
        size: 10
      });
      setMyRegistrations(unwrapPage(response));
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to cancel the registration.'), 'error');
    }
  }

  async function handleDecision(id, status) {
    try {
      await registrationApi.decideRegistration(id, { status, rejectionReason: null, remarks: null });
      pushToast(`Registration ${status.toLowerCase()}.`);
      const response = await registrationApi.listRegistrations({
        search: registrationSearch || undefined,
        status: registrationStatus || undefined,
        page: registrationPage - 1,
        size: 10
      });
      setRegistrationList(unwrapPage(response));
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the registration.'), 'error');
    }
  }

  async function handleBulkDecision(status) {
    if (!selectedIds.length) {
      pushToast('Select at least one registration first.', 'warning');
      return;
    }
    try {
      await Promise.all(selectedIds.map(id => registrationApi.decideRegistration(id, { status, rejectionReason: null, remarks: null })));
      pushToast(`Selected registrations ${status.toLowerCase()}.`);
      setSelectedIds([]);
      const response = await registrationApi.listRegistrations({
        search: registrationSearch || undefined,
        status: registrationStatus || undefined,
        page: registrationPage - 1,
        size: 10
      });
      setRegistrationList(unwrapPage(response));
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to complete the bulk action.'), 'error');
    }
  }

  async function handleInvitationResponse(id, accepted) {
    try {
      await (accepted ? registrationApi.acceptInvitation(id) : registrationApi.rejectInvitation(id));
      pushToast(accepted ? 'Invitation accepted.' : 'Invitation rejected.');
      const response = await registrationApi.getEventRegistrationForm(eventId);
      const data = unwrap(response);
      setEventForm(data);
      setFormContext(data?.context ?? null);
      setTeams(data?.teams ?? []);
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the invitation.'), 'error');
    }
  }

  async function handleTeamOwnershipTransfer(teamId, newLeaderUserId) {
    try {
      await registrationApi.transferTeamOwnership(teamId, { newLeaderUserId: Number(newLeaderUserId) });
      pushToast('Team ownership transferred.');
      await refreshTeams();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to transfer ownership.'), 'error');
    }
  }

  async function handleLeaveTeam(teamId) {
    try {
      await registrationApi.leaveTeam(teamId);
      pushToast('You left the team.');
      await refreshTeams();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to leave the team.'), 'error');
    }
  }

  const overviewCards = [
    { title: 'Total registrations', value: dashboard?.total ?? 0, detail: 'All records available to your role.' },
    { title: 'Approved', value: dashboard?.approved ?? 0, detail: 'Registrations confirmed for events.' },
    { title: 'Pending', value: dashboard?.pending ?? 0, detail: 'Waiting for review or publication.' },
    { title: 'Waitlisted', value: dashboard?.waitlisted ?? 0, detail: 'Moved to the queue when capacity is full.' }
  ];

  const selectedCount = selectedIds.length;
  const canManageAll = isAdmin;
  const allRows = registrationList.content;
  const myRows = myRegistrations.content;

  const teamRows = teams.map(team => ({
    ...team,
    memberCount: team.memberCount ?? 0
  }));

  function toggleSelected(id, checked) {
    setSelectedIds(current => checked ? [...new Set([...current, id])] : current.filter(item => item !== id));
  }

  if (route.view === 'event-register' && eventFormLoading) {
    return <LoadingSkeleton lines={8} />;
  }

  if (route.view === 'event-register' && eventFormError) {
    return <ErrorState title="Registration unavailable" description={eventFormError} onRetry={() => window.location.reload()} />;
  }

  if (route.view === 'event-register') {
    return (
      <div className="registration-workspace">
        <section className="dashboard-page__hero workspace-hero">
          <div>
            <p className="dashboard-page__eyebrow">Event registration</p>
            <h1>{pageTitle}</h1>
            <p>
              Register for the event, create a team if needed, and stay within the published registration window and eligibility rules.
            </p>
          </div>
          <div className="workspace-actions">
            <Button as={Link} variant="secondary" to={APP_ROUTES.events}>
              Back to events
            </Button>
          </div>
        </section>

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-summary">
            <div className="workspace-summary__grid">
              <div className="workspace-summary__item"><span>Event</span><strong>{formContext?.eventTitle ?? '-'}</strong></div>
              <div className="workspace-summary__item"><span>Window</span><strong>{formatDateTime(formContext?.registrationEndDateTime)}</strong></div>
              <div className="workspace-summary__item"><span>Seats remaining</span><strong>{formContext?.seatsRemaining ?? 'Open'}</strong></div>
              <div className="workspace-summary__item"><span>Status</span><strong>{formContext?.registrationOpen ? 'Open' : 'Closed'}</strong></div>
            </div>
          </Card>
          <Card elevated className="workspace-summary">
            <h2>What this form does</h2>
            <ul className="workspace-summary__list">
              <li>Validates your account against the event timeline and eligibility rules.</li>
              <li>Supports individual or team registration when the event allows it.</li>
              <li>Shows live messages instead of raw backend errors.</li>
            </ul>
          </Card>
        </div>

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Register now</h2>
                <p>Choose the registration type and submit a concise request.</p>
              </div>
            </div>
            <SuccessBanner message={registrationSuccess} />
            <ErrorBanner message={registrationErrors[0]} />
            <form className="registration-form" onSubmit={submitEventRegistration}>
              <FormSection title="Registration details" description="Keep the request focused and aligned to the event settings.">
                <div className="registration-form__grid">
                  <Select
                    id="registrationType"
                    label="Registration type"
                    value={formPayload.registrationType}
                    onChange={event => setFormPayload(current => ({ ...current, registrationType: event.target.value }))}
                  >
                    {REGISTRATION_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                  </Select>
                  {formPayload.registrationType === 'TEAM' && (
                    <>
                      <Input
                        id="teamName"
                        label="Team name"
                        value={formPayload.teamName}
                        onChange={event => setFormPayload(current => ({ ...current, teamName: event.target.value }))}
                        placeholder="For example, Nexus Builders"
                      />
                      <Input
                        id="teamCode"
                        label="Team code"
                        value={formPayload.teamCode}
                        onChange={event => setFormPayload(current => ({ ...current, teamCode: event.target.value }))}
                        placeholder="Optional team code"
                      />
                    </>
                  )}
                  <Textarea
                    id="remarks"
                    label="Remarks"
                    value={formPayload.remarks}
                    onChange={event => setFormPayload(current => ({ ...current, remarks: event.target.value }))}
                    rows={4}
                    placeholder="Add a short note if needed."
                  />
                </div>
                <ValidationMessages messages={registrationErrors} />
              </FormSection>
              <div className="workspace-actions">
                <LoadingButton type="submit" loading={registrationSubmitting} disabled={!formContext?.canRegister}>
                  Submit registration
                </LoadingButton>
                <Button as={Link} variant="secondary" to={APP_ROUTES.events}>
                  Cancel
                </Button>
              </div>
            </form>
          </Card>

          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Eligibility snapshot</h2>
                <p>{formContext?.message ?? 'The backend will validate your account before it accepts the registration.'}</p>
              </div>
            </div>
            <div className="workspace-summary__grid">
              <div className="workspace-summary__item"><span>Team event</span><strong>{formContext?.teamEvent ? 'Yes' : 'No'}</strong></div>
              <div className="workspace-summary__item"><span>Approval</span><strong>{formContext?.approvalRequired ? 'Required' : 'Not required'}</strong></div>
              <div className="workspace-summary__item"><span>Waitlist</span><strong>{formContext?.waitlistEnabled ? 'Enabled' : 'Disabled'}</strong></div>
              <div className="workspace-summary__item"><span>Type</span><strong>{friendlyStatus(formContext?.mode)}</strong></div>
            </div>
            <div className="workspace-footer">
              <span>Notes</span>
              <p>
                If registration is full, the system can place you on the waitlist. If you later cancel, the next queued
                participant is promoted automatically.
              </p>
            </div>
          </Card>
        </div>

        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>Teams and invitations</h2>
              <p>Manage team participation after the team registration request is created.</p>
            </div>
            <div className="workspace-actions">
              <Button variant="secondary" onClick={() => setCreateTeamOpen(true)}>
                Create team
              </Button>
            </div>
          </div>
          {teamRows.length > 0 ? (
            <Table
              columns={[
                { key: 'teamName', header: 'Team', render: row => <strong>{row.teamName}</strong> },
                { key: 'teamCode', header: 'Code', render: row => row.teamCode ?? '-' },
                { key: 'leaderName', header: 'Leader', render: row => row.leaderName ?? '-' },
                { key: 'memberCount', header: 'Members', render: row => row.memberCount },
                { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                {
                  key: 'actions',
                  header: 'Actions',
                  render: row => (
                    <div className="workspace-actions">
                      <Button variant="secondary" size="sm" onClick={() => { setActiveTeamId(row.id); setInviteOpen(true); }}>
                        Invite
                      </Button>
                      <Button variant="secondary" size="sm" onClick={() => handleLeaveTeam(row.id)}>
                        Leave
                      </Button>
                    </div>
                  )
                }
              ]}
              rows={teamRows}
              emptyMessage="No teams exist for this event yet."
            />
          ) : (
            <EmptyState
              title="No teams yet"
              description="Create a team first if this event uses team registrations."
              actionLabel="Create team"
              onAction={() => setCreateTeamOpen(true)}
            />
          )}
        </Card>

        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>Registration history</h2>
              <p>Recent registrations and invitations for this event.</p>
            </div>
          </div>
          {eventForm?.registrations?.length ? (
            <Table
              columns={[
                { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                { key: 'registrationNumber', header: 'Registration #', render: row => row.registrationNumber },
                { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                { key: 'registrationDate', header: 'Submitted', render: row => formatDateTime(row.registrationDate) }
              ]}
              rows={eventForm.registrations}
            />
          ) : (
            <EmptyState title="No registrations yet" description="Submitted registrations will appear here after the first request." />
          )}
        </Card>

        <Modal
          open={createTeamOpen}
          title="Create team"
          description="Create a team for this event before inviting other participants."
          onClose={() => setCreateTeamOpen(false)}
          onSubmit={submitCreateTeam}
          submitLabel="Create team"
          loading={teamActionLoading}
        >
          <ValidationMessages messages={teamFormErrors} />
          <Input id="create-team-name" label="Team name" value={teamForm.teamName} onChange={event => setTeamForm(current => ({ ...current, teamName: event.target.value }))} />
          <Input id="create-team-code" label="Team code" value={teamForm.teamCode} onChange={event => setTeamForm(current => ({ ...current, teamCode: event.target.value }))} />
        </Modal>

        <Modal
          open={inviteOpen}
          title="Invite member"
          description="Invite a user from the same institution to join the team."
          onClose={() => setInviteOpen(false)}
          onSubmit={submitInvite}
          submitLabel="Send invitation"
          loading={teamActionLoading}
        >
          <ValidationMessages messages={inviteErrors} />
          <Input
            id="invite-user-id"
            label="User ID"
            value={inviteForm.invitedUserId}
            onChange={event => setInviteForm(current => ({ ...current, invitedUserId: event.target.value }))}
          />
          <Textarea
            id="invite-message"
            label="Message"
            value={inviteForm.message}
            onChange={event => setInviteForm(current => ({ ...current, message: event.target.value }))}
            rows={3}
          />
        </Modal>

        {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
      </div>
    );
  }

  return (
    <div className="registration-workspace">
      <section className="dashboard-page__hero workspace-hero">
        <div>
          <p className="dashboard-page__eyebrow">Registration management</p>
          <h1>Participant registrations and team activity</h1>
          <p>
            Track approvals, waitlists, team invitations, and the participant history for every event without leaving the dashboard.
          </p>
        </div>
        <div className="workspace-actions">
          <Button as={Link} variant="secondary" to={APP_ROUTES.events}>
            Events
          </Button>
          <Button as={Link} to={`${APP_ROUTES.events}/1/register`}>
            Register for an event
          </Button>
        </div>
      </section>

      <div className="card-grid card-grid--two">
        {dashboardLoading ? <LoadingSkeleton lines={4} /> : overviewCards.map(card => <SummaryCard key={card.title} {...card} />)}
      </div>

      <ErrorBanner message={dashboardError} />

      <div className="workspace-overview">
        <Card elevated className="workspace-summary">
          <div className="workspace-summary__grid">
            <div className="workspace-summary__item"><span>Upcoming</span><strong>{dashboard?.upcomingRegistrations?.length ?? 0}</strong></div>
            <div className="workspace-summary__item"><span>Pending approvals</span><strong>{dashboard?.pendingApprovals?.length ?? 0}</strong></div>
            <div className="workspace-summary__item"><span>Recent activity</span><strong>{dashboard?.recentRegistrations?.length ?? 0}</strong></div>
            <div className="workspace-summary__item"><span>Session</span><strong>{buildDisplayName(user)}</strong></div>
          </div>
        </Card>
        <Card elevated className="workspace-summary">
          <h2>Workspace notes</h2>
          <ul className="workspace-summary__list">
            <li>Every table uses backend pagination, search, and status filters.</li>
            <li>Bulk approval and rejection run through the same backend rules as single actions.</li>
            <li>Nothing falls back to fake analytics when the server is unavailable.</li>
          </ul>
        </Card>
      </div>

      <Tabs items={WORKSPACE_TABS} activeKey={location.pathname.includes('/notifications') ? 'notifications' : location.pathname.includes('/teams') ? 'teams' : location.pathname.includes('/registrations/me') ? 'mine' : location.pathname.includes('/registrations') ? 'registrations' : 'overview'} onChange={key => {
        const target = key === 'overview' ? APP_ROUTES.dashboard + '/registrations' : key === 'registrations' ? APP_ROUTES.dashboard + '/registrations' : key === 'mine' ? APP_ROUTES.dashboard + '/registrations' : key === 'teams' ? APP_ROUTES.dashboard + '/registrations' : APP_ROUTES.dashboard + '/registrations';
        window.history.pushState({}, '', target);
        window.dispatchEvent(new PopStateEvent('popstate'));
      }} />

      <div className="card-grid card-grid--two">
        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>All registrations</h2>
              <p>Search, filter, and review event participation records.</p>
            </div>
            <div className="workspace-actions">
              <Button variant="secondary" size="sm" onClick={resetFilters}>Reset</Button>
            </div>
          </div>
          <div className="workspace-filters">
            <SearchBar value={registrationSearch} onChange={setRegistrationSearch} placeholder="Search registrations" />
            <Select id="registration-status" value={registrationStatus} onChange={event => { setRegistrationStatus(event.target.value); setRegistrationPage(1); }}>
              {REGISTRATION_STATUS_OPTIONS.map(([value, label]) => <option key={label} value={value}>{label}</option>)}
            </Select>
          </div>
          <div className="workspace-actions">
            <Button variant="secondary" size="sm" onClick={() => handleBulkDecision('APPROVED')} disabled={!selectedCount}>Approve selected</Button>
            <Button variant="secondary" size="sm" onClick={() => handleBulkDecision('REJECTED')} disabled={!selectedCount}>Reject selected</Button>
          </div>
          {registrationsLoading ? (
            <LoadingSkeleton lines={6} />
          ) : registrationsError ? (
            <ErrorState title="Registration list unavailable" description={registrationsError} onRetry={() => window.location.reload()} />
          ) : allRows.length ? (
            <>
              <Table
                columns={[
                  {
                    key: 'select',
                    header: '',
                    render: row => <input type="checkbox" checked={selectedIds.includes(row.id)} onChange={event => toggleSelected(row.id, event.target.checked)} aria-label={`Select registration ${row.registrationNumber}`} />
                  },
                  { key: 'participantName', header: 'Participant', render: row => row.participantName },
                  { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                  { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                  { key: 'type', header: 'Type', render: row => friendlyStatus(row.registrationType) },
                  { key: 'submitted', header: 'Submitted', render: row => formatDateTime(row.registrationDate) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <div className="workspace-actions">
                        <Button variant="secondary" size="sm" onClick={() => handleDecision(row.id, 'APPROVED')}>Approve</Button>
                        <Button variant="secondary" size="sm" onClick={() => handleDecision(row.id, 'REJECTED')}>Reject</Button>
                      </div>
                    )
                  }
                ]}
                rows={allRows}
                emptyMessage="No registrations match the current filters."
              />
              <div className="workspace-footer">
                <span>Total: {registrationList.totalElements}</span>
                <Pagination page={registrationList.page} totalPages={registrationList.totalPages} onPageChange={setRegistrationPage} />
              </div>
            </>
          ) : (
            <EmptyState
              title="No registrations yet"
              description="Once participants register for events, their records will appear here with approvals, waitlist, and cancellation details."
              actionLabel="Reset filters"
              onAction={resetFilters}
            />
          )}
        </Card>

        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>My registrations</h2>
              <p>Review your own submissions and cancel them when the event allows it.</p>
            </div>
            <div className="workspace-actions">
              <Button variant="secondary" size="sm" onClick={resetMyFilters}>Reset</Button>
            </div>
          </div>
          <div className="workspace-filters">
            <SearchBar value={mySearch} onChange={setMySearch} placeholder="Search my registrations" />
            <Select id="my-status" value={myStatus} onChange={event => { setMyStatus(event.target.value); setMyPage(1); }}>
              {REGISTRATION_STATUS_OPTIONS.map(([value, label]) => <option key={label} value={value}>{label}</option>)}
            </Select>
          </div>
          {myLoading ? (
            <LoadingSkeleton lines={5} />
          ) : myError ? (
            <ErrorState title="Your registrations are unavailable" description={myError} onRetry={() => window.location.reload()} />
          ) : myRows.length ? (
            <>
              <Table
                columns={[
                  { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                  { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                  { key: 'submitted', header: 'Submitted', render: row => formatDateTime(row.registrationDate) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <div className="workspace-actions">
                        <Button variant="secondary" size="sm" onClick={() => handleCancelRegistration(row.id)}>Cancel</Button>
                      </div>
                    )
                  }
                ]}
                rows={myRows}
                emptyMessage="No personal registrations found."
              />
              <div className="workspace-footer">
                <span>Total: {myRegistrations.totalElements}</span>
                <Pagination page={myRegistrations.page} totalPages={myRegistrations.totalPages} onPageChange={setMyPage} />
              </div>
            </>
          ) : (
            <EmptyState
              title="No registrations yet"
              description="Register for an event to see your activity, approvals, and waitlist status here."
              actionLabel="Register for an event"
              onAction={() => window.location.assign(`${APP_ROUTES.events}/1/register`)}
            />
          )}
        </Card>
      </div>

      <div className="card-grid card-grid--two">
        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>Notifications</h2>
              <p>In-app messages for approvals, waitlist changes, and invitations.</p>
            </div>
          </div>
          {notificationsLoading ? (
            <LoadingSkeleton lines={5} />
          ) : notificationsError ? (
            <ErrorState title="Notifications unavailable" description={notificationsError} onRetry={() => window.location.reload()} />
          ) : notifications.length ? (
            <div className="workspace-progress">
              {notifications.slice(0, 6).map(item => (
                <div key={item.id} className={classNames('workspace-progress__item', item.readAt && 'workspace-progress__item--done')}>
                  <strong>{item.title}</strong>
                  <p>{item.message}</p>
                  <div className="workspace-actions">
                    <Badge>{friendlyStatus(item.notificationType)}</Badge>
                    {!item.readAt && <Button size="sm" variant="secondary" onClick={() => handleMarkRead(item.id)}>Mark read</Button>}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState title="No notifications yet" description="Approval changes and invitations will appear here as they happen." />
          )}
        </Card>

        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>Teams</h2>
              <p>Track the teams you manage or belong to.</p>
            </div>
            <div className="workspace-actions">
              <Button as={Link} variant="secondary" to={`${APP_ROUTES.events}/1/register`}>
                Open event form
              </Button>
            </div>
          </div>
          {teamsLoading ? (
            <LoadingSkeleton lines={4} />
          ) : teamsError ? (
            <ErrorState title="Teams unavailable" description={teamsError} onRetry={() => window.location.reload()} />
          ) : teams.length ? (
            <Table
              columns={[
                { key: 'teamName', header: 'Team', render: row => row.teamName },
                { key: 'leaderName', header: 'Leader', render: row => row.leaderName },
                { key: 'memberCount', header: 'Members', render: row => row.memberCount },
                { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                {
                  key: 'actions',
                  header: 'Actions',
                  render: row => (
                    <div className="workspace-actions">
                      <Button variant="secondary" size="sm" onClick={() => { setActiveTeamId(row.id); setInviteOpen(true); }}>
                        Invite
                      </Button>
                      <Button variant="secondary" size="sm" onClick={() => handleTeamOwnershipTransfer(row.id, row.leaderId)}>
                        Transfer
                      </Button>
                    </div>
                  )
                }
              ]}
              rows={teams}
            />
          ) : (
            <EmptyState title="No teams yet" description="Team registrations will appear here once your event allows them." />
          )}
        </Card>
      </div>

      <Modal
        open={createTeamOpen}
        title="Create team"
        description="Create a team workspace for the selected event."
        onClose={() => setCreateTeamOpen(false)}
        onSubmit={submitCreateTeam}
        submitLabel="Create team"
        loading={teamActionLoading}
      >
        <ValidationMessages messages={teamFormErrors} />
        <Input id="team-name" label="Team name" value={teamForm.teamName} onChange={event => setTeamForm(current => ({ ...current, teamName: event.target.value }))} />
        <Input id="team-code" label="Team code" value={teamForm.teamCode} onChange={event => setTeamForm(current => ({ ...current, teamCode: event.target.value }))} />
      </Modal>

      <Modal
        open={inviteOpen}
        title="Invite member"
        description="Send an invitation to a user from the same institution."
        onClose={() => setInviteOpen(false)}
        onSubmit={submitInvite}
        submitLabel="Send invitation"
        loading={teamActionLoading}
      >
        <ValidationMessages messages={inviteErrors} />
        <Input id="invite-user-id" label="User ID" value={inviteForm.invitedUserId} onChange={event => setInviteForm(current => ({ ...current, invitedUserId: event.target.value }))} />
        <Textarea id="invite-message" label="Message" value={inviteForm.message} onChange={event => setInviteForm(current => ({ ...current, message: event.target.value }))} rows={3} />
      </Modal>

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
