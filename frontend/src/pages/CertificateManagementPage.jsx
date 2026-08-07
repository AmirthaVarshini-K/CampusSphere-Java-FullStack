import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
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
import SectionHeading from '../components/SectionHeading';
import Select from '../components/Select';
import Table from '../components/Table';
import Tabs from '../components/Tabs';
import Textarea from '../components/Textarea';
import { Toast } from '../components/Toast';
import { APP_ROUTES } from '../constants/routes';
import { certificateApi } from '../services/certificateApi';
import { eventApi } from '../services/eventApi';
import { userApi } from '../services/userApi';
import useToastQueue from '../hooks/useToastQueue';
import { extractValidationMessages, getApiErrorMessage, isNetworkError } from '../utils/apiErrors';
import { getPrimaryRole } from '../utils/auth';
import { useAuth } from '../context/AuthContext';

const TABS = [
  { key: 'overview', label: 'Overview', path: APP_ROUTES.certificates },
  { key: 'templates', label: 'Templates', path: APP_ROUTES.certificateTemplates },
  { key: 'generated', label: 'Generated', path: APP_ROUTES.certificateGenerated },
  { key: 'settings', label: 'Settings', path: APP_ROUTES.certificateSettings }
];

const CERTIFICATE_TYPE_OPTIONS = [
  ['PARTICIPATION', 'Participation'],
  ['WINNER', 'Winner'],
  ['ORGANIZER', 'Organizer'],
  ['VOLUNTEER', 'Volunteer'],
  ['JUDGE', 'Judge'],
  ['FACULTY_COORDINATOR', 'Faculty Coordinator']
];

const ORIENTATION_OPTIONS = [
  ['PORTRAIT', 'Portrait'],
  ['LANDSCAPE', 'Landscape']
];

const STATUS_OPTIONS = [
  ['', 'All statuses'],
  ['ISSUED', 'Issued'],
  ['DRAFT', 'Draft'],
  ['REVOKED', 'Revoked'],
  ['ARCHIVED', 'Archived']
];

const TEMPLATE_STATUS_OPTIONS = [
  ['', 'All states'],
  ['true', 'Active'],
  ['false', 'Inactive']
];

function unwrap(response) {
  return response?.data?.data ?? null;
}

function unwrapPage(response) {
  const data = unwrap(response);
  return {
    content: data?.content ?? [],
    page: (data?.page ?? 0) + 1,
    size: data?.size ?? 10,
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    first: Boolean(data?.first),
    last: Boolean(data?.last)
  };
}

function formatDateTime(value) {
  return value ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '-';
}

function formatDate(value) {
  return value ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium' }).format(new Date(value)) : '-';
}

