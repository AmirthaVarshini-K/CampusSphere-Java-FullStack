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
import Dialog from '../components/Dialog';
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
import ConflictPanel from '../components/registrations/ConflictPanel';

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

function RowActionMenu({ label = 'Actions', items = [] }) {
  return (
    <details className="workspace-menu">
      <summary className="workspace-menu__summary">{label}</summary>
      <div className="workspace-menu__panel" role="menu">
        {items.filter(item => !item.hidden).map(item => (
          <button
            key={item.label}
            type="button"
            className={classNames('workspace-menu__item', item.tone && `workspace-menu__item--${item.tone}`)}
            disabled={item.disabled}
            onClick={event => {
              event.currentTarget.closest('details')?.removeAttribute('open');
              item.onClick?.();
            }}
          >
            {item.label}
          </button>
        ))}
      </div>
    </details>
  );
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
  const [myTeams, setMyTeams] = useState([]);
  const [myTeamsLoading, setMyTeamsLoading] = useState(true);
  const [myTeamsError, setMyTeamsError] = useState('');
  const [myTeamInvitations, setMyTeamInvitations] = useState([]);
  const [myTeamInvitationsLoading, setMyTeamInvitationsLoading] = useState(true);
  const [myTeamInvitationsError, setMyTeamInvitationsError] = useState('');
  const [selectedTeamId, setSelectedTeamId] = useState(null);
  const [selectedTeamMembers, setSelectedTeamMembers] = useState([]);
  const [selectedTeamMembersLoading, setSelectedTeamMembersLoading] = useState(false);
  const [selectedTeamMembersError, setSelectedTeamMembersError] = useState('');
  const [selectedTeamInvitations, setSelectedTeamInvitations] = useState([]);
  const [selectedTeamInvitationsLoading, setSelectedTeamInvitationsLoading] = useState(false);
  const [selectedTeamInvitationsError, setSelectedTeamInvitationsError] = useState('');
  const [selectedTeamContext, setSelectedTeamContext] = useState(null);
  const [selectedTeamContextLoading, setSelectedTeamContextLoading] = useState(false);
  const [selectedTeamContextError, setSelectedTeamContextError] = useState('');

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
  const [registrationPreview, setRegistrationPreview] = useState(null);
  const [registrationPreviewLoading, setRegistrationPreviewLoading] = useState(false);
  const [registrationPreviewError, setRegistrationPreviewError] = useState('');

  const [createTeamOpen, setCreateTeamOpen] = useState(false);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [teamActionLoading, setTeamActionLoading] = useState(false);
  const [teamForm, setTeamForm] = useState({ teamName: '', teamCode: '' });
  const [inviteForm, setInviteForm] = useState({ invitedUserId: '', message: '' });
  const [editingTeamId, setEditingTeamId] = useState(null);
  const [activeTeamId, setActiveTeamId] = useState(null);
  const [teamFormErrors, setTeamFormErrors] = useState([]);
  const [inviteErrors, setInviteErrors] = useState([]);
  const [teamTransferOpen, setTeamTransferOpen] = useState(false);
  const [teamTransferForm, setTeamTransferForm] = useState({ teamId: null, newLeaderUserId: '' });
  const [teamTransferErrors, setTeamTransferErrors] = useState([]);
  const [teamConfirm, setTeamConfirm] = useState(null);
  const [teamConfirmLoading, setTeamConfirmLoading] = useState(false);

  const isAdmin = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR', 'FACULTY_COORDINATOR', 'ORGANISER'].includes(roleCode);
  const workspaceHasBackend = Boolean(dashboard || registrationList.content.length || myRegistrations.content.length || notifications.length || teams.length || myTeams.length || myTeamInvitations.length);

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

    async function loadMyTeams() {
      if (route.view !== 'workspace') {
        return;
      }
      setMyTeamsLoading(true);
      setMyTeamsError('');
      try {
        const response = await registrationApi.listMyTeams();
        if (!active) {
          return;
        }
        const data = unwrap(response) ?? [];
        setMyTeams(data);
        setSelectedTeamId(current => current ?? data[0]?.id ?? null);
      } catch (error) {
        if (!active) {
          return;
        }
        setMyTeamsError(getApiErrorMessage(error, 'Unable to load your teams.'));
      } finally {
        if (active) {
          setMyTeamsLoading(false);
        }
      }
    }

    loadMyTeams();
    return () => {
      active = false;
    };
  }, [route.view]);

  useEffect(() => {
    let active = true;

    async function loadMyTeamInvitations() {
      if (route.view !== 'workspace') {
        return;
      }
      setMyTeamInvitationsLoading(true);
      setMyTeamInvitationsError('');
      try {
        const response = await registrationApi.listMyTeamInvitations();
        if (!active) {
          return;
        }
        setMyTeamInvitations(unwrap(response) ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        setMyTeamInvitationsError(getApiErrorMessage(error, 'Unable to load your team invitations.'));
      } finally {
        if (active) {
          setMyTeamInvitationsLoading(false);
        }
      }
    }

    loadMyTeamInvitations();
    return () => {
      active = false;
    };
  }, [route.view]);

  useEffect(() => {
    let active = true;

    async function loadSelectedTeam() {
      if (route.view !== 'workspace' || !selectedTeamId) {
        setSelectedTeamMembers([]);
        setSelectedTeamInvitations([]);
        setSelectedTeamContext(null);
        return;
      }
      setSelectedTeamMembersLoading(true);
      setSelectedTeamInvitationsLoading(true);
      setSelectedTeamMembersError('');
      setSelectedTeamInvitationsError('');
      try {
        const [membersResponse, invitationsResponse] = await Promise.all([
          registrationApi.listTeamMembers(selectedTeamId),
          registrationApi.listTeamInvitations(selectedTeamId)
        ]);
        if (!active) {
          return;
        }
        setSelectedTeamMembers(unwrap(membersResponse) ?? []);
        setSelectedTeamInvitations(unwrap(invitationsResponse) ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        const message = getApiErrorMessage(error, 'Unable to load the selected team.');
        setSelectedTeamMembersError(message);
        setSelectedTeamInvitationsError(message);
      } finally {
        if (active) {
          setSelectedTeamMembersLoading(false);
          setSelectedTeamInvitationsLoading(false);
        }
      }
    }

    loadSelectedTeam();
    return () => {
      active = false;
    };
  }, [route.view, selectedTeamId]);

  useEffect(() => {
    let active = true;

    async function loadSelectedTeamContext() {
      const currentTeam = myTeams.find(team => team.id === selectedTeamId) ?? null;
      if (route.view !== 'workspace' || !currentTeam?.eventId) {
        setSelectedTeamContext(null);
        return;
      }
      setSelectedTeamContextLoading(true);
      setSelectedTeamContextError('');
      try {
        const response = await registrationApi.getEventRegistrationContext(currentTeam.eventId);
        if (!active) {
          return;
        }
        setSelectedTeamContext(unwrap(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setSelectedTeamContextError(getApiErrorMessage(error, 'Unable to load the team context.'));
      } finally {
        if (active) {
          setSelectedTeamContextLoading(false);
        }
      }
    }

    loadSelectedTeamContext();
    return () => {
      active = false;
    };
  }, [route.view, selectedTeamId, myTeams]);

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

  useEffect(() => {
    let active = true;

    async function loadRegistrationPreview() {
      if (route.view !== 'event-register' || !eventId || !eventForm) {
        setRegistrationPreview(null);
        setRegistrationPreviewError('');
        setRegistrationPreviewLoading(false);
        return;
      }

      setRegistrationPreviewLoading(true);
      setRegistrationPreviewError('');
      try {
        const response = await registrationApi.previewRegistration(eventId, {
          registrationType: formPayload.registrationType,
          teamName: formPayload.registrationType === 'TEAM' ? formPayload.teamName : null,
          teamCode: formPayload.registrationType === 'TEAM' ? formPayload.teamCode : null,
          remarks: formPayload.remarks || null
        });
        if (!active) {
          return;
        }
        setRegistrationPreview(unwrap(response));
      } catch (error) {
        if (!active) {
          return;
        }
        setRegistrationPreview(null);
        setRegistrationPreviewError(getApiErrorMessage(error, 'Unable to preview the registration right now.'));
      } finally {
        if (active) {
          setRegistrationPreviewLoading(false);
        }
      }
    }

    const timer = window.setTimeout(() => {
      loadRegistrationPreview();
    }, 250);

    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [
    eventForm,
    eventId,
    formPayload.registrationType,
    formPayload.teamCode,
    formPayload.teamName,
    formPayload.remarks,
    route.view
  ]);

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

  async function refreshEventForm() {
    if (!eventId) {
      return;
    }
    const response = await registrationApi.getEventRegistrationForm(eventId);
    const data = unwrap(response);
    setEventForm(data);
    setFormContext(data?.context ?? null);
    setTeams(data?.teams ?? []);
  }

  async function refreshRegistrationPreview() {
    if (!eventId || !eventForm) {
      return;
    }
    setRegistrationPreviewLoading(true);
    setRegistrationPreviewError('');
    try {
      const response = await registrationApi.previewRegistration(eventId, {
        registrationType: formPayload.registrationType,
        teamName: formPayload.registrationType === 'TEAM' ? formPayload.teamName : null,
        teamCode: formPayload.registrationType === 'TEAM' ? formPayload.teamCode : null,
        remarks: formPayload.remarks || null
      });
      setRegistrationPreview(unwrap(response));
    } catch (error) {
      setRegistrationPreview(null);
      setRegistrationPreviewError(getApiErrorMessage(error, 'Unable to preview the registration right now.'));
    } finally {
      setRegistrationPreviewLoading(false);
    }
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
      if (editingTeamId) {
        await registrationApi.updateTeam(editingTeamId, teamForm);
        pushToast('Team updated successfully.');
      } else {
        await registrationApi.createTeam(eventId, teamForm);
        pushToast('Team created successfully.');
      }
      setCreateTeamOpen(false);
      setTeamForm({ teamName: '', teamCode: '' });
      setEditingTeamId(null);
      await refreshTeams();
      await refreshEventForm();
    } catch (error) {
      const messages = extractValidationMessages(error);
      setTeamFormErrors(messages.length ? messages : [getApiErrorMessage(error, editingTeamId ? 'Unable to update the team.' : 'Unable to create the team.')]);
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

  async function handleMarkAllRead() {
    try {
      await registrationApi.markAllNotificationsRead();
      const response = await registrationApi.listNotifications();
      setNotifications(unwrap(response) ?? []);
      pushToast('All notifications marked as read.');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the notifications.'), 'error');
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
      await refreshMyTeams();
      await refreshSelectedTeamDetails();
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
      await refreshEventForm();
      await refreshMyTeams();
      await refreshMyTeamInvitations();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the invitation.'), 'error');
    }
  }

  async function handleCancelInvitation(id) {
    try {
      await registrationApi.cancelInvitation(id);
      pushToast('Invitation cancelled.');
      await refreshEventForm();
      await refreshMyTeamInvitations();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to cancel the invitation.'), 'error');
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

  function openTeamEditor(team) {
    setEditingTeamId(team.id);
    setTeamForm({ teamName: team.teamName ?? '', teamCode: team.teamCode ?? '' });
    setCreateTeamOpen(true);
  }

  async function handleLeaveTeam(teamId) {
    try {
      await registrationApi.leaveTeam(teamId);
      pushToast('You left the team.');
      await refreshMyTeams();
      await refreshMyTeamInvitations();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to leave the team.'), 'error');
    }
  }

  async function handleDeleteTeam(teamId) {
    try {
      await registrationApi.deleteTeam(teamId);
      pushToast('Team deleted successfully.');
      if (selectedTeamId === teamId) {
        setSelectedTeamId(null);
      }
      await refreshMyTeams();
      await refreshMyTeamInvitations();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to delete the team.'), 'error');
    }
  }

  async function refreshMyTeams() {
    if (route.view !== 'workspace') {
      return;
    }
    try {
      const response = await registrationApi.listMyTeams();
      const data = unwrap(response) ?? [];
      setMyTeams(data);
      setSelectedTeamId(current => {
        if (current && data.some(team => team.id === current)) {
          return current;
        }
        return data[0]?.id ?? null;
      });
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to refresh your teams.'), 'error');
    }
  }

  async function refreshMyTeamInvitations() {
    if (route.view !== 'workspace') {
      return;
    }
    try {
      const response = await registrationApi.listMyTeamInvitations();
      setMyTeamInvitations(unwrap(response) ?? []);
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to refresh your invitations.'), 'error');
    }
  }

  async function refreshSelectedTeamDetails(teamId = selectedTeamId) {
    if (route.view !== 'workspace' || !teamId) {
      setSelectedTeamMembers([]);
      setSelectedTeamInvitations([]);
      return;
    }
    setSelectedTeamMembersLoading(true);
    setSelectedTeamInvitationsLoading(true);
    setSelectedTeamMembersError('');
    setSelectedTeamInvitationsError('');
    try {
      const [membersResponse, invitationsResponse] = await Promise.all([
        registrationApi.listTeamMembers(teamId),
        registrationApi.listTeamInvitations(teamId)
      ]);
      setSelectedTeamMembers(unwrap(membersResponse) ?? []);
      setSelectedTeamInvitations(unwrap(invitationsResponse) ?? []);
    } catch (error) {
      const message = getApiErrorMessage(error, 'Unable to refresh team details.');
      setSelectedTeamMembersError(message);
      setSelectedTeamInvitationsError(message);
    } finally {
      setSelectedTeamMembersLoading(false);
      setSelectedTeamInvitationsLoading(false);
    }
  }

  function requestTeamDelete(team) {
    setTeamConfirm({
      type: 'delete',
      teamId: team.id,
      title: `Delete ${team.teamName}?`,
      description: 'This will archive the team and remove its active roster from the workspace.',
      confirmLabel: 'Delete team'
    });
  }

  function requestTeamLeave(team) {
    setTeamConfirm({
      type: 'leave',
      teamId: team.id,
      title: `Leave ${team.teamName}?`,
      description: 'If you are the leader, you must transfer ownership before leaving unless you are the only remaining member.',
      confirmLabel: 'Leave team'
    });
  }

  function requestTeamMemberRemoval(team, member) {
    setTeamConfirm({
      type: 'remove-member',
      teamId: team.id,
      memberId: member.id,
      title: `Remove ${member.userName} from ${team.teamName}?`,
      description: 'The member will lose access to the team and its registration flow.',
      confirmLabel: 'Remove member'
    });
  }

  function requestInvitationCancel(invitation) {
    setTeamConfirm({
      type: 'cancel-invitation',
      invitationId: invitation.id,
      title: `Cancel invitation for ${invitation.invitedUserName}?`,
      description: 'The invited participant will no longer be able to accept this invitation.',
      confirmLabel: 'Cancel invitation'
    });
  }

  function openTeamTransfer(team) {
    setTeamTransferErrors([]);
    setTeamTransferForm({ teamId: team.id, newLeaderUserId: '' });
    setTeamTransferOpen(true);
  }

  async function submitTeamTransfer(event) {
    event.preventDefault();
    setTeamActionLoading(true);
    setTeamTransferErrors([]);
    try {
      await registrationApi.transferTeamOwnership(teamTransferForm.teamId, {
        newLeaderUserId: Number(teamTransferForm.newLeaderUserId)
      });
      pushToast('Team ownership transferred successfully.');
      setTeamTransferOpen(false);
      await refreshMyTeams();
      await refreshSelectedTeamDetails(teamTransferForm.teamId);
    } catch (error) {
      const messages = extractValidationMessages(error);
      setTeamTransferErrors(messages.length ? messages : [getApiErrorMessage(error, 'Unable to transfer ownership.')]);
    } finally {
      setTeamActionLoading(false);
    }
  }

  async function confirmTeamAction(event) {
    event.preventDefault();
    if (!teamConfirm) {
      return;
    }
    setTeamConfirmLoading(true);
    try {
      switch (teamConfirm.type) {
        case 'delete':
          await handleDeleteTeam(teamConfirm.teamId);
          break;
        case 'leave':
          await handleLeaveTeam(teamConfirm.teamId);
          break;
        case 'remove-member':
          await registrationApi.removeTeamMember(teamConfirm.teamId, teamConfirm.memberId);
          pushToast('Team member removed successfully.');
          await refreshSelectedTeamDetails(teamConfirm.teamId);
          await refreshMyTeams();
          break;
        case 'cancel-invitation':
          await registrationApi.cancelInvitation(teamConfirm.invitationId);
          pushToast('Invitation cancelled successfully.');
          await refreshSelectedTeamDetails(selectedTeamId);
          await refreshMyTeamInvitations();
          break;
        default:
          break;
      }
      setTeamConfirm(null);
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to complete the team action.'), 'error');
    } finally {
      setTeamConfirmLoading(false);
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
  const selectedTeam = myTeams.find(team => team.id === selectedTeamId) ?? null;
  const selectedTeamRegistrations = myRows.filter(row => row.teamId === selectedTeamId);
  const selectedTeamRegistration = selectedTeamRegistrations.find(row => row.participantId === user?.id) ?? selectedTeamRegistrations[0] ?? null;

  const teamRows = teams.map(team => ({
    ...team,
    memberCount: team.memberCount ?? 0
  }));
  const myTeamRows = myTeams.map(team => ({
    ...team,
    memberCount: team.memberCount ?? 0
  }));
  const unreadNotificationCount = notifications.filter(item => !item.readAt).length;

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
                <Button variant="secondary" size="sm" onClick={refreshRegistrationPreview} disabled={registrationPreviewLoading}>
                  {registrationPreviewLoading ? 'Checking...' : 'Refresh preview'}
                </Button>
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
            {registrationPreviewError ? <ErrorBanner message={registrationPreviewError} /> : null}
            <ConflictPanel preview={registrationPreview} loading={registrationPreviewLoading} />
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
              <Button variant="secondary" onClick={() => { setEditingTeamId(null); setCreateTeamOpen(true); }}>
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
                      <Button variant="secondary" size="sm" onClick={() => openTeamEditor(row)}>
                        Edit
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
              onAction={() => { setEditingTeamId(null); setCreateTeamOpen(true); }}
            />
          )}
        </Card>

        <Card elevated className="workspace-table-card">
          <div className="workspace-table-card__meta">
            <div>
              <h2>Pending invitations</h2>
              <p>Track invitations for the current event and respond without leaving the workspace.</p>
            </div>
          </div>
          {eventForm?.invitations?.length ? (
            <Table
              columns={[
                { key: 'teamName', header: 'Team', render: row => row.teamName },
                { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                { key: 'invitedAt', header: 'Invited', render: row => formatDateTime(row.invitedAt) },
                {
                  key: 'actions',
                  header: 'Actions',
                  render: row => (
                    <div className="workspace-actions">
                      <Button variant="secondary" size="sm" onClick={() => handleInvitationResponse(row.id, true)} disabled={row.status !== 'PENDING'}>
                        Accept
                      </Button>
                      <Button variant="secondary" size="sm" onClick={() => handleInvitationResponse(row.id, false)} disabled={row.status !== 'PENDING'}>
                        Reject
                      </Button>
                      <Button variant="secondary" size="sm" onClick={() => handleCancelInvitation(row.id)} disabled={row.status !== 'PENDING'}>
                        Cancel
                      </Button>
                    </div>
                  )
                }
              ]}
              rows={eventForm.invitations}
            />
          ) : (
            <EmptyState title="No invitations" description="Team invitations for this event will appear here." />
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
          title={editingTeamId ? 'Edit team' : 'Create team'}
          description={editingTeamId ? 'Update the team details for this event.' : 'Create a team for this event before inviting other participants.'}
          onClose={() => { setCreateTeamOpen(false); setEditingTeamId(null); }}
          onSubmit={submitCreateTeam}
          submitLabel={editingTeamId ? 'Save team' : 'Create team'}
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
          <Button as={Link} to={APP_ROUTES.events}>
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
        const target = key === 'notifications'
          ? APP_ROUTES.dashboard + '/notifications'
          : APP_ROUTES.dashboard + '/registrations';
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
              onAction={() => window.location.assign(APP_ROUTES.events)}
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
            <div className="workspace-actions">
              <Badge tone={unreadNotificationCount ? 'warning' : 'neutral'}>{unreadNotificationCount} unread</Badge>
              <Button variant="secondary" size="sm" onClick={handleMarkAllRead} disabled={!unreadNotificationCount}>
                Mark all read
              </Button>
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

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>My teams</h2>
                <p>Choose a team to review its members, invitations, and registration state.</p>
              </div>
              <div className="workspace-actions">
                <Button as={Link} variant="secondary" to={APP_ROUTES.events}>
                  Open event form
                </Button>
              </div>
            </div>
            {myTeamsLoading ? (
              <LoadingSkeleton lines={4} />
            ) : myTeamsError ? (
              <ErrorState title="Teams unavailable" description={myTeamsError} onRetry={() => window.location.reload()} />
            ) : myTeamRows.length ? (
              <Table
                columns={[
                  {
                    key: 'teamName',
                    header: 'Team',
                    render: row => (
                      <div className="workspace-table-cell">
                        <strong>{row.teamName}</strong>
                        {selectedTeamId === row.id && <Badge tone="success">Selected</Badge>}
                      </div>
                    )
                  },
                  { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                  { key: 'memberCount', header: 'Members', render: row => row.memberCount },
                  { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <RowActionMenu
                        items={[
                          { label: 'View team', onClick: () => setSelectedTeamId(row.id) },
                          { label: 'Invite member', onClick: () => { setSelectedTeamId(row.id); setActiveTeamId(row.id); setInviteOpen(true); } },
                          { label: 'Edit name', onClick: () => openTeamEditor(row) },
                          { label: 'Transfer ownership', onClick: () => openTeamTransfer(row) },
                          { label: 'Leave team', onClick: () => requestTeamLeave(row) },
                          { label: 'Delete team', tone: 'danger', onClick: () => requestTeamDelete(row) }
                        ]}
                      />
                    )
                  }
                ]}
                rows={myTeamRows}
                emptyMessage="No teams are linked to your account yet."
              />
            ) : (
              <EmptyState
                title="No teams yet"
                description="Create a team from an event registration page to manage members and invitations here."
                actionLabel="Open event form"
                onAction={() => window.location.assign(APP_ROUTES.events)}
              />
            )}
          </Card>

          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Team details</h2>
                <p>Review the selected team, its event context, and its registration state.</p>
              </div>
            </div>
            {!selectedTeam ? (
              <EmptyState
                title="No team selected"
                description="Select a team from the list to see the member roster and registration details."
                actionLabel="Select first team"
                onAction={() => setSelectedTeamId(myTeamRows[0]?.id ?? null)}
              />
            ) : (
              <>
                <div className="workspace-summary__grid">
                  <div className="workspace-summary__item"><span>Team</span><strong>{selectedTeam.teamName}</strong></div>
                  <div className="workspace-summary__item"><span>Leader</span><strong>{selectedTeam.leaderName}</strong></div>
                  <div className="workspace-summary__item"><span>Members</span><strong>{selectedTeam.memberCount ?? 0}</strong></div>
                  <div className="workspace-summary__item"><span>Status</span><strong><StatusBadge status={selectedTeam.status} /></strong></div>
                </div>
                {selectedTeamContextLoading ? (
                  <LoadingSkeleton lines={3} />
                ) : selectedTeamContextError ? (
                  <ErrorBanner message={selectedTeamContextError} />
                ) : selectedTeamContext ? (
                  <div className="workspace-summary__grid">
                    <div className="workspace-summary__item"><span>Event</span><strong>{selectedTeamContext.eventTitle}</strong></div>
                    <div className="workspace-summary__item"><span>Registration</span><strong>{selectedTeamContext.registrationOpen ? 'Open' : 'Closed'}</strong></div>
                    <div className="workspace-summary__item"><span>Seats remaining</span><strong>{selectedTeamContext.seatsRemaining ?? 'Open'}</strong></div>
                    <div className="workspace-summary__item"><span>Max team size</span><strong>{selectedTeamContext.maximumTeamSize ?? '-'}</strong></div>
                  </div>
                ) : null}
                <div className="workspace-footer">
                  <span>Registration status</span>
                  <p>
                    {selectedTeamRegistration
                      ? `${friendlyStatus(selectedTeamRegistration.status)}${selectedTeamRegistration.registrationNumber ? ` • ${selectedTeamRegistration.registrationNumber}` : ''}`
                      : 'No registration record is linked to this team yet.'}
                  </p>
                </div>
                <div className="workspace-actions">
                  <Button variant="secondary" size="sm" onClick={() => { setActiveTeamId(selectedTeam.id); setInviteOpen(true); }}>
                    Invite member
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => openTeamEditor(selectedTeam)}>
                    Edit
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => openTeamTransfer(selectedTeam)}>
                    Transfer
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => requestTeamLeave(selectedTeam)}>
                    Leave
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => requestTeamDelete(selectedTeam)}>
                    Delete
                  </Button>
                </div>
              </>
            )}
          </Card>
        </div>

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Team members</h2>
                <p>Review the roster for the selected team and manage individual members.</p>
              </div>
            </div>
            {selectedTeamMembersLoading ? (
              <LoadingSkeleton lines={4} />
            ) : selectedTeamMembersError ? (
              <ErrorState title="Team members unavailable" description={selectedTeamMembersError} onRetry={() => refreshSelectedTeamDetails()} />
            ) : selectedTeamMembers.length ? (
              <Table
                columns={[
                  { key: 'userName', header: 'Member', render: row => <strong>{row.userName}</strong> },
                  { key: 'role', header: 'Role', render: row => <Badge tone={row.role === 'LEADER' ? 'success' : 'neutral'}>{friendlyStatus(row.role)}</Badge> },
                  { key: 'joinedAt', header: 'Joined', render: row => formatDateTime(row.joinedAt) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <RowActionMenu
                        items={[
                          { label: 'Remove member', hidden: row.role === 'LEADER', tone: 'danger', onClick: () => requestTeamMemberRemoval(selectedTeam, row) },
                          { label: 'Transfer ownership', hidden: row.role === 'LEADER', onClick: () => { setTeamTransferForm({ teamId: selectedTeam.id, newLeaderUserId: String(row.userId) }); setTeamTransferOpen(true); } }
                        ]}
                      />
                    )
                  }
                ]}
                rows={selectedTeamMembers}
                emptyMessage="No members are listed for the selected team."
              />
            ) : (
              <EmptyState title="No members yet" description="The team roster will appear once members are invited or registered." />
            )}
          </Card>

          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Team invitations</h2>
                <p>Track invitations sent from the selected team.</p>
              </div>
            </div>
            {selectedTeamInvitationsLoading ? (
              <LoadingSkeleton lines={4} />
            ) : selectedTeamInvitationsError ? (
              <ErrorState title="Team invitations unavailable" description={selectedTeamInvitationsError} onRetry={() => refreshSelectedTeamDetails()} />
            ) : selectedTeamInvitations.length ? (
              <Table
                columns={[
                  { key: 'invitedUserName', header: 'Invited participant', render: row => row.invitedUserName },
                  { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                  { key: 'invitedAt', header: 'Invited', render: row => formatDateTime(row.invitedAt) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <RowActionMenu
                        items={[
                          { label: 'Cancel invitation', hidden: row.status !== 'PENDING', tone: 'danger', onClick: () => requestInvitationCancel(row) }
                        ]}
                      />
                    )
                  }
                ]}
                rows={selectedTeamInvitations}
                emptyMessage="No invitations have been sent from this team."
              />
            ) : (
              <EmptyState title="No team invitations" description="Send invitations from the selected team when you want to expand the roster." />
            )}
          </Card>
        </div>

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Pending invitations</h2>
                <p>Respond to invitations you received from other teams.</p>
              </div>
            </div>
            {myTeamInvitationsLoading ? (
              <LoadingSkeleton lines={4} />
            ) : myTeamInvitationsError ? (
              <ErrorState title="Pending invitations unavailable" description={myTeamInvitationsError} onRetry={() => window.location.reload()} />
            ) : myTeamInvitations.length ? (
              <Table
                columns={[
                  { key: 'teamName', header: 'Team', render: row => row.teamName },
                  { key: 'status', header: 'Status', render: row => <StatusBadge status={row.status} /> },
                  { key: 'invitedAt', header: 'Invited', render: row => formatDateTime(row.invitedAt) },
                  {
                    key: 'actions',
                    header: 'Actions',
                    render: row => (
                      <RowActionMenu
                        items={[
                          { label: 'Accept', hidden: row.status !== 'PENDING', onClick: () => handleInvitationResponse(row.id, true) },
                          { label: 'Reject', hidden: row.status !== 'PENDING', tone: 'danger', onClick: () => handleInvitationResponse(row.id, false) }
                        ]}
                      />
                    )
                  }
                ]}
                rows={myTeamInvitations}
                emptyMessage="You do not have any team invitations waiting for a response."
              />
            ) : (
              <EmptyState title="No pending invitations" description="Team invitations sent to your account will appear here." />
            )}
          </Card>

          <Card elevated className="workspace-table-card">
            <div className="workspace-table-card__meta">
              <div>
                <h2>Team registration status</h2>
                <p>See the linked registration record for the selected team.</p>
              </div>
            </div>
            {selectedTeamRegistration ? (
              <>
                <div className="workspace-summary__grid">
                  <div className="workspace-summary__item"><span>Registration #</span><strong>{selectedTeamRegistration.registrationNumber ?? '-'}</strong></div>
                  <div className="workspace-summary__item"><span>Status</span><strong><StatusBadge status={selectedTeamRegistration.status} /></strong></div>
                  <div className="workspace-summary__item"><span>Submitted</span><strong>{formatDateTime(selectedTeamRegistration.registrationDate)}</strong></div>
                  <div className="workspace-summary__item"><span>Type</span><strong>{friendlyStatus(selectedTeamRegistration.registrationType)}</strong></div>
                </div>
                <div className="workspace-footer">
                  <span>Next step</span>
                  <p>
                    {selectedTeamRegistration.status === 'APPROVED'
                      ? 'The team registration is confirmed.'
                      : selectedTeamRegistration.status === 'PENDING'
                        ? 'The team is waiting for review.'
                        : selectedTeamRegistration.status === 'WAITLISTED'
                          ? 'The team is on the waitlist until a seat opens.'
                          : 'This registration is no longer active.'}
                  </p>
                </div>
                {selectedTeamRegistration.participantId === user?.id && selectedTeamRegistration.status !== 'CANCELLED' && (
                  <div className="workspace-actions">
                    <Button variant="secondary" size="sm" onClick={() => handleCancelRegistration(selectedTeamRegistration.id)}>
                      Withdraw registration
                    </Button>
                  </div>
                )}
              </>
            ) : (
              <EmptyState title="No team registration yet" description="Create or submit a team registration before status details appear here." />
            )}
          </Card>
        </div>

      </div>

        <Modal
        open={createTeamOpen}
        title={editingTeamId ? 'Edit team' : 'Create team'}
        description={editingTeamId ? 'Update the team details for this event.' : 'Create a team workspace for the selected event.'}
        onClose={() => { setCreateTeamOpen(false); setEditingTeamId(null); }}
        onSubmit={submitCreateTeam}
        submitLabel={editingTeamId ? 'Save team' : 'Create team'}
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

      <Modal
        open={teamTransferOpen}
        title="Transfer ownership"
        description="Choose another team member to become the new leader."
        onClose={() => { setTeamTransferOpen(false); setTeamTransferErrors([]); }}
        onSubmit={submitTeamTransfer}
        submitLabel="Transfer ownership"
        loading={teamActionLoading}
      >
        <ValidationMessages messages={teamTransferErrors} />
        <Select
          id="team-transfer-user"
          label="New leader"
          value={teamTransferForm.newLeaderUserId}
          onChange={event => setTeamTransferForm(current => ({ ...current, newLeaderUserId: event.target.value }))}
        >
          <option value="">Select a member</option>
          {selectedTeamMembers
            .filter(member => member.role !== 'LEADER')
            .map(member => (
              <option key={member.userId} value={member.userId}>{member.userName}</option>
            ))}
        </Select>
      </Modal>

      <Dialog
        open={Boolean(teamConfirm)}
        title={teamConfirm?.title ?? 'Confirm action'}
        description={teamConfirm?.description ?? ''}
        confirmLabel={teamConfirm?.confirmLabel ?? 'Continue'}
        onClose={() => setTeamConfirm(null)}
        onConfirm={confirmTeamAction}
      />

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
