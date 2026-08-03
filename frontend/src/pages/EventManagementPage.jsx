import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
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
import { Toast } from '../components/Toast';
import ValidationMessages from '../components/ValidationMessages';
import { APP_ROUTES } from '../constants/routes';
import { eventApi } from '../services/eventApi';
import { masterDataApi } from '../services/masterDataApi';
import { userApi } from '../services/userApi';
import { useAuth } from '../context/AuthContext';
import useToastQueue from '../hooks/useToastQueue';
import { extractValidationMessages, getApiErrorMessage, isNetworkError } from '../utils/apiErrors';
import { buildDisplayName } from '../utils/auth';
import { classNames } from '../utils/classNames';

const BASE_PATH = `${APP_ROUTES.dashboard}/events`;
const WORKSPACE_TAB_ITEMS = [
  { key: 'overview', label: 'Overview' },
  { key: 'events', label: 'Events' },
  { key: 'categories', label: 'Categories' },
  { key: 'types', label: 'Event types' },
  { key: 'venues', label: 'Venues' }
];

const DETAIL_TAB_ITEMS = [
  { key: 'overview', label: 'Overview' },
  { key: 'sessions', label: 'Sessions' },
  { key: 'coordinators', label: 'Coordinators' },
  { key: 'eligibility', label: 'Eligibility' },
  { key: 'registration', label: 'Registration' },
  { key: 'status', label: 'Status' }
];

const STATUS_OPTIONS = [
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

const MODE_OPTIONS = [
  ['OFFLINE', 'Offline'],
  ['ONLINE', 'Online'],
  ['HYBRID', 'Hybrid']
];

const VISIBILITY_OPTIONS = [
  ['PRIVATE', 'Private'],
  ['INSTITUTION_ONLY', 'Institution only'],
  ['PUBLIC', 'Public']
];

const VENUE_TYPE_OPTIONS = [
  ['AUDITORIUM', 'Auditorium'],
  ['SEMINAR_HALL', 'Seminar hall'],
  ['CLASSROOM', 'Classroom'],
  ['LAB', 'Lab'],
  ['CONFERENCE_ROOM', 'Conference room'],
  ['OPEN_AIR', 'Open air'],
  ['ONLINE', 'Online'],
  ['OTHER', 'Other']
];

const COORDINATOR_ROLE_OPTIONS = [
  ['PRIMARY', 'Primary'],
  ['COORDINATOR', 'Coordinator'],
  ['TECHNICAL_SUPPORT', 'Technical support'],
  ['HOSPITALITY', 'Hospitality'],
  ['VOLUNTEER_LEAD', 'Volunteer lead'],
  ['FACULTY_SUPERVISOR', 'Faculty supervisor']
];

const PARTICIPANT_TYPE_OPTIONS = [
  ['STUDENT', 'Student'],
  ['FACULTY', 'Faculty'],
  ['STAFF', 'Staff'],
  ['ALUMNI', 'Alumni'],
  ['EXTERNAL', 'External'],
  ['ALL', 'All participants']
];

const ELIGIBILITY_RULE_TYPE_OPTIONS = [
  ['INCLUDE', 'Include'],
  ['EXCLUDE', 'Exclude'],
  ['MINIMUM_YEAR', 'Minimum year'],
  ['MAXIMUM_YEAR', 'Maximum year']
];

const PROGRAMME_LEVEL_OPTIONS = [
  ['UNDERGRADUATE', 'Undergraduate'],
  ['POSTGRADUATE', 'Postgraduate'],
  ['DIPLOMA', 'Diploma'],
  ['DOCTORAL', 'Doctoral'],
  ['CERTIFICATE', 'Certificate'],
  ['OTHER', 'Other']
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

function blankToNull(value) {
  return value === '' ? null : value;
}

function asNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return null;
  }
  return Number(value);
}

function parsePath(pathname) {
  const segments = pathname.replace(/^\/+|\/+$/g, '').split('/');
  const index = segments.indexOf('events');
  const sub = index >= 0 ? segments.slice(index + 1) : [];

  if (sub.length === 0) {
    return { view: 'overview' };
  }

  if (sub[0] === 'new') {
    return { view: 'event-form', mode: 'create' };
  }

  if (sub[0] === 'categories' || sub[0] === 'types' || sub[0] === 'venues') {
    return { view: 'collection', kind: sub[0] };
  }

  if (sub[0] === 'events' && sub[1] === 'new') {
    return { view: 'event-form', mode: 'create' };
  }

  if (sub[0] === 'events' && sub[1] && sub[2] === 'edit') {
    return { view: 'event-form', mode: 'edit', eventId: sub[1] };
  }

  if (sub[0] === 'events' && sub[1]) {
    return { view: 'event-detail', eventId: sub[1] };
  }

  if (/^\d+$/.test(sub[0] ?? '') && sub[1] === 'edit') {
    return { view: 'event-form', mode: 'edit', eventId: sub[0] };
  }

  if (!Number.isNaN(Number(sub[0]))) {
    return { view: 'event-detail', eventId: sub[0] };
  }

  return { view: 'overview' };
}

function toOptions(rows = [], labelKeys = ['name']) {
  return rows.map(row => ({
    value: String(row.id),
    label: labelKeys.map(key => row[key]).find(Boolean) ?? `#${row.id}`
  }));
}