function titleCase(value) {
  if (!value) {
    return '-';
  }
  return value
    .toString()
    .toLowerCase()
    .split('_')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function toneForCertificate(status) {
  switch (status) {
    case 'ISSUED':
      return 'success';
    case 'REVOKED':
      return 'danger';
    case 'DRAFT':
      return 'warning';
    default:
      return 'neutral';
  }
}

function toneForTemplate(active) {
  return active ? 'success' : 'neutral';
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(url);
}

const emptyTemplateForm = {
  templateCode: '',
  templateName: '',
  certificateType: 'PARTICIPATION',
  orientation: 'PORTRAIT',
  description: '',
  institutionLogoUrl: '',
  organizerLogoUrl: '',
  backgroundImageUrl: '',
  signatureLeftUrl: '',
  signatureRightUrl: '',
  sealUrl: '',
  primaryColor: '#0f172a',
  accentColor: '#4338ca',
  watermarkText: '',
  marginTopMm: 12,
  marginRightMm: 12,
  marginBottomMm: 12,
  marginLeftMm: 12,
  qrCodeEnabled: true,
  verificationUrlBase: '',
  templateHtml: '',
  variablesJson: '',
  active: true
};

const emptyIssueForm = {
  eventId: '',
  sessionId: '',
  academicYearId: '',
  recipientUserId: '',
  certificateType: 'PARTICIPATION',
  templateId: '',
  recipientRole: '',
  position: '',
  prize: '',
  attendancePercentage: '',
  issueDate: '',
  adminOverride: false,
  remarks: ''
};

export default function CertificateManagementPage() {
  const { user } = useAuth();
  const roleCode = getPrimaryRole(user);
  const navigate = useNavigate();
  const location = useLocation();
  const { toasts, pushToast } = useToastQueue();

  const [dashboard, setDashboard] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(true);
  const [dashboardError, setDashboardError] = useState('');

  const [certificatesPage, setCertificatesPage] = useState({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0 });
  const [certificatesLoading, setCertificatesLoading] = useState(true);
  const [certificatesError, setCertificatesError] = useState('');
  const [certificateSearch, setCertificateSearch] = useState('');
  const [certificateStatus, setCertificateStatus] = useState('');
  const [certificateType, setCertificateType] = useState('');
  const [certificatePageNumber, setCertificatePageNumber] = useState(1);

  const [templatesPage, setTemplatesPage] = useState({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0 });
  const [templatesLoading, setTemplatesLoading] = useState(true);
  const [templatesError, setTemplatesError] = useState('');
  const [templateSearch, setTemplateSearch] = useState('');
  const [templateType, setTemplateType] = useState('');
  const [templateActive, setTemplateActive] = useState('');
  const [templatePageNumber, setTemplatePageNumber] = useState(1);

  const [settings, setSettings] = useState(null);
  const [settingsLoading, setSettingsLoading] = useState(true);
  const [settingsError, setSettingsError] = useState('');

  const [events, setEvents] = useState([]);
  const [users, setUsers] = useState([]);

  const [templateModalOpen, setTemplateModalOpen] = useState(false);
  const [templateActionLoading, setTemplateActionLoading] = useState(false);
  const [templateErrors, setTemplateErrors] = useState([]);
  const [templateForm, setTemplateForm] = useState(emptyTemplateForm);
  const [editingTemplateId, setEditingTemplateId] = useState(null);
  const [templatePreview, setTemplatePreview] = useState(null);

  const [issueModalOpen, setIssueModalOpen] = useState(false);
  const [issueActionLoading, setIssueActionLoading] = useState(false);
  const [issueErrors, setIssueErrors] = useState([]);
  const [issueForm, setIssueForm] = useState(emptyIssueForm);
  const [issuePreview, setIssuePreview] = useState(null);

  const [bulkModalOpen, setBulkModalOpen] = useState(false);
  const [bulkActionLoading, setBulkActionLoading] = useState(false);
  const [bulkErrors, setBulkErrors] = useState([]);
  const [bulkForm, setBulkForm] = useState({
    eventId: '',
    certificateType: 'PARTICIPATION',
    templateId: '',
    recipientUserIds: '',
    recipientRole: '',
    position: '',
    prize: '',
    attendancePercentage: '',
    issueDate: '',
    adminOverride: false,
    remarks: ''
  });

  const [selectedCertificate, setSelectedCertificate] = useState(null);
  const [revokeDialogOpen, setRevokeDialogOpen] = useState(false);
  const [revokeReason, setRevokeReason] = useState('');
  const [revokeLoading, setRevokeLoading] = useState(false);

  const activeTab = useMemo(() => {
    if (location.pathname.includes('/templates')) {
      return 'templates';
    }
    if (location.pathname.includes('/generated')) {
      return 'generated';
    }
    if (location.pathname.includes('/settings')) {
      return 'settings';
    }
    return 'overview';
  }, [location.pathname]);

  const eventOptions = useMemo(() => events.map(event => ({ value: String(event.id), label: `${event.title}${event.eventCode ? ` · ${event.eventCode}` : ''}` })), [events]);
  const userOptions = useMemo(() => users.map(item => ({ value: String(item.id), label: `${item.firstName} ${item.lastName}${item.registerNumber ? ` · ${item.registerNumber}` : ''}`.trim() })), [users]);
  const templateOptions = useMemo(() => templatesPage.content.map(item => ({ value: String(item.id), label: `${item.templateName} · ${item.templateCode}` })), [templatesPage.content]);

  function goToTab(tabKey) {
    const tab = TABS.find(item => item.key === tabKey);
    if (tab) {
      navigate(tab.path);
    }
  }

  useEffect(() => {
    let active = true;

    async function loadInitialData() {
      setDashboardLoading(true);
      setTemplatesLoading(true);
      setCertificatesLoading(true);
      setSettingsLoading(true);
      try {
        const [dashboardResponse, certificatesResponse, templatesResponse, settingsResponse, eventsResponse, usersResponse] = await Promise.all([
          certificateApi.getDashboard(),
          certificateApi.listCertificates({ page: certificatePageNumber - 1, size: 10, search: certificateSearch || undefined, certificateStatus: certificateStatus || undefined, certificateType: certificateType || undefined }),
          certificateApi.listTemplates({ page: templatePageNumber - 1, size: 10, search: templateSearch || undefined, certificateType: templateType || undefined, active: templateActive === '' ? undefined : templateActive === 'true' }),
          certificateApi.getSettings(),
          eventApi.listEvents({ page: 0, size: 50 }),
          userApi.listUsers()
        ]);
        if (!active) {
          return;
        }
        setDashboard(unwrap(dashboardResponse));
        setCertificatesPage(unwrapPage(certificatesResponse));
        setTemplatesPage(unwrapPage(templatesResponse));
        setSettings(unwrap(settingsResponse));
        setEvents(unwrapPage(eventsResponse).content);
        setUsers(unwrap(usersResponse) ?? []);
      } catch (error) {
        if (!active) {
          return;
        }
        const message = getApiErrorMessage(error, 'CampusSphere certificate services are currently unavailable.');
        setDashboardError(message);
        setCertificatesError(message);
        setTemplatesError(message);
        setSettingsError(message);
      } finally {
        if (active) {
          setDashboardLoading(false);
          setCertificatesLoading(false);
          setTemplatesLoading(false);
          setSettingsLoading(false);
        }
      }
    }

    loadInitialData();

    return () => {
      active = false;
    };
  }, [certificatePageNumber, certificateSearch, certificateStatus, certificateType, templateActive, templatePageNumber, templateSearch, templateType]);

  const summaryCards = dashboard ? [
    { label: 'Generated', value: dashboard.certificatesGenerated, detail: 'Issued certificates in this institution.', tone: 'success' },
    { label: 'Pending', value: dashboard.pending, detail: 'Drafts waiting for final review.', tone: 'warning' },
    { label: 'Eligible', value: dashboard.eligibleRecipients, detail: 'Participants who can receive certificates.', tone: 'neutral' },
    { label: 'Revoked', value: dashboard.revoked, detail: 'Certificates removed from active verification.', tone: 'danger' }
  ] : [];

  async function handleTemplateSubmit(event) {
    event.preventDefault();
    setTemplateActionLoading(true);
    setTemplateErrors([]);
    try {
      const payload = {
        ...templateForm,
        marginTopMm: templateForm.marginTopMm === '' ? null : Number(templateForm.marginTopMm),
        marginRightMm: templateForm.marginRightMm === '' ? null : Number(templateForm.marginRightMm),
        marginBottomMm: templateForm.marginBottomMm === '' ? null : Number(templateForm.marginBottomMm),
        marginLeftMm: templateForm.marginLeftMm === '' ? null : Number(templateForm.marginLeftMm)
      };
      if (editingTemplateId) {
        await certificateApi.updateTemplate(editingTemplateId, payload);
        pushToast('Certificate template updated successfully.', 'success');
      } else {
        await certificateApi.createTemplate(payload);
        pushToast('Certificate template created successfully.', 'success');
      }
      setTemplateModalOpen(false);
      setEditingTemplateId(null);
      setTemplateForm(emptyTemplateForm);
    } catch (error) {
      setTemplateErrors(extractValidationMessages(error));
      pushToast(getApiErrorMessage(error, 'Unable to save the certificate template.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setTemplateActionLoading(false);
    }
  }

  async function openTemplateModal(template = null) {
    if (template) {
      setEditingTemplateId(template.id);
      setTemplateForm({
        templateCode: template.templateCode ?? '',
        templateName: template.templateName ?? '',
        certificateType: template.certificateType ?? 'PARTICIPATION',
        orientation: template.orientation ?? 'PORTRAIT',
        description: template.description ?? '',
        institutionLogoUrl: template.institutionLogoUrl ?? '',
        organizerLogoUrl: template.organizerLogoUrl ?? '',
        backgroundImageUrl: template.backgroundImageUrl ?? '',
        signatureLeftUrl: template.signatureLeftUrl ?? '',
        signatureRightUrl: template.signatureRightUrl ?? '',
        sealUrl: template.sealUrl ?? '',
        primaryColor: template.primaryColor ?? '#0f172a',
        accentColor: template.accentColor ?? '#4338ca',
        watermarkText: template.watermarkText ?? '',
        marginTopMm: template.marginTopMm ?? 12,
        marginRightMm: template.marginRightMm ?? 12,
        marginBottomMm: template.marginBottomMm ?? 12,
        marginLeftMm: template.marginLeftMm ?? 12,
        qrCodeEnabled: template.qrCodeEnabled ?? true,
        verificationUrlBase: template.verificationUrlBase ?? '',
        templateHtml: template.templateHtml ?? '',
        variablesJson: template.variablesJson ?? '',
        active: template.active ?? true
      });
    } else {
      setEditingTemplateId(null);
      setTemplateForm(emptyTemplateForm);
    }
    setTemplateErrors([]);
    setTemplateModalOpen(true);
  }

  async function handleTemplatePreview() {
    if (!editingTemplateId) {
      pushToast('Save the template first to preview it from the backend.', 'warning');
      return;
    }
    try {
      const response = await certificateApi.previewTemplate(editingTemplateId);
      setTemplatePreview(unwrap(response));
      pushToast('Template preview prepared.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to preview the certificate template.'), 'danger');
    }
  }

  async function handleToggleTemplate(template) {
    try {
      await certificateApi.toggleTemplateStatus(template.id, !template.active);
      pushToast(`Template ${template.active ? 'deactivated' : 'activated'} successfully.`, 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update template status.'), 'danger');
    }
  }

  async function handleDeleteTemplate(template) {
    try {
      await certificateApi.deleteTemplate(template.id);
      pushToast('Template removed successfully.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to delete the template.'), 'danger');
    }
  }

  async function handleIssueSubmit(event) {
    event.preventDefault();
    setIssueActionLoading(true);
    setIssueErrors([]);
    try {
      const payload = {
        eventId: Number(issueForm.eventId),
        sessionId: issueForm.sessionId ? Number(issueForm.sessionId) : null,
        academicYearId: issueForm.academicYearId ? Number(issueForm.academicYearId) : null,
        recipientUserId: Number(issueForm.recipientUserId),
        certificateType: issueForm.certificateType,
        templateId: issueForm.templateId ? Number(issueForm.templateId) : null,
        recipientRole: issueForm.recipientRole,
        position: issueForm.position,
        prize: issueForm.prize,
        attendancePercentage: issueForm.attendancePercentage === '' ? null : Number(issueForm.attendancePercentage),
        issueDate: issueForm.issueDate || null,
        adminOverride: Boolean(issueForm.adminOverride),
        remarks: issueForm.remarks
      };
      const response = await certificateApi.issueCertificate(payload);
      pushToast('Certificate issued successfully.', 'success');
      setIssuePreview(unwrap(response));
      setIssueModalOpen(false);
      setIssueForm(emptyIssueForm);
    } catch (error) {
      setIssueErrors(extractValidationMessages(error));
      pushToast(getApiErrorMessage(error, 'Unable to issue the certificate.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setIssueActionLoading(false);
    }
  }

  async function handleBulkIssueSubmit(event) {
    event.preventDefault();
    setBulkActionLoading(true);
    setBulkErrors([]);
    try {
      const recipientUserIds = bulkForm.recipientUserIds
        .split(',')
        .map(item => item.trim())
        .filter(Boolean)
        .map(item => Number(item));
      await certificateApi.issueBulkCertificates({
        eventId: Number(bulkForm.eventId),
        certificateType: bulkForm.certificateType,
        templateId: bulkForm.templateId ? Number(bulkForm.templateId) : null,
        recipientUserIds,
        recipientRole: bulkForm.recipientRole,
        position: bulkForm.position,
        prize: bulkForm.prize,
        attendancePercentage: bulkForm.attendancePercentage === '' ? null : Number(bulkForm.attendancePercentage),
        issueDate: bulkForm.issueDate || null,
        adminOverride: Boolean(bulkForm.adminOverride),
        remarks: bulkForm.remarks
      });
      pushToast('Bulk certificate generation completed.', 'success');
      setBulkModalOpen(false);
    } catch (error) {
      setBulkErrors(extractValidationMessages(error));
      pushToast(getApiErrorMessage(error, 'Unable to generate certificates in bulk.'), isNetworkError(error) ? 'warning' : 'danger');
    } finally {
      setBulkActionLoading(false);
    }
  }

  async function handlePreviewIssue() {
    try {
      const response = await certificateApi.previewCertificate({
        eventId: Number(issueForm.eventId),
        sessionId: issueForm.sessionId ? Number(issueForm.sessionId) : null,
        academicYearId: issueForm.academicYearId ? Number(issueForm.academicYearId) : null,
        recipientUserId: Number(issueForm.recipientUserId),
        certificateType: issueForm.certificateType,
        templateId: issueForm.templateId ? Number(issueForm.templateId) : null,
        recipientRole: issueForm.recipientRole,
        position: issueForm.position,
        prize: issueForm.prize,
        attendancePercentage: issueForm.attendancePercentage === '' ? null : Number(issueForm.attendancePercentage),
        issueDate: issueForm.issueDate || null,
        adminOverride: Boolean(issueForm.adminOverride),
        remarks: issueForm.remarks
      });
      setIssuePreview(unwrap(response));
      pushToast('Certificate preview ready.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to prepare the preview.'), 'danger');
    }
  }

  async function handleDownload(certificate) {
    try {
      const response = await certificateApi.downloadCertificate(certificate.id);
      downloadBlob(response.data, `${certificate.certificateNumber || 'certificate'}.pdf`);
      pushToast('Certificate download started.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to download the certificate.'), isNetworkError(error) ? 'warning' : 'danger');
    }
  }

  async function handleRegenerate(certificate) {
    try {
      await certificateApi.regenerateCertificate(certificate.id);
      pushToast('Certificate regenerated successfully.', 'success');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to regenerate the certificate.'), 'danger');
    }
  }

  async function handleRevokeSubmit(event) {
    event.preventDefault();
    if (!selectedCertificate) {
      return;
    }
    setRevokeLoading(true);
    try {
      await certificateApi.revokeCertificate(selectedCertificate.id, { reason: revokeReason });
      pushToast('Certificate revoked successfully.', 'success');
      setRevokeDialogOpen(false);
      setRevokeReason('');
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to revoke the certificate.'), 'danger');
    } finally {
      setRevokeLoading(false);
    }
  }

  const selectedTabContent = (
    <>
      {activeTab === 'overview' && (
        <div className="page-stack">
          <div className="certificate-dashboard">
            {summaryCards.map(card => (
              <Card key={card.label} elevated className="metric-card">
                <span className="metric-card__label">{card.label}</span>
                <strong className="metric-card__value">{card.value}</strong>
                <p className="metric-card__detail">{card.detail}</p>
              </Card>
            ))}
          </div>

          <div className="dashboard-grid dashboard-grid--split">
            <Card elevated className="dashboard-panel">
              <div className="section-heading section-heading--compact">
                <div className="section-heading__copy">
                  <h3>Recent certificates</h3>
                  <p>The latest issued certificates and their verification state.</p>
                </div>
                <Button variant="secondary" size="sm" onClick={() => goToTab('generated')}>View all</Button>
              </div>
              {certificatesLoading ? (
                <LoadingSkeleton lines={5} />
              ) : certificatesError ? (
                <ErrorState title="Certificates unavailable" description={certificatesError} onRetry={() => window.location.reload()} />
              ) : certificatesPage.content.length ? (
                <Table
                  rows={certificatesPage.content}
                  emptyMessage="No certificates found."
                  columns={[
                    { key: 'certificateNumber', header: 'Number', render: row => <strong>{row.certificateNumber}</strong> },
                    { key: 'recipientName', header: 'Recipient', render: row => row.recipientName },
                    { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                    { key: 'type', header: 'Type', render: row => titleCase(row.certificateType) },
                    { key: 'status', header: 'Status', render: row => <Badge tone={toneForCertificate(row.certificateStatus)}>{titleCase(row.certificateStatus)}</Badge> }
                  ]}
                />
              ) : (
                <EmptyState title="No certificates generated yet" description="Issue the first certificate once an event is ready for recognition." actionLabel="Issue certificate" onAction={() => setIssueModalOpen(true)} />
              )}
            </Card>

            <Card elevated className="dashboard-panel">
              <div className="section-heading section-heading--compact">
                <div className="section-heading__copy">
                  <h3>Active templates</h3>
                  <p>Reusable layouts for participation and role-based certificates.</p>
                </div>
                <Button variant="secondary" size="sm" onClick={() => openTemplateModal()}>New template</Button>
              </div>
              {templatesLoading ? (
                <LoadingSkeleton lines={5} />
              ) : templatesError ? (
                <ErrorState title="Templates unavailable" description={templatesError} onRetry={() => window.location.reload()} />
              ) : templatesPage.content.length ? (
                <Table
                  rows={templatesPage.content}
                  emptyMessage="No templates found."
                  columns={[
                    { key: 'templateName', header: 'Template', render: row => <strong>{row.templateName}</strong> },
                    { key: 'certificateType', header: 'Type', render: row => titleCase(row.certificateType) },
                    { key: 'orientation', header: 'Orientation', render: row => titleCase(row.orientation) },
                    { key: 'active', header: 'State', render: row => <Badge tone={toneForTemplate(row.active)}>{row.active ? 'Active' : 'Inactive'}</Badge> }
                  ]}
                />
              ) : (
                <EmptyState title="No templates yet" description="Create a template before issuing certificates." actionLabel="Create template" onAction={() => openTemplateModal()} />
              )}
            </Card>
          </div>
        </div>
      )}

      {activeTab === 'templates' && (
        <div className="page-stack">
          <SectionHeading
            eyebrow="Templates"
            title="Certificate templates"
            description="Design, activate, duplicate, and preview the layouts used for issuance."
            action={<Button onClick={() => openTemplateModal()}>New template</Button>}
          />

          <Card elevated className="workspace-panel">
            <FilterPanel title="Template filters">
              <div className="workspace-filter-grid">
                <SearchBar value={templateSearch} onChange={setTemplateSearch} placeholder="Search templates" />
                <Select label="Type" value={templateType} onChange={event => setTemplateType(event.target.value)}>
                  {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </Select>
                <Select label="State" value={templateActive} onChange={event => setTemplateActive(event.target.value)}>
                  {TEMPLATE_STATUS_OPTIONS.map(([value, label]) => <option key={label} value={value}>{label}</option>)}
                </Select>
              </div>
            </FilterPanel>

            {templatesLoading ? (
              <LoadingSkeleton lines={6} />
            ) : templatesError ? (
              <ErrorState title="Templates unavailable" description={templatesError} onRetry={() => window.location.reload()} />
            ) : templatesPage.content.length ? (
              <>
                <Table
                  rows={templatesPage.content}
                  emptyMessage="No templates match the current filters."
                  columns={[
                    { key: 'templateName', header: 'Template', render: row => <div><strong>{row.templateName}</strong><div className="table__subtext">{row.templateCode}</div></div> },
                    { key: 'certificateType', header: 'Type', render: row => titleCase(row.certificateType) },
                    { key: 'orientation', header: 'Orientation', render: row => titleCase(row.orientation) },
                    { key: 'active', header: 'State', render: row => <Badge tone={toneForTemplate(row.active)}>{row.active ? 'Active' : 'Inactive'}</Badge> },
                    {
                      key: 'actions',
                      header: 'Actions',
                      render: row => (
                        <div className="inline-actions">
                          <Button size="sm" variant="secondary" onClick={() => openTemplateModal(row)}>Edit</Button>
                          <Button size="sm" variant="secondary" onClick={() => handleToggleTemplate(row)}>{row.active ? 'Deactivate' : 'Activate'}</Button>
                          <Button size="sm" variant="secondary" onClick={() => {
                            setEditingTemplateId(null);
                            setTemplateForm({
                              templateCode: `${row.templateCode}-COPY`,
                              templateName: `${row.templateName} Copy`,
                              certificateType: row.certificateType ?? 'PARTICIPATION',
                              orientation: row.orientation ?? 'PORTRAIT',
                              description: row.description ?? '',
                              institutionLogoUrl: row.institutionLogoUrl ?? '',
                              organizerLogoUrl: row.organizerLogoUrl ?? '',
                              backgroundImageUrl: row.backgroundImageUrl ?? '',
                              signatureLeftUrl: row.signatureLeftUrl ?? '',
                              signatureRightUrl: row.signatureRightUrl ?? '',
                              sealUrl: row.sealUrl ?? '',
                              primaryColor: row.primaryColor ?? '#0f172a',
                              accentColor: row.accentColor ?? '#4338ca',
                              watermarkText: row.watermarkText ?? '',
                              marginTopMm: row.marginTopMm ?? 12,
                              marginRightMm: row.marginRightMm ?? 12,
                              marginBottomMm: row.marginBottomMm ?? 12,
                              marginLeftMm: row.marginLeftMm ?? 12,
                              qrCodeEnabled: row.qrCodeEnabled ?? true,
                              verificationUrlBase: row.verificationUrlBase ?? '',
                              templateHtml: row.templateHtml ?? '',
                              variablesJson: row.variablesJson ?? '',
                              active: row.active ?? true
                            });
                            setTemplateErrors([]);
                            setTemplateModalOpen(true);
                          }}>Duplicate</Button>
                          <Button size="sm" variant="secondary" onClick={() => setTemplatePreview({ sampleRender: row.templateName })}>Preview</Button>
                        </div>
                      )
                    }
                  ]}
                />
                <Pagination page={templatesPage.page} totalPages={Math.max(templatesPage.totalPages, 1)} onPageChange={setTemplatePageNumber} />
              </>
            ) : (
              <EmptyState title="No templates found" description="Create the first certificate template for this institution." actionLabel="Create template" onAction={() => openTemplateModal()} />
            )}
          </Card>
        </div>
      )}

      {activeTab === 'generated' && (
        <div className="page-stack">
          <SectionHeading
            eyebrow="Generated"
            title="Issued certificates"
            description="Search issued records, download PDFs, regenerate tokens, or revoke access."
            action={<div className="inline-actions"><Button variant="secondary" onClick={() => setBulkModalOpen(true)}>Bulk generate</Button><Button onClick={() => setIssueModalOpen(true)}>Issue certificate</Button></div>}
          />

          <Card elevated className="workspace-panel">
            <FilterPanel title="Certificate filters">
              <div className="workspace-filter-grid">
                <SearchBar value={certificateSearch} onChange={setCertificateSearch} placeholder="Search by number, recipient, or event" />
                <Select label="Type" value={certificateType} onChange={event => setCertificateType(event.target.value)}>
                  <option value="">All types</option>
                  {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </Select>
                <Select label="Status" value={certificateStatus} onChange={event => setCertificateStatus(event.target.value)}>
                  {STATUS_OPTIONS.map(([value, label]) => <option key={label} value={value}>{label}</option>)}
                </Select>
              </div>
            </FilterPanel>

            {certificatesLoading ? (
              <LoadingSkeleton lines={6} />
            ) : certificatesError ? (
              <ErrorState title="Certificates unavailable" description={certificatesError} onRetry={() => window.location.reload()} />
            ) : certificatesPage.content.length ? (
              <>
                <Table
                  rows={certificatesPage.content}
                  emptyMessage="No certificates match the filters."
                  columns={[
                    { key: 'certificateNumber', header: 'Certificate', render: row => <div><strong>{row.certificateNumber}</strong><div className="table__subtext">{row.recipientName}</div></div> },
                    { key: 'eventTitle', header: 'Event', render: row => row.eventTitle },
                    { key: 'certificateType', header: 'Type', render: row => titleCase(row.certificateType) },
                    { key: 'certificateStatus', header: 'Status', render: row => <Badge tone={toneForCertificate(row.certificateStatus)}>{titleCase(row.certificateStatus)}</Badge> },
                    { key: 'issueDate', header: 'Issued', render: row => formatDateTime(row.issueDate) },
                    {
                      key: 'actions',
                      header: 'Actions',
                      render: row => (
                        <div className="inline-actions">
                          <Button size="sm" variant="secondary" onClick={() => handleDownload(row)}>Download</Button>
                          <Button size="sm" variant="secondary" onClick={() => setSelectedCertificate(row) || setRevokeReason('') || setRevokeDialogOpen(true)} disabled={row.certificateStatus === 'REVOKED'}>Revoke</Button>
                          <Button size="sm" variant="secondary" onClick={() => handleRegenerate(row)}>Regenerate</Button>
                        </div>
                      )
                    }
                  ]}
                />
                <Pagination page={certificatesPage.page} totalPages={Math.max(certificatesPage.totalPages, 1)} onPageChange={setCertificatePageNumber} />
              </>
            ) : (
              <EmptyState title="No certificates found" description="Issue certificates from an approved event to populate this list." actionLabel="Issue certificate" onAction={() => setIssueModalOpen(true)} />
            )}
          </Card>
        </div>
      )}

      {activeTab === 'settings' && (
        <div className="page-stack">
          <SectionHeading
            eyebrow="Settings"
            title="Certificate settings"
            description="Review verification defaults and the variables available to template authors."
            action={<Button variant="secondary" as={Link} to={APP_ROUTES.certificateVerification}>Open verifier</Button>}
          />

          {settingsLoading ? (
            <LoadingSkeleton lines={5} />
          ) : settingsError ? (
            <ErrorState title="Settings unavailable" description={settingsError} onRetry={() => window.location.reload()} />
          ) : settings ? (
            <div className="dashboard-grid dashboard-grid--split">
              <Card elevated className="dashboard-panel">
                <FormSection title="Verification defaults" description="These values shape QR generation and public verification.">
                  <div className="definition-grid">
                    <div><span>Frontend base URL</span><strong>{settings.frontendBaseUrl || 'http://localhost:5173'}</strong></div>
                    <div><span>Verification path</span><strong>{settings.verificationPath}</strong></div>
                    <div><span>QR enabled</span><strong>{settings.qrCodeEnabled ? 'Yes' : 'No'}</strong></div>
                  </div>
                </FormSection>
              </Card>
              <Card elevated className="dashboard-panel">
                <FormSection title="Supported variables" description="Use these placeholders inside certificate templates.">
                  <div className="chip-cloud">
                    {settings.supportedVariables?.map(variable => <Badge key={variable} tone="neutral">{variable}</Badge>)}
                  </div>
                </FormSection>
                <FormSection title="Supported types" description="These certificate categories are currently supported.">
                  <div className="chip-cloud">
                    {settings.supportedCertificateTypes?.map(type => <Badge key={type} tone="neutral">{titleCase(type)}</Badge>)}
                  </div>
                </FormSection>
              </Card>
            </div>
          ) : (
            <EmptyState title="No settings available" description="Certificate settings will appear after the backend publishes the configuration payload." />
          )}
        </div>
      )}
    </>
  );

  return (
    <div className="page-stack certificate-page">
      <SectionHeading
        eyebrow="Certificates"
        title="Certificate management"
        description="Issue, verify, and maintain certificates without leaving the CampusSphere dashboard."
        action={<div className="inline-actions"><Button variant="secondary" as={Link} to={APP_ROUTES.certificateVerification}>Verify token</Button><Button onClick={() => setIssueModalOpen(true)}>Issue certificate</Button></div>}
      />

      <Tabs items={TABS} activeKey={activeTab} onChange={goToTab} />

      {selectedTabContent}

      <Modal
        open={templateModalOpen}
        title={editingTemplateId ? 'Edit template' : 'Create template'}
        description="Keep the template metadata concise and aligned with the certificate type."
        onClose={() => setTemplateModalOpen(false)}
        onSubmit={handleTemplateSubmit}
        submitLabel={editingTemplateId ? 'Update template' : 'Save template'}
        loading={templateActionLoading}
        size="lg"
      >
        <div className="form-grid form-grid--two">
          <FormSection title="Identity" description="Naming and classification details.">
            <div className="form-grid">
              <Input label="Template code" value={templateForm.templateCode} onChange={event => setTemplateForm(current => ({ ...current, templateCode: event.target.value }))} />
              <Input label="Template name" value={templateForm.templateName} onChange={event => setTemplateForm(current => ({ ...current, templateName: event.target.value }))} />
              <Select label="Certificate type" value={templateForm.certificateType} onChange={event => setTemplateForm(current => ({ ...current, certificateType: event.target.value }))}>
                {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Select label="Orientation" value={templateForm.orientation} onChange={event => setTemplateForm(current => ({ ...current, orientation: event.target.value }))}>
                {ORIENTATION_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Textarea label="Description" value={templateForm.description} onChange={event => setTemplateForm(current => ({ ...current, description: event.target.value }))} rows={3} />
            </div>
          </FormSection>
          <FormSection title="Visuals" description="Brand images and accent colours.">
            <div className="form-grid">
              <Input label="Institution logo URL" value={templateForm.institutionLogoUrl} onChange={event => setTemplateForm(current => ({ ...current, institutionLogoUrl: event.target.value }))} />
              <Input label="Organizer logo URL" value={templateForm.organizerLogoUrl} onChange={event => setTemplateForm(current => ({ ...current, organizerLogoUrl: event.target.value }))} />
              <Input label="Background image URL" value={templateForm.backgroundImageUrl} onChange={event => setTemplateForm(current => ({ ...current, backgroundImageUrl: event.target.value }))} />
              <Input label="Signature left URL" value={templateForm.signatureLeftUrl} onChange={event => setTemplateForm(current => ({ ...current, signatureLeftUrl: event.target.value }))} />
              <Input label="Signature right URL" value={templateForm.signatureRightUrl} onChange={event => setTemplateForm(current => ({ ...current, signatureRightUrl: event.target.value }))} />
              <Input label="Seal URL" value={templateForm.sealUrl} onChange={event => setTemplateForm(current => ({ ...current, sealUrl: event.target.value }))} />
              <Input type="color" label="Primary colour" value={templateForm.primaryColor} onChange={event => setTemplateForm(current => ({ ...current, primaryColor: event.target.value }))} />
              <Input type="color" label="Accent colour" value={templateForm.accentColor} onChange={event => setTemplateForm(current => ({ ...current, accentColor: event.target.value }))} />
            </div>
          </FormSection>
          <FormSection title="Layout and verification" description="Margins, QR, and verification settings.">
            <div className="form-grid">
              <Input label="Watermark text" value={templateForm.watermarkText} onChange={event => setTemplateForm(current => ({ ...current, watermarkText: event.target.value }))} />
              <Input type="number" label="Top margin (mm)" value={templateForm.marginTopMm} onChange={event => setTemplateForm(current => ({ ...current, marginTopMm: event.target.value }))} />
              <Input type="number" label="Right margin (mm)" value={templateForm.marginRightMm} onChange={event => setTemplateForm(current => ({ ...current, marginRightMm: event.target.value }))} />
              <Input type="number" label="Bottom margin (mm)" value={templateForm.marginBottomMm} onChange={event => setTemplateForm(current => ({ ...current, marginBottomMm: event.target.value }))} />
              <Input type="number" label="Left margin (mm)" value={templateForm.marginLeftMm} onChange={event => setTemplateForm(current => ({ ...current, marginLeftMm: event.target.value }))} />
              <Input label="Verification base URL" value={templateForm.verificationUrlBase} onChange={event => setTemplateForm(current => ({ ...current, verificationUrlBase: event.target.value }))} />
              <Textarea label="Template HTML" value={templateForm.templateHtml} onChange={event => setTemplateForm(current => ({ ...current, templateHtml: event.target.value }))} rows={4} />
              <Textarea label="Variables JSON" value={templateForm.variablesJson} onChange={event => setTemplateForm(current => ({ ...current, variablesJson: event.target.value }))} rows={4} />
            </div>
          </FormSection>
          <label className="field">
            <span className="field__label">Active</span>
            <select className="select" value={String(templateForm.active)} onChange={event => setTemplateForm(current => ({ ...current, active: event.target.value === 'true' }))}>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </label>
        </div>
        <div className="dialog__subactions">
          <Button type="button" variant="secondary" onClick={handleTemplatePreview} disabled={!editingTemplateId}>Preview current template</Button>
        </div>
        {templatePreview && (
          <Card className="preview-card">
            <strong>Preview</strong>
            <p>{templatePreview.sampleRender}</p>
          </Card>
        )}
        {templateErrors.length > 0 && <div className="validation-list">{templateErrors.map(message => <div key={message}>{message}</div>)}</div>}
      </Modal>

      <Modal
        open={issueModalOpen}
        title="Issue certificate"
        description="Generate a single certificate for a validated participant."
        onClose={() => setIssueModalOpen(false)}
        onSubmit={handleIssueSubmit}
        submitLabel="Issue certificate"
        loading={issueActionLoading}
        size="lg"
      >
        <div className="form-grid form-grid--two">
          <FormSection title="Recipient" description="Select the event and person who should receive the certificate.">
            <div className="form-grid">
              <Select label="Event" value={issueForm.eventId} onChange={event => setIssueForm(current => ({ ...current, eventId: event.target.value }))}>
                <option value="">Select event</option>
                {eventOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
              </Select>
              <Select label="Recipient" value={issueForm.recipientUserId} onChange={event => setIssueForm(current => ({ ...current, recipientUserId: event.target.value }))}>
                <option value="">Select user</option>
                {userOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
              </Select>
              <Select label="Certificate type" value={issueForm.certificateType} onChange={event => setIssueForm(current => ({ ...current, certificateType: event.target.value }))}>
                {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Select label="Template" value={issueForm.templateId} onChange={event => setIssueForm(current => ({ ...current, templateId: event.target.value }))}>
                <option value="">Auto-select default</option>
                {templateOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
              </Select>
              <Input label="Recipient role" value={issueForm.recipientRole} onChange={event => setIssueForm(current => ({ ...current, recipientRole: event.target.value }))} />
            </div>
          </FormSection>
          <FormSection title="Qualification" description="Optional metadata used for winners and role-based certificates.">
            <div className="form-grid">
              <Select label="Event session" value={issueForm.sessionId} onChange={event => setIssueForm(current => ({ ...current, sessionId: event.target.value }))}>
                <option value="">No session</option>
              </Select>
              <Input label="Academic year ID" value={issueForm.academicYearId} onChange={event => setIssueForm(current => ({ ...current, academicYearId: event.target.value }))} />
              <Input label="Position" value={issueForm.position} onChange={event => setIssueForm(current => ({ ...current, position: event.target.value }))} />
              <Input label="Prize" value={issueForm.prize} onChange={event => setIssueForm(current => ({ ...current, prize: event.target.value }))} />
              <Input type="number" label="Attendance percentage" value={issueForm.attendancePercentage} onChange={event => setIssueForm(current => ({ ...current, attendancePercentage: event.target.value }))} />
              <Input type="datetime-local" label="Issue date" value={issueForm.issueDate} onChange={event => setIssueForm(current => ({ ...current, issueDate: event.target.value }))} />
              <Input label="Remarks" value={issueForm.remarks} onChange={event => setIssueForm(current => ({ ...current, remarks: event.target.value }))} />
            </div>
          </FormSection>
        </div>
        <div className="dialog__subactions">
          <Button type="button" variant="secondary" onClick={handlePreviewIssue}>Preview certificate</Button>
        </div>
        {issuePreview && (
          <Card className="preview-card">
            <strong>{issuePreview.certificateNumber || 'Preview certificate'}</strong>
            <p>{issuePreview.recipientName} · {issuePreview.eventTitle} · {titleCase(issuePreview.certificateType)}</p>
          </Card>
        )}
        {issueErrors.length > 0 && <div className="validation-list">{issueErrors.map(message => <div key={message}>{message}</div>)}</div>}
      </Modal>

      <Modal
        open={bulkModalOpen}
        title="Bulk generate certificates"
        description="Create certificates for multiple recipients in a single action."
        onClose={() => setBulkModalOpen(false)}
        onSubmit={handleBulkIssueSubmit}
        submitLabel="Generate certificates"
        loading={bulkActionLoading}
        size="lg"
      >
        <div className="form-grid form-grid--two">
          <FormSection title="Bulk request" description="Provide recipient IDs as a comma-separated list.">
            <div className="form-grid">
              <Select label="Event" value={bulkForm.eventId} onChange={event => setBulkForm(current => ({ ...current, eventId: event.target.value }))}>
                <option value="">Select event</option>
                {eventOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
              </Select>
              <Select label="Certificate type" value={bulkForm.certificateType} onChange={event => setBulkForm(current => ({ ...current, certificateType: event.target.value }))}>
                {CERTIFICATE_TYPE_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </Select>
              <Select label="Template" value={bulkForm.templateId} onChange={event => setBulkForm(current => ({ ...current, templateId: event.target.value }))}>
                <option value="">Auto-select default</option>
                {templateOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
              </Select>
              <Textarea label="Recipient user IDs" value={bulkForm.recipientUserIds} onChange={event => setBulkForm(current => ({ ...current, recipientUserIds: event.target.value }))} rows={4} helperText="Example: 11, 12, 13" />
            </div>
          </FormSection>
          <FormSection title="Optional metadata" description="Shared values applied to all generated certificates.">
            <div className="form-grid">
              <Input label="Recipient role" value={bulkForm.recipientRole} onChange={event => setBulkForm(current => ({ ...current, recipientRole: event.target.value }))} />
              <Input label="Position" value={bulkForm.position} onChange={event => setBulkForm(current => ({ ...current, position: event.target.value }))} />
              <Input label="Prize" value={bulkForm.prize} onChange={event => setBulkForm(current => ({ ...current, prize: event.target.value }))} />
              <Input type="number" label="Attendance percentage" value={bulkForm.attendancePercentage} onChange={event => setBulkForm(current => ({ ...current, attendancePercentage: event.target.value }))} />
              <Input type="datetime-local" label="Issue date" value={bulkForm.issueDate} onChange={event => setBulkForm(current => ({ ...current, issueDate: event.target.value }))} />
              <Input label="Remarks" value={bulkForm.remarks} onChange={event => setBulkForm(current => ({ ...current, remarks: event.target.value }))} />
            </div>
          </FormSection>
        </div>
        {bulkErrors.length > 0 && <div className="validation-list">{bulkErrors.map(message => <div key={message}>{message}</div>)}</div>}
      </Modal>

      <Modal
        open={revokeDialogOpen}
        title="Revoke certificate"
        description={`This will revoke ${selectedCertificate?.certificateNumber ?? 'the selected certificate'} and stop public verification.`}
        onClose={() => setRevokeDialogOpen(false)}
        onSubmit={handleRevokeSubmit}
        submitLabel={revokeLoading ? 'Revoking...' : 'Revoke certificate'}
        loading={revokeLoading}
      >
        <Input label="Reason" value={revokeReason} onChange={event => setRevokeReason(event.target.value)} />
      </Modal>

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