function makeStatusLabel(value) {
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

function getStatusTone(status) {
  switch (status) {
    case 'ACTIVE':
    case 'PUBLISHED':
    case 'REGISTRATION_OPEN':
    case 'ONGOING':
      return 'success';
    case 'PENDING_APPROVAL':
    case 'REGISTRATION_CLOSED':
      return 'warning';
    case 'CANCELLED':
    case 'ARCHIVED':
    case 'INACTIVE':
      return 'neutral';
    default:
      return 'neutral';
  }
}

function CollectionModal({ open, title, description, fields, values, errors, onChange, onSubmit, onClose, loading, submitLabel = 'Save' }) {
  return (
    <Modal
      open={open}
      title={title}
      description={description}
      onClose={onClose}
      onSubmit={onSubmit}
      submitLabel={submitLabel}
      loading={loading}
      size="lg"
    >
      <div className="grid grid--2">
        {fields.map(field => {
          if (field.type === 'select') {
            return (
              <Select
                key={field.name}
                id={field.name}
                label={field.label}
                value={values[field.name] ?? ''}
                onChange={event => onChange(field.name, event.target.value)}
              >
                <option value="">{field.placeholder ?? `Select ${field.label.toLowerCase()}`}</option>
                {field.options.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </Select>
            );
          }

          if (field.type === 'textarea') {
            return (
              <Textarea
                key={field.name}
                id={field.name}
                label={field.label}
                rows={field.rows ?? 4}
                value={values[field.name] ?? ''}
                onChange={event => onChange(field.name, event.target.value)}
              />
            );
          }

          return (
            <Input
              key={field.name}
              id={field.name}
              label={field.label}
              type={field.type ?? 'text'}
              min={field.min}
              step={field.step}
              placeholder={field.placeholder}
              value={values[field.name] ?? ''}
              onChange={event => onChange(field.name, event.target.value)}
            />
          );
        })}
      </div>
      <ValidationMessages messages={errors} />
    </Modal>
  );
}

function DetailHeader({ event, onBack, onEdit, onStatusChange, nextStatuses }) {
  if (!event) {
    return null;
  }

  return (
    <Card elevated className="workspace-hero">
      <div className="workspace-summary">
        <div className="section-panel__title">
          <div>
            <h1 style={{ margin: 0 }}>{event.title}</h1>
            <p className="dashboard-page__muted" style={{ marginTop: '0.35rem' }}>
              {event.eventCode} · {makeStatusLabel(event.status)} · {event.mode}
            </p>
          </div>
          <div className="workspace-actions">
            <Button variant="secondary" onClick={onBack}>Back</Button>
            <Button variant="secondary" onClick={onEdit}>Edit</Button>
            <Button as={Link} variant="secondary" to={`${APP_ROUTES.dashboard}/events/${event.id}/register`}>
              Register
            </Button>
            {nextStatuses.map(status => (
              <Button key={status} onClick={() => onStatusChange(status)}>
                {makeStatusLabel(status)}
              </Button>
            ))}
          </div>
        </div>
        <div className="workspace-summary__grid">
          <div className="workspace-summary__item"><span>Category</span><strong>{event.eventCategoryName ?? '-'}</strong></div>
          <div className="workspace-summary__item"><span>Type</span><strong>{event.eventTypeName ?? '-'}</strong></div>
          <div className="workspace-summary__item"><span>Department</span><strong>{event.organizingDepartmentName ?? '-'}</strong></div>
          <div className="workspace-summary__item"><span>Venue</span><strong>{event.venueName ?? (event.onlineMeetingUrl ? 'Online / Hybrid' : '-')}</strong></div>
        </div>
      </div>
    </Card>
  );
}

function EventFormPage({ mode, eventId, onBack, onSaved, catalogs, scopeInstitutionId, pushToast }) {
  const [loading, setLoading] = useState(Boolean(eventId));
  const [saving, setSaving] = useState(false);
  const [serverError, setServerError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [form, setForm] = useState({
    institutionId: scopeInstitutionId ?? '',
    title: '',
    eventCode: '',
    slug: '',
    shortDescription: '',
    fullDescription: '',
    eventCategoryId: '',
    eventTypeId: '',
    organizingDepartmentId: '',
    academicYearId: '',
    venueId: '',
    mode: 'OFFLINE',
    visibility: 'INSTITUTION_ONLY',
    startDateTime: '',
    endDateTime: '',
    registrationStartDateTime: '',
    registrationEndDateTime: '',
    cancellationDeadline: '',
    onlineMeetingUrl: '',
    maximumParticipants: '',
    minimumParticipants: '',
    registrationFee: '',
    currency: 'INR',
    bannerImageUrl: '',
    contactEmail: '',
    contactPhone: ''
  });

  useEffect(() => {
    if (!eventId) {
      return;
    }

    let active = true;
    async function load() {
      try {
        setLoading(true);
        const response = await eventApi.getEvent(eventId);
        const data = unwrap(response);
        if (!active || !data) {
          return;
        }
        setForm({
          institutionId: data.institutionId ?? scopeInstitutionId ?? '',
          title: data.title ?? '',
          eventCode: data.eventCode ?? '',
          slug: data.slug ?? '',
          shortDescription: data.shortDescription ?? '',
          fullDescription: data.fullDescription ?? '',
          eventCategoryId: data.eventCategoryId ? String(data.eventCategoryId) : '',
          eventTypeId: data.eventTypeId ? String(data.eventTypeId) : '',
          organizingDepartmentId: data.organizingDepartmentId ? String(data.organizingDepartmentId) : '',
          academicYearId: data.academicYearId ? String(data.academicYearId) : '',
          venueId: data.venueId ? String(data.venueId) : '',
          mode: data.mode ?? 'OFFLINE',
          visibility: data.visibility ?? 'INSTITUTION_ONLY',
          startDateTime: data.startDateTime ? String(data.startDateTime).slice(0, 16) : '',
          endDateTime: data.endDateTime ? String(data.endDateTime).slice(0, 16) : '',
          registrationStartDateTime: data.registrationStartDateTime ? String(data.registrationStartDateTime).slice(0, 16) : '',
          registrationEndDateTime: data.registrationEndDateTime ? String(data.registrationEndDateTime).slice(0, 16) : '',
          cancellationDeadline: data.cancellationDeadline ? String(data.cancellationDeadline).slice(0, 16) : '',
          onlineMeetingUrl: data.onlineMeetingUrl ?? '',
          maximumParticipants: data.maximumParticipants ?? '',
          minimumParticipants: data.minimumParticipants ?? '',
          registrationFee: data.registrationFee ?? '',
          currency: data.currency ?? 'INR',
          bannerImageUrl: data.bannerImageUrl ?? '',
          contactEmail: data.contactEmail ?? '',
          contactPhone: data.contactPhone ?? ''
        });
      } catch (error) {
        setServerError(getApiErrorMessage(error, 'Unable to load the event details.'));
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [eventId, scopeInstitutionId]);

  function updateField(name, value) {
    setForm(current => ({ ...current, [name]: value }));
    setFieldErrors(current => {
      if (!current[name]) {
        return current;
      }
      const next = { ...current };
      delete next[name];
      return next;
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const payload = {
      ...form,
      institutionId: form.institutionId ? Number(form.institutionId) : null,
      eventCategoryId: form.eventCategoryId ? Number(form.eventCategoryId) : null,
      eventTypeId: form.eventTypeId ? Number(form.eventTypeId) : null,
      organizingDepartmentId: form.organizingDepartmentId ? Number(form.organizingDepartmentId) : null,
      academicYearId: form.academicYearId ? Number(form.academicYearId) : null,
      venueId: form.venueId ? Number(form.venueId) : null,
      startDateTime: form.startDateTime ? `${form.startDateTime}:00` : null,
      endDateTime: form.endDateTime ? `${form.endDateTime}:00` : null,
      registrationStartDateTime: form.registrationStartDateTime ? `${form.registrationStartDateTime}:00` : null,
      registrationEndDateTime: form.registrationEndDateTime ? `${form.registrationEndDateTime}:00` : null,
      cancellationDeadline: form.cancellationDeadline ? `${form.cancellationDeadline}:00` : null,
      maximumParticipants: form.maximumParticipants === '' ? null : Number(form.maximumParticipants),
      minimumParticipants: form.minimumParticipants === '' ? null : Number(form.minimumParticipants),
      registrationFee: form.registrationFee === '' ? null : Number(form.registrationFee),
      currency: blankToNull(form.currency)
    };

    setServerError('');
    setFieldErrors({});
    setSaving(true);
    try {
      if (mode === 'edit' && eventId) {
        await eventApi.updateEvent(eventId, payload);
        pushToast('Event updated successfully.');
      } else {
        await eventApi.createEvent(payload);
        pushToast('Event created successfully.');
      }
      onSaved();
    } catch (error) {
      const validationMessages = extractValidationMessages(error);
      if (validationMessages.length) {
        const next = {};
        validationMessages.forEach(message => {
          const [field, ...rest] = message.split(':');
          if (rest.length && field.length < 40) {
            next[field.trim()] = rest.join(':').trim();
          }
        });
        setFieldErrors(next);
        setServerError('Please review the highlighted fields.');
      } else {
        setServerError(getApiErrorMessage(error, 'Unable to save the event right now.'));
      }
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <Card elevated><LoadingSkeleton lines={8} /></Card>;
  }

  const categories = catalogs.categories.map(item => ({ value: String(item.id), label: `${item.categoryName} (${item.categoryCode})` }));
  const types = catalogs.types.map(item => ({ value: String(item.id), label: `${item.typeName} (${item.typeCode})` }));
  const departments = catalogs.departments.map(item => ({ value: String(item.id), label: `${item.departmentName} (${item.departmentCode})` }));
  const academicYears = catalogs.academicYears.map(item => ({ value: String(item.id), label: item.yearLabel }));
  const venues = catalogs.venues.map(item => ({ value: String(item.id), label: `${item.venueName} (${item.venueCode})` }));

  return (
    <Card elevated>
      <div className="section-panel__title">
        <div>
          <h2>{mode === 'edit' ? 'Edit event' : 'Create event'}</h2>
          <p>Complete the core event details before moving to sessions, coordinators, and eligibility rules.</p>
        </div>
        <Button variant="secondary" onClick={onBack}>Back to events</Button>
      </div>
      {serverError && <div className={classNames('banner', 'banner--error')}>{serverError}</div>}
      <form className="stack stack--wide" onSubmit={handleSubmit}>
        <FormSection title="Basic information" description="Event identity and short summary.">
          <div className="grid grid--2">
            <Input id="title" label="Title" value={form.title} onChange={e => updateField('title', e.target.value)} />
            <Input id="eventCode" label="Event code" value={form.eventCode} onChange={e => updateField('eventCode', e.target.value)} />
            <Input id="slug" label="Slug" value={form.slug} onChange={e => updateField('slug', e.target.value)} />
            <Select id="visibility" label="Visibility" value={form.visibility} onChange={e => updateField('visibility', e.target.value)}>
              {VISIBILITY_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </Select>
          </div>
          <Textarea id="shortDescription" label="Short description" rows={3} value={form.shortDescription} onChange={e => updateField('shortDescription', e.target.value)} />
          <Textarea id="fullDescription" label="Full description" rows={5} value={form.fullDescription} onChange={e => updateField('fullDescription', e.target.value)} />
        </FormSection>

        <FormSection title="Classification" description="Choose the category, type, department, academic year, and venue.">
          <div className="grid grid--2">
            <Select id="eventCategoryId" label="Category" value={form.eventCategoryId} onChange={e => updateField('eventCategoryId', e.target.value)}>
              <option value="">Select category</option>
              {categories.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="eventTypeId" label="Type" value={form.eventTypeId} onChange={e => updateField('eventTypeId', e.target.value)}>
              <option value="">Select type</option>
              {types.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="organizingDepartmentId" label="Department" value={form.organizingDepartmentId} onChange={e => updateField('organizingDepartmentId', e.target.value)}>
              <option value="">Select department</option>
              {departments.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="academicYearId" label="Academic year" value={form.academicYearId} onChange={e => updateField('academicYearId', e.target.value)}>
              <option value="">Select academic year</option>
              {academicYears.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="venueId" label="Venue" value={form.venueId} onChange={e => updateField('venueId', e.target.value)}>
              <option value="">Select venue</option>
              {venues.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </Select>
            <Select id="mode" label="Mode" value={form.mode} onChange={e => updateField('mode', e.target.value)}>
              {MODE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </Select>
          </div>
        </FormSection>

        <FormSection title="Schedule and registration" description="Set the event window and registration deadlines.">
          <div className="grid grid--2">
            <Input id="startDateTime" label="Start date and time" type="datetime-local" value={form.startDateTime} onChange={e => updateField('startDateTime', e.target.value)} />
            <Input id="endDateTime" label="End date and time" type="datetime-local" value={form.endDateTime} onChange={e => updateField('endDateTime', e.target.value)} />
            <Input id="registrationStartDateTime" label="Registration start" type="datetime-local" value={form.registrationStartDateTime} onChange={e => updateField('registrationStartDateTime', e.target.value)} />
            <Input id="registrationEndDateTime" label="Registration end" type="datetime-local" value={form.registrationEndDateTime} onChange={e => updateField('registrationEndDateTime', e.target.value)} />
            <Input id="cancellationDeadline" label="Cancellation deadline" type="datetime-local" value={form.cancellationDeadline} onChange={e => updateField('cancellationDeadline', e.target.value)} />
            <Input id="onlineMeetingUrl" label="Online meeting URL" value={form.onlineMeetingUrl} onChange={e => updateField('onlineMeetingUrl', e.target.value)} />
          </div>
        </FormSection>

        <FormSection title="Capacity and contact" description="Define the participant limits and contact details.">
          <div className="grid grid--2">
            <Input id="minimumParticipants" label="Minimum participants" type="number" min="1" value={form.minimumParticipants} onChange={e => updateField('minimumParticipants', e.target.value)} />
            <Input id="maximumParticipants" label="Maximum participants" type="number" min="1" value={form.maximumParticipants} onChange={e => updateField('maximumParticipants', e.target.value)} />
            <Input id="registrationFee" label="Registration fee" type="number" min="0" step="0.01" value={form.registrationFee} onChange={e => updateField('registrationFee', e.target.value)} />
            <Input id="currency" label="Currency" value={form.currency} onChange={e => updateField('currency', e.target.value)} />
            <Input id="bannerImageUrl" label="Banner image URL" value={form.bannerImageUrl} onChange={e => updateField('bannerImageUrl', e.target.value)} />
            <Input id="contactEmail" label="Contact email" type="email" value={form.contactEmail} onChange={e => updateField('contactEmail', e.target.value)} />
            <Input id="contactPhone" label="Contact phone" value={form.contactPhone} onChange={e => updateField('contactPhone', e.target.value)} />
          </div>
        </FormSection>

        <ValidationMessages messages={Object.values(fieldErrors)} />
        <div className="workspace-footer">
          <LoadingButton type="submit" loading={saving}>{mode === 'edit' ? 'Update event' : 'Create event'}</LoadingButton>
        </div>
      </form>
    </Card>
  );
}

function CollectionWorkspace({ kind, catalogs, setCatalogs, scopeInstitutionId, pushToast }) {
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [active, setActive] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [rows, setRows] = useState([]);
  const [pageMeta, setPageMeta] = useState({ page: 1, totalPages: 0 });
  const [serverError, setServerError] = useState('');
  const [editingRow, setEditingRow] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({});
  const [fieldErrors, setFieldErrors] = useState({});

  const singular = kind === 'categories' ? 'category' : kind === 'types' ? 'type' : 'venue';

  const config = useMemo(() => {
    if (kind === 'categories') {
      return {
        title: 'Event categories',
        description: 'Manage the category taxonomy used to group events and symposiums.',
        list: params => eventApi.listCategories(params),
        create: payload => eventApi.createCategory(payload),
        update: (id, payload) => eventApi.updateCategory(id, payload),
        toggle: (id, value) => eventApi.updateCategoryStatus(id, value),
        columns: [
          { key: 'categoryCode', header: 'Code' },
          { key: 'categoryName', header: 'Category' },
          { key: 'description', header: 'Description' },
          { key: 'status', header: 'Status', render: row => <Badge tone={getStatusTone(row.active ? 'ACTIVE' : 'INACTIVE')}>{row.active ? 'Active' : 'Inactive'}</Badge> }
        ],
        fields: [
          { name: 'institutionId', label: 'Institution', type: 'text' },
          { name: 'categoryCode', label: 'Category code' },
          { name: 'categoryName', label: 'Category name' },
          { name: 'description', label: 'Description', type: 'textarea' }
        ],
        defaultValues: { institutionId: scopeInstitutionId ?? '', categoryCode: '', categoryName: '', description: '' },
        buildPayload: values => ({ ...values, institutionId: Number(values.institutionId) || scopeInstitutionId || null })
      };
    }

    if (kind === 'types') {
      return {
        title: 'Event types',
        description: 'Manage event types such as workshop, symposium, hackathon, or seminar.',
        list: params => eventApi.listTypes(params),
        create: payload => eventApi.createType(payload),
        update: (id, payload) => eventApi.updateType(id, payload),
        toggle: (id, value) => eventApi.updateTypeStatus(id, value),
        columns: [
          { key: 'typeCode', header: 'Code' },
          { key: 'typeName', header: 'Type' },
          { key: 'description', header: 'Description' },
          { key: 'status', header: 'Status', render: row => <Badge tone={getStatusTone(row.active ? 'ACTIVE' : 'INACTIVE')}>{row.active ? 'Active' : 'Inactive'}</Badge> }
        ],
        fields: [
          { name: 'institutionId', label: 'Institution', type: 'text' },
          { name: 'typeCode', label: 'Type code' },
          { name: 'typeName', label: 'Type name' },
          { name: 'description', label: 'Description', type: 'textarea' }
        ],
        defaultValues: { institutionId: scopeInstitutionId ?? '', typeCode: '', typeName: '', description: '' },
        buildPayload: values => ({ ...values, institutionId: Number(values.institutionId) || scopeInstitutionId || null })
      };
    }

    return {
      title: 'Venues',
      description: 'Capture lecture halls, seminar spaces, and online venues used by events.',
      list: params => eventApi.listVenues(params),
      create: payload => eventApi.createVenue(payload),
      update: (id, payload) => eventApi.updateVenue(id, payload),
      toggle: (id, value) => eventApi.updateVenueStatus(id, value),
      columns: [
        { key: 'venueCode', header: 'Code' },
        { key: 'venueName', header: 'Venue' },
        { key: 'venueType', header: 'Type', render: row => makeStatusLabel(row.venueType) },
        { key: 'capacity', header: 'Capacity' },
        { key: 'status', header: 'Status', render: row => <Badge tone={getStatusTone(row.active ? 'ACTIVE' : 'INACTIVE')}>{row.active ? 'Active' : 'Inactive'}</Badge> }
      ],
      fields: [
        { name: 'institutionId', label: 'Institution', type: 'text' },
        { name: 'venueCode', label: 'Venue code' },
        { name: 'venueName', label: 'Venue name' },
        { name: 'building', label: 'Building' },
        { name: 'floor', label: 'Floor' },
        { name: 'roomNumber', label: 'Room number' },
        { name: 'address', label: 'Address', type: 'textarea' },
        { name: 'capacity', label: 'Capacity', type: 'number', min: 0 },
        { name: 'venueType', label: 'Venue type', type: 'select', options: VENUE_TYPE_OPTIONS.map(([value, label]) => ({ value, label })) }
      ],
      defaultValues: { institutionId: scopeInstitutionId ?? '', venueCode: '', venueName: '', building: '', floor: '', roomNumber: '', address: '', capacity: '', venueType: 'OTHER' },
      buildPayload: values => ({ ...values, institutionId: Number(values.institutionId) || scopeInstitutionId || null, capacity: values.capacity === '' ? null : Number(values.capacity) })
    };
  }, [kind, scopeInstitutionId]);

  useEffect(() => {
    let activeRequest = true;
    async function load() {
      try {
        setLoading(true);
        setServerError('');
        const response = await config.list({
          search: search || undefined,
          active: active === '' ? undefined : active === 'true',
          page: page - 1,
          size
        });
        if (!activeRequest) {
          return;
        }
        const nextPage = unwrapPage(response);
        setRows(nextPage.content);
        setPageMeta({ page: nextPage.page, totalPages: nextPage.totalPages });
      } catch (error) {
        if (activeRequest) {
          setServerError(getApiErrorMessage(error, 'Unable to load this workspace right now.'));
        }
      } finally {
        if (activeRequest) {
          setLoading(false);
        }
      }
    }
    load();
    return () => {
      activeRequest = false;
    };
  }, [config, search, active, page, size]);

  function openCreate() {
    setEditingRow(null);
    setForm(config.defaultValues);
    setFieldErrors({});
    setModalOpen(true);
  }

  function openEdit(row) {
    setEditingRow(row);
    setForm({
      ...config.defaultValues,
      ...row,
      institutionId: String(row.institutionId ?? scopeInstitutionId ?? ''),
      venueType: row.venueType ?? 'OTHER',
      capacity: row.capacity ?? ''
    });
    setFieldErrors({});
    setModalOpen(true);
  }

  async function handleSave(event) {
    event.preventDefault();
    const payload = config.buildPayload(form);
    setSaving(true);
    setServerError('');
    try {
      if (editingRow) {
        await config.update(editingRow.id, payload);
        pushToast(`${singular.charAt(0).toUpperCase() + singular.slice(1)} updated successfully.`);
      } else {
        await config.create(payload);
        pushToast(`${singular.charAt(0).toUpperCase() + singular.slice(1)} created successfully.`);
      }
      setModalOpen(false);
      setPage(1);
      const refreshed = await config.list({ page: 0, size, search: search || undefined, active: active === '' ? undefined : active === 'true' });
      const nextPage = unwrapPage(refreshed);
      setRows(nextPage.content);
      setPageMeta({ page: nextPage.page, totalPages: nextPage.totalPages });
    } catch (error) {
      const validation = extractValidationMessages(error);
      if (validation.length) {
        setFieldErrors({ validation: validation[0] });
        setServerError('Please correct the highlighted fields.');
      } else {
        setServerError(getApiErrorMessage(error, 'Unable to save this record right now.'));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleToggle(row) {
    try {
      await config.toggle(row.id, !row.active);
      pushToast(`${singular.charAt(0).toUpperCase() + singular.slice(1)} status updated.`);
      const refreshed = await config.list({ page: page - 1, size, search: search || undefined, active: active === '' ? undefined : active === 'true' });
      const nextPage = unwrapPage(refreshed);
      setRows(nextPage.content);
      setPageMeta({ page: nextPage.page, totalPages: nextPage.totalPages });
    } catch (error) {
      setServerError(getApiErrorMessage(error, 'Unable to update status.'));
    }
  }

  return (
    <Card elevated className="workspace-table-card">
      <div className="section-panel__title">
        <div>
          <h2>{config.title}</h2>
          <p>{config.description}</p>
        </div>
        <Button onClick={openCreate}>Create</Button>
      </div>

      <div className="workspace-filters">
        <SearchBar value={search} onChange={setSearch} placeholder={`Search ${kind}`} />
        <div className="grid grid--3">
          <Select id={`${kind}-status`} label="Status" value={active} onChange={event => setActive(event.target.value)}>
            <option value="">All statuses</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </Select>
          <Select id={`${kind}-size`} label="Page size" value={size} onChange={event => setSize(Number(event.target.value))}>
            {[10, 20, 50].map(option => <option key={option} value={option}>{option}</option>)}
          </Select>
        </div>
      </div>

      {serverError && <ErrorState title="Unable to load records" description={serverError} onRetry={() => setPage(current => current)} />}
      {loading ? (
        <LoadingSkeleton lines={5} />
      ) : rows.length === 0 ? (
        <EmptyState title={`No ${kind} found`} description={`Create the first ${singular} to begin using CampusSphere event workflows.`} actionLabel="Create" onAction={openCreate} />
      ) : (
        <Table
          columns={[
            ...config.columns,
            {
              key: 'actions',
              header: 'Actions',
              render: row => (
                <div className="workspace-actions">
                  <Button variant="secondary" size="sm" onClick={() => openEdit(row)}>Edit</Button>
                  <Button variant="secondary" size="sm" onClick={() => handleToggle(row)}>{row.active ? 'Deactivate' : 'Activate'}</Button>
                </div>
              )
            }
          ]}
          rows={rows}
        />
      )}

      <Pagination page={pageMeta.page} totalPages={pageMeta.totalPages} onPageChange={setPage} />

      <CollectionModal
        open={modalOpen}
        title={editingRow ? `Edit ${singular}` : `Create ${singular}`}
        description={`Maintain the ${singular} catalogue used across events.`}
        fields={config.fields}
        values={form}
        errors={fieldErrors.validation ? [fieldErrors.validation] : []}
        onChange={(name, value) => setForm(current => ({ ...current, [name]: value }))}
        onSubmit={handleSave}
        onClose={() => setModalOpen(false)}
        loading={saving}
        submitLabel={editingRow ? 'Update' : 'Save'}
      />
    </Card>
  );
}

function EventListWorkspace({ catalogData, onOpenEvent, scopeInstitutionId, pushToast }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState({ status: '', mode: '', categoryId: '', typeId: '', departmentId: '', venueId: '' });

  async function load() {
    try {
      setLoading(true);
      setServerError('');
      const response = await eventApi.listEvents({
        search: search || undefined,
        status: filters.status || undefined,
        mode: filters.mode || undefined,
        categoryId: filters.categoryId || undefined,
        typeId: filters.typeId || undefined,
        departmentId: filters.departmentId || undefined,
        venueId: filters.venueId || undefined,
        page: page - 1,
        size
      });
      const nextPage = unwrapPage(response);
      setRows(nextPage.content);
      setTotalPages(nextPage.totalPages);
    } catch (error) {
      setServerError(getApiErrorMessage(error, 'Unable to load events right now.'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [page, size, search, filters.status, filters.mode, filters.categoryId, filters.typeId, filters.departmentId, filters.venueId]);

  const columns = [
    { key: 'title', header: 'Event', render: row => (<div><strong>{row.title}</strong><div className="dashboard-page__muted">{row.eventCode}</div></div>) },
    { key: 'status', header: 'Status', render: row => <Badge tone={getStatusTone(row.status)}>{makeStatusLabel(row.status)}</Badge> },
    { key: 'mode', header: 'Mode', render: row => makeStatusLabel(row.mode) },
    { key: 'eventCategoryName', header: 'Category' },
    { key: 'eventTypeName', header: 'Type' },
    { key: 'departmentName', header: 'Department' },
    { key: 'venueName', header: 'Venue' },
    { key: 'actions', header: 'Actions', render: row => (
      <div className="workspace-actions">
        <Button variant="secondary" size="sm" onClick={() => navigate(`${BASE_PATH}/${row.id}`)}>View</Button>
        <Button variant="secondary" size="sm" onClick={() => navigate(`${BASE_PATH}/${row.id}/edit`)}>Edit</Button>
      </div>
    ) }
  ];

  const summary = useMemo(() => {
    return {
      total: rows.length,
      draft: rows.filter(row => row.status === 'DRAFT').length,
      live: rows.filter(row => row.status === 'PUBLISHED' || row.status === 'REGISTRATION_OPEN').length,
      upcoming: rows.filter(row => row.status === 'PUBLISHED' && row.startDateTime).length
    };
  }, [rows]);

  return (
    <div className="dashboard-page">
      <Card elevated className="dashboard-page__hero workspace-hero">
        <div>
          <p className="eyebrow">Event management</p>
          <h1>Plan events, manage sessions, and keep every college workflow in one place.</h1>
          <p>CampusSphere keeps event setup institution-scoped, validated, and ready for publication without depending on hardcoded data.</p>
          <div className="dashboard-page__hero-actions">
            <Button as={Link} to={`${BASE_PATH}/new`}>Create event</Button>
            <Button as={Link} variant="secondary" to={`${BASE_PATH}/categories`}>Manage categories</Button>
          </div>
        </div>
        <div className="workspace-summary">
          <div className="workspace-summary__grid">
            <div className="workspace-summary__item"><span>Total events</span><strong>{summary.total}</strong></div>
            <div className="workspace-summary__item"><span>Draft</span><strong>{summary.draft}</strong></div>
            <div className="workspace-summary__item"><span>Published / open</span><strong>{summary.live}</strong></div>
            <div className="workspace-summary__item"><span>Upcoming</span><strong>{summary.upcoming}</strong></div>
          </div>
          <p className="dashboard-page__muted">Current institution scope: {scopeInstitutionId ?? 'resolved from your account'}</p>
        </div>
      </Card>

      <Tabs items={WORKSPACE_TAB_ITEMS} activeKey="events" onChange={key => navigate(key === 'overview' ? BASE_PATH : `${BASE_PATH}/${key}`)} />

      {serverError && <ErrorState title="Unable to load events" description={serverError} onRetry={load} />}

      <div className="workspace-table-card">
        <div className="workspace-filters">
          <SearchBar value={search} onChange={setSearch} placeholder="Search events by title or code" />
          <div className="grid grid--3">
            <Select id="event-status" label="Status" value={filters.status} onChange={e => setFilters(current => ({ ...current, status: e.target.value }))}>
              <option value="">All statuses</option>
              {STATUS_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </Select>
            <Select id="event-mode" label="Mode" value={filters.mode} onChange={e => setFilters(current => ({ ...current, mode: e.target.value }))}>
              <option value="">All modes</option>
              {MODE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </Select>
            <Select id="event-category" label="Category" value={filters.categoryId} onChange={e => setFilters(current => ({ ...current, categoryId: e.target.value }))}>
              <option value="">All categories</option>
              {catalogData.categories.map(item => <option key={item.id} value={item.id}>{item.categoryName}</option>)}
            </Select>
            <Select id="event-type" label="Type" value={filters.typeId} onChange={e => setFilters(current => ({ ...current, typeId: e.target.value }))}>
              <option value="">All types</option>
              {catalogData.types.map(item => <option key={item.id} value={item.id}>{item.typeName}</option>)}
            </Select>
            <Select id="event-department" label="Department" value={filters.departmentId} onChange={e => setFilters(current => ({ ...current, departmentId: e.target.value }))}>
              <option value="">All departments</option>
              {catalogData.departments.map(item => <option key={item.id} value={item.id}>{item.departmentName}</option>)}
            </Select>
            <Select id="event-venue" label="Venue" value={filters.venueId} onChange={e => setFilters(current => ({ ...current, venueId: e.target.value }))}>
              <option value="">All venues</option>
              {catalogData.venues.map(item => <option key={item.id} value={item.id}>{item.venueName}</option>)}
            </Select>
          </div>
        </div>
        {loading ? <LoadingSkeleton lines={6} /> : rows.length === 0 ? <EmptyState title="No events found" description="Create an event to begin scheduling sessions, coordinators, and registration settings." actionLabel="Create event" onAction={() => navigate(`${BASE_PATH}/new`)} /> : <Table columns={columns} rows={rows} />}
        <div className="workspace-footer">
          <Select id="event-size" label="Page size" value={size} onChange={e => setSize(Number(e.target.value))}>
            {[10, 20, 50].map(option => <option key={option} value={option}>{option}</option>)}
          </Select>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  );
}

function EventDetailWorkspace({ eventId, catalogData, scopeInstitutionId, pushToast }) {
  const navigate = useNavigate();
  const location = useLocation();
  const tab = new URLSearchParams(location.search).get('tab') ?? 'overview';
  const [loading, setLoading] = useState(true);
  const [event, setEvent] = useState(null);
  const [overview, setOverview] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [coordinators, setCoordinators] = useState([]);
  const [rules, setRules] = useState([]);
  const [config, setConfig] = useState(null);
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [dialog, setDialog] = useState(null);
  const [sessionForm, setSessionForm] = useState({ title: '', description: '', sessionStart: '', sessionEnd: '', venueId: '', speakerName: '', sequenceNumber: 1 });
  const [coordinatorForm, setCoordinatorForm] = useState({ userId: '', coordinatorRole: 'COORDINATOR', primaryCoordinator: false });
  const [ruleForm, setRuleForm] = useState({ departmentId: '', programmeId: '', sectionId: '', participantType: 'ALL', ruleType: 'INCLUDE', minimumYear: '', maximumYear: '' });
  const [registrationForm, setRegistrationForm] = useState({ registrationRequired: true, approvalRequired: false, waitlistEnabled: false, teamEvent: false, minimumTeamSize: '', maximumTeamSize: '', allowExternalParticipants: false, allowMultipleRegistrations: false, certificateEnabled: false, attendanceRequiredForCertificate: false, cancellationAllowed: false, cancellationDeadline: '' });

  async function load() {
    try {
      setLoading(true);
      const [eventRes, overviewRes, sessionsRes, coordinatorsRes, rulesRes, configRes] = await Promise.all([
        eventApi.getEvent(eventId),
        eventApi.getEventOverview(eventId),
        eventApi.listSessions(eventId),
        eventApi.listCoordinators(eventId),
        eventApi.listEligibilityRules(eventId),
        eventApi.getRegistrationConfig(eventId)
      ]);
      const eventData = unwrap(eventRes);
      setEvent(eventData);
      setOverview(unwrap(overviewRes));
      setSessions(unwrap(sessionsRes) ?? []);
      setCoordinators(unwrap(coordinatorsRes) ?? []);
      setRules(unwrap(rulesRes) ?? []);
      setConfig(unwrap(configRes));
      try {
        const usersRes = await userApi.listUsers();
        setUsers(unwrap(usersRes) ?? []);
      } catch {
        setUsers([]);
      }
      if (unwrap(configRes)) {
        const current = unwrap(configRes);
        setRegistrationForm({
          registrationRequired: current.registrationRequired,
          approvalRequired: current.approvalRequired,
          waitlistEnabled: current.waitlistEnabled,
          teamEvent: current.teamEvent,
          minimumTeamSize: current.minimumTeamSize ?? '',
          maximumTeamSize: current.maximumTeamSize ?? '',
          allowExternalParticipants: current.allowExternalParticipants,
          allowMultipleRegistrations: current.allowMultipleRegistrations,
          certificateEnabled: current.certificateEnabled,
          attendanceRequiredForCertificate: current.attendanceRequiredForCertificate,
          cancellationAllowed: current.cancellationAllowed,
          cancellationDeadline: current.cancellationDeadline ? String(current.cancellationDeadline).slice(0, 16) : ''
        });
      }
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, 'Unable to load event details.'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [eventId]);

  async function changeStatus(status) {
    try {
      setSaving(true);
      await eventApi.updateEventStatus(eventId, { status });
      pushToast('Event status updated.');
      await load();
    } catch (statusError) {
      setError(getApiErrorMessage(statusError, 'Unable to change the event status.'));
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <Card elevated><LoadingSkeleton lines={8} /></Card>;
  }

  if (!event) {
    return <ErrorState title="Event not found" description={error || 'The selected event is unavailable.'} onRetry={load} />;
  }

  const activeTab = tab;
  const availableNextStatuses = {
    DRAFT: ['PENDING_APPROVAL', 'CANCELLED', 'ARCHIVED'],
    PENDING_APPROVAL: ['DRAFT', 'PUBLISHED', 'CANCELLED', 'ARCHIVED'],
    PUBLISHED: ['REGISTRATION_OPEN', 'CANCELLED', 'ARCHIVED'],
    REGISTRATION_OPEN: ['REGISTRATION_CLOSED', 'ONGOING', 'CANCELLED', 'ARCHIVED'],
    REGISTRATION_CLOSED: ['ONGOING', 'CANCELLED', 'ARCHIVED'],
    ONGOING: ['COMPLETED', 'CANCELLED', 'ARCHIVED'],
    COMPLETED: ['ARCHIVED'],
    CANCELLED: ['ARCHIVED'],
    ARCHIVED: []
  }[event.status] ?? [];

  return (
    <div className="dashboard-page">
      <DetailHeader
        event={event}
        onBack={() => navigate(BASE_PATH)}
        onEdit={() => navigate(`${BASE_PATH}/${eventId}/edit`)}
        onStatusChange={changeStatus}
        nextStatuses={availableNextStatuses}
      />

      <Tabs items={DETAIL_TAB_ITEMS} activeKey={activeTab} onChange={key => navigate(`${BASE_PATH}/${eventId}?tab=${key}`)} />

      {activeTab === 'overview' && (
        <div className="workspace-overview">
          <Card elevated className="workspace-summary">
            <div className="workspace-summary__grid">
              <div className="workspace-summary__item"><span>Publication readiness</span><strong>{overview?.publicationReady ? 'Ready' : 'Not yet ready'}</strong></div>
              <div className="workspace-summary__item"><span>Coordinators</span><strong>{overview?.coordinatorCount ?? coordinators.length}</strong></div>
              <div className="workspace-summary__item"><span>Sessions</span><strong>{overview?.sessionCount ?? sessions.length}</strong></div>
              <div className="workspace-summary__item"><span>Eligibility rules</span><strong>{overview?.eligibilityRuleCount ?? rules.length}</strong></div>
            </div>
            <p className="dashboard-page__muted">{overview?.nextAction ?? 'Keep the event setup complete before publishing.'}</p>
          </Card>
          <Card elevated className="workspace-summary">
            <h3>Setup checklist</h3>
            <ul className="workspace-summary__list">
              <li>Confirm event timeline and venue.</li>
              <li>Add at least one coordinator.</li>
              <li>Configure sessions and registration settings.</li>
              <li>Review eligibility rules before publishing.</li>
            </ul>
          </Card>
        </div>
      )}

      {activeTab === 'sessions' && (
        <Card elevated>
          <div className="section-panel__title">
            <div>
              <h2>Sessions</h2>
              <p>Create or update the session schedule for this event.</p>
            </div>
          </div>
          <FormSection title="Add session" description="Keep the session inside the event window.">
            <div className="grid grid--2">
              <Input label="Title" value={sessionForm.title} onChange={e => setSessionForm(current => ({ ...current, title: e.target.value }))} />
              <Input label="Sequence" type="number" min="1" value={sessionForm.sequenceNumber} onChange={e => setSessionForm(current => ({ ...current, sequenceNumber: e.target.value }))} />
              <Input label="Start" type="datetime-local" value={sessionForm.sessionStart} onChange={e => setSessionForm(current => ({ ...current, sessionStart: e.target.value }))} />
              <Input label="End" type="datetime-local" value={sessionForm.sessionEnd} onChange={e => setSessionForm(current => ({ ...current, sessionEnd: e.target.value }))} />
              <Select label="Venue" value={sessionForm.venueId} onChange={e => setSessionForm(current => ({ ...current, venueId: e.target.value }))}>
                <option value="">Select venue</option>
                {catalogData.venues.map(item => <option key={item.id} value={item.id}>{item.venueName}</option>)}
              </Select>
              <Input label="Speaker" value={sessionForm.speakerName} onChange={e => setSessionForm(current => ({ ...current, speakerName: e.target.value }))} />
            </div>
            <Textarea label="Description" rows={3} value={sessionForm.description} onChange={e => setSessionForm(current => ({ ...current, description: e.target.value }))} />
            <Button onClick={async () => {
              setSaving(true);
              try {
                await eventApi.createSession(eventId, {
                  eventId: Number(eventId),
                  title: sessionForm.title,
                  description: sessionForm.description,
                  sessionStart: sessionForm.sessionStart ? `${sessionForm.sessionStart}:00` : null,
                  sessionEnd: sessionForm.sessionEnd ? `${sessionForm.sessionEnd}:00` : null,
                  venueId: sessionForm.venueId ? Number(sessionForm.venueId) : null,
                  speakerName: sessionForm.speakerName,
                  sequenceNumber: Number(sessionForm.sequenceNumber)
                });
                pushToast('Session added successfully.');
                await load();
              } catch (sessionError) {
                setError(getApiErrorMessage(sessionError, 'Unable to save the session.'));
              } finally {
                setSaving(false);
              }
            }} disabled={saving}>Add session</Button>
          </FormSection>
          <Table
            columns={[
              { key: 'sequenceNumber', header: '#' },
              { key: 'title', header: 'Title' },
              { key: 'sessionStart', header: 'Start' },
              { key: 'sessionEnd', header: 'End' },
              { key: 'venueName', header: 'Venue' },
              { key: 'status', header: 'Status' }
            ]}
            rows={sessions}
          />
        </Card>
      )}

      {activeTab === 'coordinators' && (
        <Card elevated>
          <FormSection title="Assign coordinator" description="Choose a user from the current institution.">
            <div className="grid grid--2">
              <Select label="User" value={coordinatorForm.userId} onChange={e => setCoordinatorForm(current => ({ ...current, userId: e.target.value }))}>
                <option value="">Select user</option>
                {users.filter(user => user.institutionId === event.institutionId).map(user => <option key={user.id} value={user.id}>{buildDisplayName(user)} ({user.email})</option>)}
              </Select>
              <Select label="Coordinator role" value={coordinatorForm.coordinatorRole} onChange={e => setCoordinatorForm(current => ({ ...current, coordinatorRole: e.target.value }))}>
                {COORDINATOR_ROLE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
            </div>
            <label className="checkbox">
              <input type="checkbox" checked={coordinatorForm.primaryCoordinator} onChange={e => setCoordinatorForm(current => ({ ...current, primaryCoordinator: e.target.checked }))} />
              Primary coordinator
            </label>
            <Button onClick={async () => {
              setSaving(true);
              try {
                await eventApi.assignCoordinator(eventId, {
                  eventId: Number(eventId),
                  userId: Number(coordinatorForm.userId),
                  coordinatorRole: coordinatorForm.coordinatorRole,
                  primaryCoordinator: Boolean(coordinatorForm.primaryCoordinator)
                });
                pushToast('Coordinator assigned.');
                await load();
              } catch (coordinatorError) {
                setError(getApiErrorMessage(coordinatorError, 'Unable to assign coordinator.'));
              } finally {
                setSaving(false);
              }
            }}>Assign</Button>
          </FormSection>
          <Table
            columns={[
              { key: 'userName', header: 'User' },
              { key: 'userEmail', header: 'Email' },
              { key: 'coordinatorRole', header: 'Role' },
              { key: 'primaryCoordinator', header: 'Primary', render: row => (row.primaryCoordinator ? 'Yes' : 'No') }
            ]}
            rows={coordinators}
          />
        </Card>
      )}

      {activeTab === 'eligibility' && (
        <Card elevated>
          <FormSection title="Eligibility rule" description="Define who can participate in this event.">
            <div className="grid grid--2">
              <Select label="Participant type" value={ruleForm.participantType} onChange={e => setRuleForm(current => ({ ...current, participantType: e.target.value }))}>
                {PARTICIPANT_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Select label="Rule type" value={ruleForm.ruleType} onChange={e => setRuleForm(current => ({ ...current, ruleType: e.target.value }))}>
                {ELIGIBILITY_RULE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Select label="Department" value={ruleForm.departmentId} onChange={e => setRuleForm(current => ({ ...current, departmentId: e.target.value }))}>
                <option value="">Any department</option>
                {catalogData.departments.map(item => <option key={item.id} value={item.id}>{item.departmentName}</option>)}
              </Select>
              <Select label="Programme" value={ruleForm.programmeId} onChange={e => setRuleForm(current => ({ ...current, programmeId: e.target.value }))}>
                <option value="">Any programme</option>
                {catalogData.programmes.map(item => <option key={item.id} value={item.id}>{item.programmeName}</option>)}
              </Select>
              <Select label="Section" value={ruleForm.sectionId} onChange={e => setRuleForm(current => ({ ...current, sectionId: e.target.value }))}>
                <option value="">Any section</option>
                {catalogData.sections.map(item => <option key={item.id} value={item.id}>{item.sectionName}</option>)}
              </Select>
              <Input label="Minimum year" type="number" min="1" value={ruleForm.minimumYear} onChange={e => setRuleForm(current => ({ ...current, minimumYear: e.target.value }))} />
              <Input label="Maximum year" type="number" min="1" value={ruleForm.maximumYear} onChange={e => setRuleForm(current => ({ ...current, maximumYear: e.target.value }))} />
            </div>
            <Button onClick={async () => {
              setSaving(true);
              try {
                await eventApi.createEligibilityRule(eventId, {
                  eventId: Number(eventId),
                  departmentId: ruleForm.departmentId ? Number(ruleForm.departmentId) : null,
                  programmeId: ruleForm.programmeId ? Number(ruleForm.programmeId) : null,
                  sectionId: ruleForm.sectionId ? Number(ruleForm.sectionId) : null,
                  participantType: ruleForm.participantType,
                  ruleType: ruleForm.ruleType,
                  minimumYear: ruleForm.minimumYear === '' ? null : Number(ruleForm.minimumYear),
                  maximumYear: ruleForm.maximumYear === '' ? null : Number(ruleForm.maximumYear)
                });
                pushToast('Eligibility rule added.');
                await load();
              } catch (eligibilityError) {
                setError(getApiErrorMessage(eligibilityError, 'Unable to save the eligibility rule.'));
              } finally {
                setSaving(false);
              }
            }}>Add rule</Button>
          </FormSection>
          <Table columns={[{ key: 'departmentName', header: 'Department' }, { key: 'programmeName', header: 'Programme' }, { key: 'sectionName', header: 'Section' }, { key: 'participantType', header: 'Participant type' }, { key: 'ruleType', header: 'Rule type' }]} rows={rules} />
        </Card>
      )}

      {activeTab === 'registration' && (
        <Card elevated>
          <FormSection title="Registration configuration" description="Control approval, waitlist, and certificate behaviour.">
            <div className="grid grid--2">
              {[
                ['registrationRequired', 'Registration required'],
                ['approvalRequired', 'Approval required'],
                ['waitlistEnabled', 'Waitlist enabled'],
                ['teamEvent', 'Team event'],
                ['allowExternalParticipants', 'External participants allowed'],
                ['allowMultipleRegistrations', 'Multiple registrations allowed'],
                ['certificateEnabled', 'Certificate enabled'],
                ['attendanceRequiredForCertificate', 'Attendance required for certificate'],
                ['cancellationAllowed', 'Cancellation allowed']
              ].map(([name, label]) => (
                <label key={name} className="checkbox">
                  <input type="checkbox" checked={Boolean(registrationForm[name])} onChange={e => setRegistrationForm(current => ({ ...current, [name]: e.target.checked }))} />
                  {label}
                </label>
              ))}
              <Input label="Minimum team size" type="number" min="1" value={registrationForm.minimumTeamSize} onChange={e => setRegistrationForm(current => ({ ...current, minimumTeamSize: e.target.value }))} />
              <Input label="Maximum team size" type="number" min="1" value={registrationForm.maximumTeamSize} onChange={e => setRegistrationForm(current => ({ ...current, maximumTeamSize: e.target.value }))} />
              <Input label="Cancellation deadline" type="datetime-local" value={registrationForm.cancellationDeadline} onChange={e => setRegistrationForm(current => ({ ...current, cancellationDeadline: e.target.value }))} />
            </div>
            <Button onClick={async () => {
              setSaving(true);
              try {
                await eventApi.saveRegistrationConfig(eventId, {
                  eventId: Number(eventId),
                  ...registrationForm,
                  minimumTeamSize: registrationForm.minimumTeamSize === '' ? null : Number(registrationForm.minimumTeamSize),
                  maximumTeamSize: registrationForm.maximumTeamSize === '' ? null : Number(registrationForm.maximumTeamSize),
                  cancellationDeadline: registrationForm.cancellationDeadline ? `${registrationForm.cancellationDeadline}:00` : null
                });
                pushToast('Registration settings saved.');
                await load();
              } catch (registrationError) {
                setError(getApiErrorMessage(registrationError, 'Unable to save registration settings.'));
              } finally {
                setSaving(false);
              }
            }}>Save settings</Button>
          </FormSection>
          {config ? <Badge tone="success">Configuration exists</Badge> : <EmptyState title="No registration settings yet" description="Create the registration configuration before opening signups." />}
        </Card>
      )}

      {activeTab === 'status' && (
        <Card elevated>
          <h2>Lifecycle</h2>
          <p className="dashboard-page__muted">Only valid next actions are shown here, and the backend still enforces the transition rules.</p>
          <div className="workspace-actions">
            {availableNextStatuses.map(status => (
              <Button key={status} variant="secondary" onClick={() => changeStatus(status)} disabled={saving}>
                {makeStatusLabel(status)}
              </Button>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}

export default function EventManagementPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { pushToast, toasts } = useToastQueue();
  const parsed = parsePath(location.pathname);
  const [catalogs, setCatalogs] = useState({
    categories: [],
    types: [],
    venues: [],
    departments: [],
    programmes: [],
    academicYears: [],
    sections: []
  });
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState('');

  useEffect(() => {
    let active = true;
    async function loadCatalogs() {
      try {
        setCatalogLoading(true);
        const [categoriesRes, typesRes, venuesRes, departmentsRes, programmesRes, yearsRes, sectionsRes] = await Promise.all([
          eventApi.listCategories({ page: 0, size: 200 }),
          eventApi.listTypes({ page: 0, size: 200 }),
          eventApi.listVenues({ page: 0, size: 200 }),
          masterDataApi.listDepartments({ page: 0, size: 200 }),
          masterDataApi.listProgrammes({ page: 0, size: 200 }),
          masterDataApi.listAcademicYears({ page: 0, size: 200 }),
          masterDataApi.listSections({ page: 0, size: 200 })
        ]);
        if (!active) {
          return;
        }
        setCatalogs({
          categories: unwrapPage(categoriesRes).content,
          types: unwrapPage(typesRes).content,
          venues: unwrapPage(venuesRes).content,
          departments: unwrapPage(departmentsRes).content,
          programmes: unwrapPage(programmesRes).content,
          academicYears: unwrapPage(yearsRes).content,
          sections: unwrapPage(sectionsRes).content
        });
      } catch (error) {
        if (active) {
          setCatalogError(getApiErrorMessage(error, 'Unable to load event setup catalogs.'));
        }
      } finally {
        if (active) {
          setCatalogLoading(false);
        }
      }
    }
    loadCatalogs();
    return () => {
      active = false;
    };
  }, []);

  const scopeInstitutionId = user?.institutionId ?? user?.institution?.id ?? '';

  if (catalogLoading) {
    return <Card elevated><LoadingSkeleton lines={7} /></Card>;
  }

  if (catalogError) {
    return <ErrorState title="Event workspace unavailable" description={catalogError} onRetry={() => window.location.reload()} />;
  }

  if (parsed.view === 'collection') {
    return (
      <>
        <CollectionWorkspace kind={parsed.kind} catalogs={catalogs} setCatalogs={setCatalogs} scopeInstitutionId={scopeInstitutionId} pushToast={pushToast} />
        <div className="toast-stack">
          {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
        </div>
      </>
    );
  }

  if (parsed.view === 'event-form') {
    return (
      <>
        <EventFormPage
          mode={parsed.mode}
          eventId={parsed.eventId}
          catalogs={catalogs}
          scopeInstitutionId={scopeInstitutionId}
          pushToast={pushToast}
          onBack={() => navigate(BASE_PATH)}
          onSaved={() => navigate(BASE_PATH)}
        />
        <div className="toast-stack">
          {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
        </div>
      </>
    );
  }

  if (parsed.view === 'event-detail') {
    return (
      <>
        <EventDetailWorkspace eventId={parsed.eventId} catalogData={catalogs} scopeInstitutionId={scopeInstitutionId} pushToast={pushToast} />
        <div className="toast-stack">
          {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
        </div>
      </>
    );
  }

  return (
    <>
      <EventListWorkspace catalogData={catalogs} scopeInstitutionId={scopeInstitutionId} pushToast={pushToast} />
      <div className="toast-stack">
        {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
      </div>
    </>
  );
}
