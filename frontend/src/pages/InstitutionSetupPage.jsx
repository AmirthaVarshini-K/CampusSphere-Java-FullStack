import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { ErrorBanner } from '../components/Banner';
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
import RoleBadge from '../components/RoleBadge';
import SearchBar from '../components/SearchBar';
import Select from '../components/Select';
import Table from '../components/Table';
import Textarea from '../components/Textarea';
import StatusIndicator from '../components/StatusIndicator';
import ValidationMessages from '../components/ValidationMessages';
import { Toast } from '../components/Toast';
import { APP_ROUTES } from '../constants/routes';
import { MASTER_DATA_RESOURCES, MASTER_DATA_TABS, getResourceByPath, mapAcademicYears, mapDepartments, mapMappings, mapInstitutions, mapProgrammes, mapSemesters } from './institution-setup/masterDataResources';
import { useAuth } from '../context/AuthContext';
import useToastQueue from '../hooks/useToastQueue';
import { masterDataApi } from '../services/masterDataApi';
import { classNames } from '../utils/classNames';
import { extractValidationMessages, getApiErrorMessage, isNetworkError } from '../utils/apiErrors';
import { buildDisplayName, getPrimaryRole, getRoleLabel } from '../utils/auth';

const PAGE_SIZE_OPTIONS = [10, 20, 50];
const CATALOG_PAGE_SIZE = 200;
const WORKSPACE_ROLES = new Set(['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR']);
const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function createDefaultFilters(resource) {
  const defaults = { page: 1, size: 10 };

  (resource?.filters ?? []).forEach(filter => {
    defaults[filter.name] = '';
  });

  return defaults;
}

function unwrapData(response) {
  return response?.data?.data ?? null;
}

function unwrapPage(response) {
  const data = unwrapData(response);
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

function formatDate(value) {
  if (!value) {
    return '-';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return String(value);
  }

  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  }).format(parsed);
}

function formatStatus(active) {
  if (active === true || active === 'true') {
    return 'Active';
  }

  if (active === false || active === 'false') {
    return 'Inactive';
  }

  return 'Unknown';
}

function activeTone(active) {
  return active ? 'success' : 'neutral';
}

function parseFieldErrors(error) {
  const errors = error?.response?.data?.errors;
  if (!errors || typeof errors !== 'object') {
    return {};
  }

  return Object.fromEntries(
    Object.entries(errors).map(([field, messages]) => [
      field,
      Array.isArray(messages) ? messages[0] : String(messages)
    ])
  );
}

function normalizeText(value) {
  return typeof value === 'string' ? value.trim() : value;
}

function isPositiveInteger(value) {
  if (value === '' || value === null || value === undefined) {
    return false;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0;
}

function findRecord(rows = [], id) {
  return rows.find(row => String(row.id) === String(id));
}

function uniqueById(rows = []) {
  const seen = new Set();
  return rows.filter(row => {
    const key = String(row.id);
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

function getVisibleInstitutionOptions(catalogs) {
  return mapInstitutions(catalogs.institutions);
}

function getDefaultInstitutionId(scopeInstitutionId, institutionOptions) {
  if (scopeInstitutionId) {
    return scopeInstitutionId;
  }

  return institutionOptions.length === 1 ? institutionOptions[0].value : '';
}

function buildResolvedFormValues(resource, row, defaultInstitutionId) {
  const values = { ...(resource?.defaultValues ?? {}) };

  if (row) {
    Object.assign(values, resource.rowToForm ? resource.rowToForm(row) : row);
  }

  if (resource?.key !== 'institution' && defaultInstitutionId && !values.institutionId) {
    values.institutionId = defaultInstitutionId;
  }

  return values;
}

function filterByInstitution(rows = [], institutionId) {
  if (!institutionId) {
    return rows;
  }

  return rows.filter(row => String(row.institutionId ?? row.institution?.id ?? '') === String(institutionId));
}

function fieldLabel(resource, fieldName) {
  return resource?.fields?.find(field => field.name === fieldName)?.label ?? fieldName;
}

function getSelectOptions({ resource, field, catalogs, formValues, filters, isAdmin, hiddenInstitutionField }) {
  if (Array.isArray(field.options)) {
    return field.options;
  }

  const institutionScopeId = formValues?.institutionId || filters?.institutionId || '';
  const institutionOptions = getVisibleInstitutionOptions(catalogs);

  switch (field.optionsKey) {
    case 'institutions':
      return institutionOptions;
    case 'departments':
      return mapDepartments(filterByInstitution(catalogs.departments, institutionScopeId));
    case 'programmes':
      return mapProgrammes(filterByInstitution(catalogs.programmes, institutionScopeId));
    case 'academicYears':
      return mapAcademicYears(filterByInstitution(catalogs.academicYears, institutionScopeId));
    case 'semesters':
      return mapSemesters(filterByInstitution(catalogs.semesters, institutionScopeId));
    case 'sectionProgrammes': {
      const mappingRows = filterByInstitution(catalogs.mappings, institutionScopeId);
      const selectedDepartmentId = String(formValues?.departmentId ?? '');
      const allowedProgrammeIds = selectedDepartmentId
        ? new Set(mappingRows.filter(mapping => String(mapping.departmentId) === selectedDepartmentId).map(mapping => String(mapping.programmeId)))
        : new Set(mappingRows.map(mapping => String(mapping.programmeId)));
      const matchedProgrammes = catalogs.programmes.filter(programme => allowedProgrammeIds.has(String(programme.id)));
      return mapProgrammes(matchedProgrammes.length > 0 ? matchedProgrammes : filterByInstitution(catalogs.programmes, institutionScopeId));
    }
    case 'sectionSemesters': {
      const selectedProgrammeId = String(formValues?.programmeId ?? '');
      const semesters = filterByInstitution(catalogs.semesters, institutionScopeId);
      return mapSemesters(
        selectedProgrammeId
          ? semesters.filter(semester => String(semester.programmeId) === selectedProgrammeId)
          : semesters
      );
    }
    default:
      return [];
  }
}

function buildPreviewStats(counts = {}) {
  const ordered = [
    ['institution', 'Institution'],
    ['departments', 'Departments'],
    ['academicYears', 'Academic years'],
    ['programmes', 'Programmes'],
    ['mappings', 'Mappings'],
    ['semesters', 'Semesters'],
    ['sections', 'Sections']
  ];

  return ordered.map(([key, label]) => ({
    key,
    label,
    value: counts[key] ?? 0
  }));
}

function validateResourceForm(resource, values, catalogs, defaultInstitutionId) {
  const messages = [];
  const fieldErrors = {};
  const institutionId = normalizeText(values.institutionId || defaultInstitutionId || '');
  const institutionScope = institutionId || '';

  const addError = (field, message) => {
    if (!fieldErrors[field]) {
      fieldErrors[field] = message;
    }
    if (!messages.includes(message)) {
      messages.push(message);
    }
  };

  const isVisibleInstitution = resource.key !== 'institution';

  for (const field of resource.fields) {
    if (field.name === 'institutionId' && !isVisibleInstitution) {
      continue;
    }

    const rawValue = values[field.name];
    const stringValue = typeof rawValue === 'string' ? rawValue.trim() : rawValue;

    if (field.required) {
      const empty = stringValue === '' || stringValue === null || stringValue === undefined || stringValue === false;
      if (empty) {
        addError(field.name, `${field.label} is required.`);
        continue;
      }
    }

    if (stringValue === '' || stringValue === null || stringValue === undefined) {
      continue;
    }

    if (field.type === 'email' && !EMAIL_PATTERN.test(String(stringValue))) {
      addError(field.name, `Enter a valid ${field.label.toLowerCase()}.`);
    }

    if (field.type === 'url') {
      try {
        // eslint-disable-next-line no-new
        new URL(String(stringValue));
      } catch {
        addError(field.name, `Enter a valid ${field.label.toLowerCase()}.`);
      }
    }

    if (field.type === 'number') {
      const numeric = Number(stringValue);
      if (Number.isNaN(numeric)) {
        addError(field.name, `${field.label} must be a number.`);
        continue;
      }
      if (field.min !== undefined && numeric < field.min) {
        addError(field.name, `${field.label} must be at least ${field.min}.`);
      }
      if (field.max !== undefined && numeric > field.max) {
        addError(field.name, `${field.label} must be at most ${field.max}.`);
      }
    }
  }

  if (resource.key === 'institution') {
    if (values.email && !EMAIL_PATTERN.test(values.email.trim())) {
      addError('email', 'Enter a valid official email address.');
    }

    if (values.institutionCode && values.institutionCode.trim().length < 2) {
      addError('institutionCode', 'Institution code should be at least 2 characters long.');
    }
  }

  if (resource.key === 'academicYears') {
    const startDate = values.startDate ? new Date(values.startDate) : null;
    const endDate = values.endDate ? new Date(values.endDate) : null;
    if (startDate && endDate && startDate >= endDate) {
      addError('endDate', 'End date must be later than start date.');
    }
  }

  if (resource.key === 'programmes') {
    if (!isPositiveInteger(values.durationYears)) {
      addError('durationYears', 'Duration in years must be a positive whole number.');
    }
    if (!isPositiveInteger(values.durationSemesters)) {
      addError('durationSemesters', 'Duration in semesters must be a positive whole number.');
    }
  }

  if (resource.key === 'semesters') {
    if (!isPositiveInteger(values.semesterNumber)) {
      addError('semesterNumber', 'Semester number must be a positive whole number.');
    }

    const programme = findRecord(catalogs.programmes, values.programmeId);
    if (programme && isPositiveInteger(values.semesterNumber)) {
      const semesterNumber = Number(values.semesterNumber);
      if (semesterNumber > Number(programme.durationSemesters ?? 0)) {
        addError('semesterNumber', `Semester number cannot exceed ${programme.durationSemesters}.`);
      }
    }
  }

  if (resource.key === 'departmentProgrammes') {
    if (institutionScope && values.departmentId && values.programmeId) {
      const department = findRecord(catalogs.departments, values.departmentId);
      const programme = findRecord(catalogs.programmes, values.programmeId);
      if (department && programme && String(department.institutionId) !== String(programme.institutionId)) {
        addError('programmeId', 'Choose a programme that belongs to the same institution as the department.');
      }
    }

    const duplicate = catalogs.mappings.some(mapping =>
      String(mapping.departmentId) === String(values.departmentId) &&
      String(mapping.programmeId) === String(values.programmeId)
    );
    if (duplicate) {
      addError('programmeId', 'That department-programme mapping already exists.');
    }
  }

  if (resource.key === 'sections') {
    if (!isPositiveInteger(values.capacity) && values.capacity !== '') {
      addError('capacity', 'Capacity must be a positive whole number.');
    }
    if (!isPositiveInteger(values.studyYear) && values.studyYear !== '') {
      addError('studyYear', 'Study year must be a positive whole number.');
    }

    const mappingRows = filterByInstitution(catalogs.mappings, institutionScope);
    const allowedProgrammeIds = new Set(
      mappingRows
        .filter(mapping => String(mapping.departmentId) === String(values.departmentId))
        .map(mapping => String(mapping.programmeId))
    );
    if (allowedProgrammeIds.size > 0 && !allowedProgrammeIds.has(String(values.programmeId))) {
      addError('programmeId', 'Choose a programme that is mapped to the selected department.');
    }

    const semester = findRecord(catalogs.semesters, values.semesterId);
    if (semester && String(semester.programmeId) !== String(values.programmeId)) {
      addError('semesterId', 'Choose a semester that belongs to the selected programme.');
    }
  }

  return { messages, fieldErrors };
}

function getResourceActionButtons(resource, row, onEdit, onToggleStatus, onSetCurrent) {
  const buttons = [];

  if (resource.updateApi) {
    buttons.push(
      <Button key="edit" variant="secondary" size="sm" onClick={() => onEdit(row)}>
        Edit
      </Button>
    );
  }

  if (resource.currentApi) {
    buttons.push(
      <Button key="current" variant="secondary" size="sm" onClick={() => onSetCurrent(row)} disabled={Boolean(row.currentYear)}>
        {row.currentYear ? 'Current' : 'Set current'}
      </Button>
    );
  }

  if (resource.statusApi) {
    buttons.push(
      <Button
        key="status"
        variant="secondary"
        size="sm"
        onClick={() => onToggleStatus(row)}
      >
        {row.active ? 'Deactivate' : 'Activate'}
      </Button>
    );
  }

  return <div className="workspace-actions">{buttons}</div>;
}

function WorkspaceSkeleton() {
  return (
    <Card elevated>
      <LoadingSkeleton lines={6} />
    </Card>
  );
}

function NoInstitutionSelected({ onClear }) {
  return (
    <EmptyState
      title="Select an institution to continue"
      description="CampusSphere can scope departments, programmes, academic years, semesters, and sections to one college at a time. Pick an institution to load the related master data."
      actionLabel="Clear filters"
      onAction={onClear}
    />
  );
}

export default function InstitutionSetupPage() {
  const location = useLocation();
  const { user, refreshCurrentUser } = useAuth();
  const { toasts, pushToast } = useToastQueue();
  const roleCode = getPrimaryRole(user);
  const canAccessWorkspace = WORKSPACE_ROLES.has(roleCode);
  const resource = useMemo(() => getResourceByPath(location.pathname), [location.pathname]);
  const isOverview = location.pathname === APP_ROUTES.dashboard + '/institution-setup';
  const currentResource = resource && !isOverview ? resource : null;

  const [filters, setFilters] = useState(() => createDefaultFilters(currentResource));
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [rows, setRows] = useState([]);
  const [pageMeta, setPageMeta] = useState({ page: 1, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [catalogs, setCatalogs] = useState({
    institutions: [],
    departments: [],
    programmes: [],
    academicYears: [],
    semesters: [],
    mappings: []
  });
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [catalogError, setCatalogError] = useState('');
  const [scopeInstitutionId, setScopeInstitutionId] = useState('');
  const [overview, setOverview] = useState(null);
  const [overviewLoading, setOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState('');
  const [formState, setFormState] = useState({
    open: false,
    mode: 'create',
    values: {},
    fieldErrors: {},
    validationMessages: [],
    submitting: false,
    rowId: null
  });
  const [statusDialog, setStatusDialog] = useState({ open: false, row: null, nextActive: false, title: '', description: '', action: 'status' });
  const listRequestRef = useRef(0);
  const catalogRequestRef = useRef(0);
  const overviewRequestRef = useRef(0);
  const initializedRef = useRef(false);

  const institutionOptions = useMemo(() => getVisibleInstitutionOptions(catalogs), [catalogs]);
  const defaultInstitutionId = getDefaultInstitutionId(scopeInstitutionId, institutionOptions);
  const visibleInstitutionField = currentResource ? currentResource.key !== 'institution' && (roleCode === 'ADMINISTRATOR' || roleCode === 'SUPER_ADMIN' || institutionOptions.length !== 1) : false;
  const currentCatalogInstitutionId = formState.open ? (formState.values.institutionId || filters.institutionId || scopeInstitutionId) : (filters.institutionId || scopeInstitutionId);

  useEffect(() => {
    if (!currentResource) {
      setFilters(createDefaultFilters(null));
      setPage(1);
      setRows([]);
      setErrorMessage('');
      return;
    }

    setFilters(prev => {
      const next = createDefaultFilters(currentResource);
      if (currentResource.filters.some(filter => filter.name === 'institutionId')) {
        next.institutionId = prev.institutionId || scopeInstitutionId || '';
      }
      return next;
    });
    setPage(1);
    setPageSize(10);
    setErrorMessage('');
    setFormState(current => ({
      ...current,
      open: false,
      fieldErrors: {},
      validationMessages: [],
      submitting: false
    }));
    setStatusDialog({ open: false, row: null, nextActive: false, title: '', description: '', action: 'status' });
  }, [currentResource?.key]);

  useEffect(() => {
    if (!currentResource) {
      return;
    }

    const institutionFilterVisible = currentResource.filters.some(filter => filter.name === 'institutionId');
    if (institutionFilterVisible && filters.institutionId !== scopeInstitutionId) {
      setFilters(prev => ({ ...prev, institutionId: scopeInstitutionId, page: 1 }));
      setPage(1);
    }
  }, [currentResource, scopeInstitutionId, filters.institutionId]);

  useEffect(() => {
    let cancelled = false;
    const requestId = ++catalogRequestRef.current;

    async function loadCatalogs() {
      setCatalogLoading(true);
      setCatalogError('');
      try {
        const [institutionsResponse, departmentsResponse, programmesResponse, academicYearsResponse, semestersResponse, mappingsResponse] = await Promise.all([
          masterDataApi.listInstitutions({ page: 0, size: CATALOG_PAGE_SIZE }),
          currentCatalogInstitutionId ? masterDataApi.listDepartments({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
          currentCatalogInstitutionId ? masterDataApi.listProgrammes({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
          currentCatalogInstitutionId ? masterDataApi.listAcademicYears({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
          currentCatalogInstitutionId ? masterDataApi.listSemesters({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
          currentCatalogInstitutionId ? masterDataApi.listDepartmentProgrammes({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null)
        ]);

        if (cancelled || requestId !== catalogRequestRef.current) {
          return;
        }

        const departments = departmentsResponse ? unwrapPage(departmentsResponse).content : [];
        const programmes = programmesResponse ? unwrapPage(programmesResponse).content : [];
        const academicYears = academicYearsResponse ? unwrapPage(academicYearsResponse).content : [];
        const semesters = semestersResponse ? unwrapPage(semestersResponse).content : [];
        const mappings = mappingsResponse ? unwrapPage(mappingsResponse).content : [];

        setCatalogs({
          institutions: uniqueById(unwrapPage(institutionsResponse).content),
          departments,
          programmes,
          academicYears,
          semesters,
          mappings
        });

        if (!initializedRef.current) {
          initializedRef.current = true;
        }

        if (!scopeInstitutionId && !visibleInstitutionField && institutionsResponse) {
          const scopedId = unwrapPage(institutionsResponse).content[0]?.id ? String(unwrapPage(institutionsResponse).content[0].id) : '';
          if (scopedId) {
            setScopeInstitutionId(scopedId);
          }
        }
      } catch (error) {
        if (!cancelled && requestId === catalogRequestRef.current) {
          setCatalogError(getApiErrorMessage(error, 'Unable to load master-data options right now.'));
        }
      } finally {
        if (!cancelled && requestId === catalogRequestRef.current) {
          setCatalogLoading(false);
        }
      }
    }

    loadCatalogs();

    return () => {
      cancelled = true;
    };
  }, [currentCatalogInstitutionId, visibleInstitutionField, scopeInstitutionId]);

  useEffect(() => {
    if (!currentResource || isOverview) {
      return;
    }

    let cancelled = false;
    const requestId = ++listRequestRef.current;

    async function loadList() {
      setLoading(true);
      setErrorMessage('');
      try {
        const requestFilters = {
          ...filters,
          page: page - 1,
          size: pageSize
        };
        const params = currentResource.listQuery(requestFilters);
        const response = await masterDataApi[currentResource.listApi](params);
        const payload = unwrapPage(response);
        if (cancelled || requestId !== listRequestRef.current) {
          return;
        }
        setRows(payload.content);
        setPageMeta(payload);
      } catch (error) {
        if (cancelled || requestId !== listRequestRef.current) {
          return;
        }
        setRows([]);
        setPageMeta({ page, size: pageSize, totalElements: 0, totalPages: 0, first: true, last: true });
        setErrorMessage(getApiErrorMessage(error, 'Unable to load this workspace right now.'));
      } finally {
        if (!cancelled && requestId === listRequestRef.current) {
          setLoading(false);
        }
      }
    }

    loadList();

    return () => {
      cancelled = true;
    };
  }, [currentResource?.key, filters, page, pageSize, isOverview]);

  useEffect(() => {
    if (!isOverview) {
      return;
    }

    let cancelled = false;
    const requestId = ++overviewRequestRef.current;

    async function loadOverview() {
      setOverviewLoading(true);
      setOverviewError('');

      if (!scopeInstitutionId) {
        setOverview(null);
        setOverviewLoading(false);
        return;
      }

      try {
        const [institutionResponse, departmentsResponse, academicYearsResponse, programmesResponse, mappingsResponse, semestersResponse, sectionsResponse] = await Promise.all([
          masterDataApi.getInstitution(scopeInstitutionId),
          masterDataApi.listDepartments({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
          masterDataApi.listAcademicYears({ institutionId: scopeInstitutionId, page: 0, size: 20 }),
          masterDataApi.listProgrammes({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
          masterDataApi.listDepartmentProgrammes({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
          masterDataApi.listSemesters({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
          masterDataApi.listSections({ institutionId: scopeInstitutionId, page: 0, size: 1 })
        ]);

        if (cancelled || requestId !== overviewRequestRef.current) {
          return;
        }

        const institution = unwrapData(institutionResponse);
        const departmentsPage = unwrapPage(departmentsResponse);
        const academicYearPage = unwrapPage(academicYearsResponse);
        const programmePage = unwrapPage(programmesResponse);
        const mappingPage = unwrapPage(mappingsResponse);
        const semesterPage = unwrapPage(semestersResponse);
        const sectionPage = unwrapPage(sectionsResponse);
        const academicYears = academicYearPage.content;
        const currentAcademicYear = academicYears.find(year => year.currentYear) ?? null;

        const counts = {
          institution: institution ? 1 : 0,
          departments: departmentsPage.totalElements,
          academicYears: academicYearPage.totalElements,
          programmes: programmePage.totalElements,
          mappings: mappingPage.totalElements,
          semesters: semesterPage.totalElements,
          sections: sectionPage.totalElements
        };

        setOverview({
          institution,
          counts,
          currentAcademicYear
        });
      } catch (error) {
        if (!cancelled && requestId === overviewRequestRef.current) {
          setOverviewError(getApiErrorMessage(error, 'Unable to load the institutional overview right now.'));
        }
      } finally {
        if (!cancelled && requestId === overviewRequestRef.current) {
          setOverviewLoading(false);
        }
      }
    }

    loadOverview();

    return () => {
      cancelled = true;
    };
  }, [isOverview, scopeInstitutionId]);

  const currentPageMeta = useMemo(() => pageMeta, [pageMeta]);
  const overviewStats = useMemo(() => buildPreviewStats(overview?.counts), [overview]);
  const setupChecklist = useMemo(() => {
    const counts = overview?.counts ?? {};
    return [
      { key: 'institution', label: 'Institution profile', done: counts.institution > 0, note: 'Basic college details are available.' },
      { key: 'departments', label: 'Departments', done: counts.departments > 0, note: 'Department records support academic and event workflows.' },
      { key: 'academicYears', label: 'Academic years', done: counts.academicYears > 0, note: 'At least one academic year is configured.' },
      { key: 'programmes', label: 'Programmes', done: counts.programmes > 0, note: 'Programme definitions are ready for student and faculty workflows.' },
      { key: 'mappings', label: 'Department mappings', done: counts.mappings > 0, note: 'Department-programme relationships are in place.' },
      { key: 'semesters', label: 'Semesters', done: counts.semesters > 0, note: 'Semester slots exist for programme-based forms.' },
      { key: 'sections', label: 'Sections', done: counts.sections > 0, note: 'Section records are ready for downstream modules.' }
    ];
  }, [overview]);

  const currentScopeLabel = useMemo(() => {
    if (!scopeInstitutionId) {
      return 'All institutions';
    }

    return institutionOptions.find(option => String(option.value) === String(scopeInstitutionId))?.label ?? 'Selected institution';
  }, [institutionOptions, scopeInstitutionId]);

  const resourceColumns = useMemo(() => {
    if (!currentResource) {
      return [];
    }

    const actionsColumn = {
      key: '__actions',
      header: 'Actions',
      render: row => (
        <div className="workspace-actions">
          {currentResource.updateApi && (
            <Button variant="secondary" size="sm" onClick={() => openEditForm(row)}>
              Edit
            </Button>
          )}
          {currentResource.currentApi && (
            <Button variant="secondary" size="sm" onClick={() => handleSetCurrentYear(row)} disabled={Boolean(row.currentYear)}>
              {row.currentYear ? 'Current' : 'Set current'}
            </Button>
          )}
          {currentResource.statusApi && (
            <Button variant="secondary" size="sm" onClick={() => handleOpenStatusDialog(row)}>
              {row.active ? 'Deactivate' : 'Activate'}
            </Button>
          )}
        </div>
      )
    };

    return [...currentResource.columns, actionsColumn];
  }, [currentResource]);

  function handleOpenCreate() {
    if (!currentResource) {
      return;
    }

    const formValues = buildResolvedFormValues(currentResource, null, defaultInstitutionId);
    setFormState({
      open: true,
      mode: 'create',
      values: formValues,
      fieldErrors: {},
      validationMessages: [],
      submitting: false,
      rowId: null
    });
  }

  function openEditForm(row) {
    if (!currentResource || !currentResource.updateApi) {
      return;
    }

    const formValues = buildResolvedFormValues(currentResource, row, defaultInstitutionId);
    setFormState({
      open: true,
      mode: 'edit',
      values: formValues,
      fieldErrors: {},
      validationMessages: [],
      submitting: false,
      rowId: row.id
    });
  }

  function closeForm() {
    setFormState(current => ({
      ...current,
      open: false,
      fieldErrors: {},
      validationMessages: [],
      submitting: false
    }));
  }

  function handleFilterChange(name, value) {
    setFilters(current => ({
      ...current,
      [name]: value,
      page: 1
    }));
    setPage(1);

    if (name === 'institutionId') {
      setScopeInstitutionId(value);
    }
  }

  function handleFormChange(name, value) {
    setFormState(current => {
      const nextValues = { ...current.values, [name]: value };

      if (name === 'institutionId') {
        if (currentResource?.key === 'departmentProgrammes') {
          nextValues.departmentId = '';
          nextValues.programmeId = '';
          nextValues.academicYearId = '';
        }

        if (currentResource?.key === 'semesters') {
          nextValues.programmeId = '';
        }

        if (currentResource?.key === 'sections') {
          nextValues.departmentId = '';
          nextValues.programmeId = '';
          nextValues.academicYearId = '';
          nextValues.semesterId = '';
        }
      }

      if (name === 'departmentId' && currentResource?.key === 'sections') {
        nextValues.programmeId = '';
        nextValues.semesterId = '';
      }

      if (name === 'programmeId' && currentResource?.key === 'sections') {
        nextValues.semesterId = '';
      }

      return {
        ...current,
        values: nextValues,
        fieldErrors: {
          ...current.fieldErrors,
          [name]: undefined
        },
        validationMessages: []
      };
    });
  }

  function handleFieldClear(fieldName) {
    setFormState(current => ({
      ...current,
      fieldErrors: {
        ...current.fieldErrors,
        [fieldName]: undefined
      }
    }));
  }

  function handleCloseStatusDialog() {
    setStatusDialog({ open: false, row: null, nextActive: false, title: '', description: '', action: 'status' });
  }

  function handleOpenStatusDialog(row) {
    if (!currentResource) {
      return;
    }

    setStatusDialog({
      open: true,
      row,
      nextActive: !row.active,
      title: `${row.active ? 'Deactivate' : 'Activate'} ${currentResource.label.toLowerCase()}`,
      description: row.active
        ? 'Deactivate this record if it should no longer be available for future forms.'
        : 'Activate this record so it becomes available for current workflows.',
      action: 'status'
    });
  }

  function handleSetCurrentYear(row) {
    setStatusDialog({
      open: true,
      row,
      nextActive: true,
      title: 'Set current academic year',
      description: 'CampusSphere will mark this academic year as current and clear the previous current year for the same institution.',
      action: 'current'
    });
  }

  async function refreshWorkspace() {
    if (isOverview) {
      await refreshOverview();
      return;
    }

    await refreshList();
    await refreshCatalogs();
  }

  async function refreshCatalogs() {
    const requestId = ++catalogRequestRef.current;
    setCatalogLoading(true);
    setCatalogError('');

    try {
      const [institutionsResponse, departmentsResponse, programmesResponse, academicYearsResponse, semestersResponse, mappingsResponse] = await Promise.all([
        masterDataApi.listInstitutions({ page: 0, size: CATALOG_PAGE_SIZE }),
        currentCatalogInstitutionId ? masterDataApi.listDepartments({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
        currentCatalogInstitutionId ? masterDataApi.listProgrammes({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
        currentCatalogInstitutionId ? masterDataApi.listAcademicYears({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
        currentCatalogInstitutionId ? masterDataApi.listSemesters({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null),
        currentCatalogInstitutionId ? masterDataApi.listDepartmentProgrammes({ institutionId: currentCatalogInstitutionId, page: 0, size: CATALOG_PAGE_SIZE }) : Promise.resolve(null)
      ]);

      if (requestId !== catalogRequestRef.current) {
        return;
      }

      const fetchedInstitutions = uniqueById(unwrapPage(institutionsResponse).content);

      setCatalogs({
        institutions: fetchedInstitutions,
        departments: departmentsResponse ? unwrapPage(departmentsResponse).content : [],
        programmes: programmesResponse ? unwrapPage(programmesResponse).content : [],
        academicYears: academicYearsResponse ? unwrapPage(academicYearsResponse).content : [],
        semesters: semestersResponse ? unwrapPage(semestersResponse).content : [],
        mappings: mappingsResponse ? unwrapPage(mappingsResponse).content : []
      });

      if (!scopeInstitutionId && fetchedInstitutions.length === 1) {
        setScopeInstitutionId(fetchedInstitutions[0].value);
      }
    } catch (error) {
      if (requestId === catalogRequestRef.current) {
        setCatalogError(getApiErrorMessage(error, 'Unable to load master data options right now.'));
      }
    } finally {
      if (requestId === catalogRequestRef.current) {
        setCatalogLoading(false);
      }
    }
  }

  async function refreshList() {
    if (!currentResource) {
      return;
    }

    const requestId = ++listRequestRef.current;
    setLoading(true);
    setErrorMessage('');

    try {
      const requestFilters = {
        ...filters,
        page: page - 1,
        size: pageSize
      };
      const params = currentResource.listQuery(requestFilters);
      const response = await masterDataApi[currentResource.listApi](params);
      if (requestId !== listRequestRef.current) {
        return;
      }
      const payload = unwrapPage(response);
      setRows(payload.content);
      setPageMeta(payload);
    } catch (error) {
      if (requestId !== listRequestRef.current) {
        return;
      }
      setRows([]);
      setPageMeta({ page, size: pageSize, totalElements: 0, totalPages: 0, first: true, last: true });
      setErrorMessage(getApiErrorMessage(error, 'Unable to load this workspace right now.'));
    } finally {
      if (requestId === listRequestRef.current) {
        setLoading(false);
      }
    }
  }

  async function refreshOverview() {
    const requestId = ++overviewRequestRef.current;
    setOverviewLoading(true);
    setOverviewError('');

    if (!scopeInstitutionId) {
      setOverview(null);
      setOverviewLoading(false);
      return;
    }

    try {
      const [institutionResponse, departmentsResponse, academicYearsResponse, programmesResponse, mappingsResponse, semestersResponse, sectionsResponse] = await Promise.all([
        masterDataApi.getInstitution(scopeInstitutionId),
        masterDataApi.listDepartments({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
        masterDataApi.listAcademicYears({ institutionId: scopeInstitutionId, page: 0, size: 20 }),
        masterDataApi.listProgrammes({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
        masterDataApi.listDepartmentProgrammes({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
        masterDataApi.listSemesters({ institutionId: scopeInstitutionId, page: 0, size: 1 }),
        masterDataApi.listSections({ institutionId: scopeInstitutionId, page: 0, size: 1 })
      ]);

      if (requestId !== overviewRequestRef.current) {
        return;
      }

      const academicYearPage = unwrapPage(academicYearsResponse);
      const currentAcademicYear = academicYearPage.content.find(year => year.currentYear) ?? null;

      setOverview({
        institution: unwrapData(institutionResponse),
        counts: {
          institution: 1,
          departments: unwrapPage(departmentsResponse).totalElements,
          academicYears: academicYearPage.totalElements,
          programmes: unwrapPage(programmesResponse).totalElements,
          mappings: unwrapPage(mappingsResponse).totalElements,
          semesters: unwrapPage(semestersResponse).totalElements,
          sections: unwrapPage(sectionsResponse).totalElements
        },
        currentAcademicYear
      });
    } catch (error) {
      if (requestId === overviewRequestRef.current) {
        setOverviewError(getApiErrorMessage(error, 'Unable to load the institutional overview right now.'));
      }
    } finally {
      if (requestId === overviewRequestRef.current) {
        setOverviewLoading(false);
      }
    }
  }

  useEffect(() => {
    if (!canAccessWorkspace) {
      return;
    }

    if (isOverview) {
      refreshOverview();
    }
  }, [isOverview, scopeInstitutionId, canAccessWorkspace]);

  useEffect(() => {
    if (!canAccessWorkspace || isOverview || !currentResource) {
      return;
    }

    refreshList();
  }, [currentResource?.key, filters, page, pageSize, isOverview, canAccessWorkspace]);

  useEffect(() => {
    if (!canAccessWorkspace) {
      return;
    }

    refreshCatalogs();
  }, [currentCatalogInstitutionId, canAccessWorkspace]);

  async function handleFormSubmit(event) {
    event.preventDefault();
    if (!currentResource) {
      return;
    }

    const validation = validateResourceForm(currentResource, formState.values, catalogs, defaultInstitutionId);
    setFormState(current => ({
      ...current,
      fieldErrors: validation.fieldErrors,
      validationMessages: validation.messages
    }));

    if (validation.messages.length > 0) {
      return;
    }

    const payload = buildPayload(currentResource, formState.values, defaultInstitutionId);
    const apiAction = formState.mode === 'edit' ? currentResource.updateApi : currentResource.createApi;
    if (!apiAction) {
      pushToast('This action is not available for the current resource.', 'error');
      return;
    }

    setFormState(current => ({ ...current, submitting: true }));

    try {
      if (formState.mode === 'edit') {
        await masterDataApi[apiAction](formState.rowId, payload);
        pushToast(`${currentResource.label} updated successfully.`, 'success');
      } else {
        await masterDataApi[apiAction](payload);
        pushToast(`${currentResource.label} created successfully.`, 'success');
      }

      closeForm();
      await refreshWorkspace();
    } catch (error) {
      const fieldErrors = parseFieldErrors(error);
      const validationMessages = extractValidationMessages(error);
      const message = getApiErrorMessage(error, `Unable to save ${currentResource.label.toLowerCase()} right now.`);
      setFormState(current => ({
        ...current,
        fieldErrors,
        validationMessages
      }));
      pushToast(message, 'error');
    } finally {
      setFormState(current => ({ ...current, submitting: false }));
    }
  }

  function buildPayload(resourceConfig, values, institutionIdFallback) {
    const basePayload = resourceConfig.buildPayload ? resourceConfig.buildPayload(values) : { ...values };

    if (resourceConfig.key !== 'institution') {
      const institutionId = normalizeText(values.institutionId || institutionIdFallback || '');
      if (institutionId) {
        basePayload.institutionId = Number.isNaN(Number(institutionId)) ? institutionId : Number(institutionId);
      }
    }

    return Object.fromEntries(
      Object.entries(basePayload).map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
    );
  }

  async function confirmStatusChange() {
    if (!currentResource || !statusDialog.row) {
      return;
    }

    try {
      if (statusDialog.action === 'current') {
        await masterDataApi[currentResource.currentApi](statusDialog.row.id);
        pushToast('Academic year marked as current.', 'success');
      } else {
        await masterDataApi[currentResource.statusApi](statusDialog.row.id, statusDialog.nextActive);
        pushToast(`${currentResource.label} ${statusDialog.nextActive ? 'activated' : 'deactivated'}.`, 'success');
      }

      handleCloseStatusDialog();
      await refreshWorkspace();
    } catch (error) {
      pushToast(getApiErrorMessage(error, 'Unable to update the selected record right now.'), 'error');
      handleCloseStatusDialog();
    }
  }

  function renderField(field) {
    if (field.name === 'institutionId' && currentResource?.key !== 'institution' && !visibleInstitutionField) {
      return null;
    }

    const error = formState.fieldErrors[field.name];
    const options = field.type === 'select' ? getSelectOptions({
      resource: currentResource,
      field,
      catalogs,
      formValues: formState.values,
      filters,
      isAdmin: roleCode === 'ADMINISTRATOR' || roleCode === 'SUPER_ADMIN',
      hiddenInstitutionField: !visibleInstitutionField
    }) : [];

    const disabled =
      Boolean(formState.submitting) ||
      Boolean(catalogLoading && field.type === 'select' && field.optionsKey !== 'institutions') ||
      (currentResource?.key === 'sections' && field.name === 'programmeId' && !formState.values.departmentId) ||
      (currentResource?.key === 'sections' && field.name === 'semesterId' && !formState.values.programmeId) ||
      (currentResource?.key !== 'institution' && field.name !== 'institutionId' && field.optionsKey && !currentCatalogInstitutionId && visibleInstitutionField);

    const commonProps = {
      id: field.name,
      name: field.name,
      value: formState.values[field.name] ?? '',
      disabled,
      onChange: event => handleFormChange(field.name, field.type === 'checkbox' ? event.target.checked : event.target.value)
    };

    const fieldClassName = error ? `${field.type === 'textarea' ? 'textarea' : field.type === 'select' ? 'select' : 'input'}--error` : '';

    if (field.type === 'checkbox') {
      return (
        <label key={field.name} className={classNames('checkbox', error && 'field--error')}>
          <input
            type="checkbox"
            id={field.name}
            name={field.name}
            checked={Boolean(formState.values[field.name])}
            disabled={Boolean(formState.submitting)}
            onChange={event => handleFormChange(field.name, event.target.checked)}
          />
          <span>
            {field.label}
            {field.helpText && <span className="field__help"> {field.helpText}</span>}
            {error && <span className="field__error">{error}</span>}
          </span>
        </label>
      );
    }

    if (field.type === 'textarea') {
      return (
        <div key={field.name} className={classNames('field', error && 'field--error')}>
          <Textarea
            {...commonProps}
            label={field.label}
            helperText={field.helpText}
            rows={field.rows ?? 4}
            className={fieldClassName}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? `${field.name}-error` : undefined}
          />
          {error && (
            <span className="field__error" id={`${field.name}-error`}>
              {error}
            </span>
          )}
        </div>
      );
    }

    if (field.type === 'select') {
      const selectValue = formState.values[field.name] ?? '';
      const computedHelpText =
        field.name === 'programmeId' && currentResource?.key === 'sections' && !formState.values.departmentId
          ? 'Choose a department first to narrow the programme list.'
          : field.name === 'semesterId' && currentResource?.key === 'sections' && !formState.values.programmeId
            ? 'Choose a programme first to load matching semesters.'
            : field.helpText;

      return (
        <div key={field.name} className={classNames('field', error && 'field--error')}>
          <Select
            {...commonProps}
            value={selectValue}
            label={field.label}
            helperText={computedHelpText}
            className={fieldClassName}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? `${field.name}-error` : undefined}
          >
            <option value="">Select {field.label.toLowerCase()}</option>
            {options.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </Select>
          {error && (
            <span className="field__error" id={`${field.name}-error`}>
              {error}
            </span>
          )}
        </div>
      );
    }

    return (
      <div key={field.name} className={classNames('field', error && 'field--error')}>
        <Input
          {...commonProps}
          type={field.type === 'date' ? 'date' : field.type === 'number' ? 'number' : field.type === 'url' ? 'url' : field.type === 'tel' ? 'tel' : field.type === 'email' ? 'email' : 'text'}
          label={field.label}
          helperText={field.helpText}
          min={field.min}
          max={field.max}
          step={field.step}
          maxLength={field.maxLength}
          className={fieldClassName}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${field.name}-error` : undefined}
        />
        {error && (
          <span className="field__error" id={`${field.name}-error`}>
            {error}
          </span>
        )}
      </div>
    );
  }

  function renderFilters() {
    if (!currentResource) {
      return null;
    }

    return (
      <FilterPanel title="Filters">
        <div className="workspace-filters">
          {currentResource.filters.map(filter => {
            if (filter.type === 'search') {
              return (
                <SearchBar
                  key={filter.name}
                  value={filters[filter.name] ?? ''}
                  onChange={value => handleFilterChange(filter.name, value)}
                  placeholder={filter.placeholder ?? filter.label}
                />
              );
            }

            if (filter.type === 'select') {
              const options = filter.options ?? getSelectOptions({
                resource: currentResource,
                field: filter,
                catalogs,
                formValues: filters,
                filters,
                isAdmin: roleCode === 'ADMINISTRATOR' || roleCode === 'SUPER_ADMIN',
                hiddenInstitutionField: !visibleInstitutionField
              });

              const shouldHide = filter.name === 'institutionId' && institutionOptions.length === 1 && !visibleInstitutionField;
              if (shouldHide) {
                return null;
              }

              return (
                <Select
                  key={filter.name}
                  id={`filter-${filter.name}`}
                  label={filter.label}
                  value={filters[filter.name] ?? ''}
                  onChange={event => handleFilterChange(filter.name, event.target.value)}
                >
                  <option value="">All {filter.label.toLowerCase()}</option>
                  {options.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </Select>
              );
            }

            return null;
          })}

          <Select
            id="filter-size"
            label="Rows per page"
            value={String(pageSize)}
            onChange={event => {
              setPageSize(Number(event.target.value));
              setPage(1);
            }}
          >
            {PAGE_SIZE_OPTIONS.map(option => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </Select>
        </div>
      </FilterPanel>
    );
  }

  function renderResourceWorkspace() {
    if (!currentResource) {
      return null;
    }

    const editable = Boolean(currentResource.updateApi);
    const currentRows = rows;

    return (
      <>
        <section className="dashboard-page__hero workspace-hero">
          <div>
            <Badge tone="neutral">{currentResource.label}</Badge>
            <h1>{currentResource.title}</h1>
            <p>{currentResource.description}</p>
          </div>
          <div className="dashboard-page__hero-actions">
            <Badge tone={catalogError ? 'warning' : 'neutral'}>
              {catalogLoading ? 'Loading options' : currentScopeLabel}
            </Badge>
            {currentResource.key === 'departmentProgrammes' && <Badge tone="neutral">Create and status only</Badge>}
            <Button variant="secondary" size="sm" onClick={() => refreshWorkspace()}>
              Refresh
            </Button>
            {currentResource.canCreate && (
              <Button size="sm" onClick={handleOpenCreate}>
                {currentResource.key === 'institution' ? 'Add institution' : `New ${currentResource.label.toLowerCase()}`}
              </Button>
            )}
          </div>
        </section>

        {catalogError && <ErrorBanner message={catalogError} />}

        <div className="workspace-overview">
          <Card elevated className="workspace-summary">
            <div className="section-panel__title">
              <strong>Workspace summary</strong>
              <Badge tone="neutral">{pageMeta.totalElements} records</Badge>
            </div>
            <div className="workspace-summary__grid">
              <div className="workspace-summary__item">
                <span>Current page</span>
                <strong>{pageMeta.page || 1}</strong>
              </div>
              <div className="workspace-summary__item">
                <span>Page size</span>
                <strong>{pageSize}</strong>
              </div>
              <div className="workspace-summary__item">
                <span>Scope</span>
                <strong>{currentScopeLabel}</strong>
              </div>
            </div>
          </Card>

          <Card elevated className="workspace-summary">
            <div className="section-panel__title">
              <strong>Status rules</strong>
              <Badge tone="neutral">Validated</Badge>
            </div>
            <ul className="workspace-summary__list">
              <li>Institutions, departments, programmes, semesters, and sections stay institution-scoped.</li>
              <li>Backend validation still prevents cross-institution records and duplicate keys.</li>
              <li>Success and error feedback uses toasts rather than raw server payloads.</li>
            </ul>
          </Card>
        </div>

        <div className="card-grid card-grid--two">
          <Card elevated className="section-panel">
            <div className="section-panel__title">
              <strong>{currentResource.title}</strong>
              <Badge tone={editable ? 'success' : 'neutral'}>{editable ? 'Editable' : 'Limited by API'}</Badge>
            </div>
            {renderFilters()}
          </Card>

          <Card elevated className="section-panel">
            <div className="section-panel__title">
              <strong>Scoping reminder</strong>
              <Badge tone="neutral">Institution aware</Badge>
            </div>
            {scopeInstitutionId ? (
              <div className="workspace-scope">
                <p>
                  Records shown here are scoped to <strong>{currentScopeLabel}</strong>.
                </p>
                <p>
                  Changing the institution filter updates the master-data option lists so the form values stay valid.
                </p>
              </div>
            ) : (
              <NoInstitutionSelected onClear={() => {
                setScopeInstitutionId('');
                setFilters(createDefaultFilters(currentResource));
              }} />
            )}
          </Card>
        </div>

        <Card elevated className="workspace-table-card">
          <div className="section-panel__title">
            <strong>{currentResource.title}</strong>
            <div className="workspace-table-card__meta">
              <Badge tone="neutral">{currentPageMeta.totalElements} total</Badge>
              <RoleBadge role={roleCode} />
            </div>
          </div>

          {loading ? (
            <WorkspaceSkeleton />
          ) : errorMessage ? (
            <ErrorState
              title="Could not load records"
              description={errorMessage}
              onRetry={() => refreshList()}
            />
          ) : currentRows.length === 0 ? (
            <EmptyState
              title={`No ${currentResource.label.toLowerCase()} records found`}
              description={
                currentResource.key === 'departmentProgrammes'
                  ? 'Create a mapping first so departments can offer specific programmes.'
                  : `Use ${currentResource.canCreate ? 'the create action' : 'your filters'} to add or find data for this workspace.`
              }
              actionLabel={currentResource.canCreate ? `New ${currentResource.label.toLowerCase()}` : undefined}
              onAction={currentResource.canCreate ? handleOpenCreate : undefined}
            />
          ) : (
            <Table columns={resourceColumns} rows={currentRows} emptyMessage={`No ${currentResource.label.toLowerCase()} records available.`} />
          )}

          <div className="workspace-footer">
            <Pagination
              page={currentPageMeta.page || page}
              totalPages={currentPageMeta.totalPages || 1}
              onPageChange={nextPage => setPage(nextPage)}
            />
          </div>
        </Card>

        <Modal
          open={formState.open}
          title={formState.mode === 'edit' ? `Edit ${currentResource.label.toLowerCase()}` : `Create ${currentResource.label.toLowerCase()}`}
          description={`Maintain ${currentResource.label.toLowerCase()} records with institution scoping and backend validation.`}
          onClose={closeForm}
          onSubmit={handleFormSubmit}
          submitLabel={formState.mode === 'edit' ? 'Save changes' : 'Create record'}
          loading={formState.submitting}
          size={currentResource.formSections?.length > 2 ? 'lg' : 'md'}
        >
          <ErrorBanner message={formState.validationMessages.length > 0 ? 'Please correct the highlighted fields before saving.' : ''} />
          <ValidationMessages messages={formState.validationMessages} />

          <div className="stack stack--wide">
            {(currentResource.formSections?.length ? currentResource.formSections : [{ title: currentResource.title, description: currentResource.description, fields: currentResource.fields.map(field => field.name) }]).map(section => (
              <FormSection key={section.title} title={section.title} description={section.description}>
                <div className={classNames('grid', section.fields.length > 1 && section.fields.length % 2 === 0 ? 'grid--2' : '')}>
                  {section.fields.map(fieldName => {
                    if (fieldName === 'institutionId' && currentResource.key !== 'institution' && !visibleInstitutionField) {
                      return null;
                    }
                    const field = currentResource.fields.find(candidate => candidate.name === fieldName);
                    return field ? renderField(field) : null;
                  })}
                </div>
              </FormSection>
            ))}
          </div>
        </Modal>

        <Modal
          open={statusDialog.open}
          title={statusDialog.title}
          description={statusDialog.description}
          confirmLabel={statusDialog.action === 'current' ? 'Set current' : statusDialog.nextActive ? 'Activate' : 'Deactivate'}
          cancelLabel="Cancel"
          onClose={handleCloseStatusDialog}
          onConfirm={confirmStatusChange}
        />
      </>
    );
  }

  function renderOverviewWorkspace() {
    return (
      <>
        <section className="dashboard-page__hero workspace-hero">
          <div>
            <Badge tone="neutral">Institution setup</Badge>
            <h1>Master data for every college.</h1>
            <p>Institution records, timelines, programmes, and sections stay scoped here.</p>
          </div>
          <div className="dashboard-page__hero-actions">
            <Badge tone="neutral">{currentScopeLabel}</Badge>
            <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.dashboard}>
              Back to dashboard
            </Button>
            <RoleBadge role={roleCode} />
          </div>
        </section>

        <div className="card-grid card-grid--two">
          <Card elevated className="workspace-summary workspace-summary--overview">
            <div className="section-panel__title">
              <strong>Institution selection</strong>
              <Badge tone="neutral">{institutionOptions.length} available</Badge>
            </div>
            {institutionOptions.length > 1 ? (
              <Select
                id="overview-institution"
                label="Choose an institution"
                value={scopeInstitutionId}
                onChange={event => setScopeInstitutionId(event.target.value)}
              >
                <option value="">Select an institution</option>
                {institutionOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </Select>
            ) : (
              <EmptyState
                title={scopeInstitutionId ? 'Scoped to one institution' : 'No institution selected'}
                description={scopeInstitutionId ? 'The workspace is already scoped to the only institution available to this account.' : 'Choose an institution to review live setup progress.'}
                actionLabel={!scopeInstitutionId ? 'Load institution' : undefined}
                onAction={!scopeInstitutionId ? () => setScopeInstitutionId(institutionOptions[0]?.value ?? '') : undefined}
              />
            )}
          </Card>

          <Card elevated className="workspace-summary workspace-summary--overview">
            <div className="section-panel__title">
              <strong>Setup progress</strong>
              <Badge tone={overview?.counts ? 'success' : 'neutral'}>{overview?.counts ? `${Math.round((setupChecklist.filter(item => item.done).length / setupChecklist.length) * 100)}%` : '0%'}</Badge>
            </div>
            <div className="workspace-progress">
              {setupChecklist.map(item => (
                <div key={item.key} className={classNames('workspace-progress__item', item.done && 'workspace-progress__item--done')}>
                  <StatusIndicator tone={item.done ? 'success' : 'warning'} label={item.done ? 'Ready' : 'Pending'} />
                  <div>
                    <strong>{item.label}</strong>
                    <p>{item.note}</p>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>

        {overviewError && <ErrorState title="Overview unavailable" description={overviewError} onRetry={refreshOverview} />}

        {overviewLoading ? (
          <Card elevated>
            <LoadingSkeleton lines={6} />
          </Card>
        ) : overview ? (
          <>
            <div className="workspace-stats">
              {overviewStats.map(stat => (
                <Card key={stat.key} elevated className="workspace-stat">
                  <span>{stat.label}</span>
                  <strong>{stat.value}</strong>
                </Card>
              ))}
            </div>

            <div className="card-grid card-grid--two">
              <Card elevated className="section-panel">
                <div className="section-panel__title">
                  <strong>Institution profile</strong>
                  <Badge tone={overview.institution?.active ? 'success' : 'neutral'}>{overview.institution?.active ? 'Active' : 'Inactive'}</Badge>
                </div>
                <div className="workspace-profile">
                  <div>
                    <h2>{overview.institution?.institutionName ?? 'Institution'}</h2>
                    <p>{overview.institution?.shortName ?? overview.institution?.institutionCode ?? 'No institution metadata available'}</p>
                  </div>
                  <div className="workspace-profile__grid">
                    <div>
                      <span>Code</span>
                      <strong>{overview.institution?.institutionCode ?? '-'}</strong>
                    </div>
                    <div>
                      <span>Type</span>
                      <strong>{overview.institution?.institutionType ?? '-'}</strong>
                    </div>
                    <div>
                      <span>City</span>
                      <strong>{overview.institution?.city ?? '-'}</strong>
                    </div>
                    <div>
                      <span>Email</span>
                      <strong>{overview.institution?.email ?? '-'}</strong>
                    </div>
                  </div>
                </div>
              </Card>

              <Card elevated className="section-panel">
                <div className="section-panel__title">
                  <strong>Current academic year</strong>
                  <Badge tone={overview.currentAcademicYear ? 'success' : 'neutral'}>{overview.currentAcademicYear ? 'Current' : 'Not set'}</Badge>
                </div>
                {overview.currentAcademicYear ? (
                  <div className="workspace-current-year">
                    <h2>{overview.currentAcademicYear.yearLabel}</h2>
                    <p>
                      {formatDate(overview.currentAcademicYear.startDate)} to {formatDate(overview.currentAcademicYear.endDate)}
                    </p>
                    <div className="workspace-current-year__meta">
                      <Badge tone={overview.currentAcademicYear.registrationOpen ? 'success' : 'neutral'}>
                        {overview.currentAcademicYear.registrationOpen ? 'Registration open' : 'Registration closed'}
                      </Badge>
                      <Badge tone={overview.currentAcademicYear.active ? 'success' : 'neutral'}>
                        {overview.currentAcademicYear.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                  </div>
                ) : (
                  <EmptyState
                    title="No current academic year"
                    description="Set one academic year as current before opening registration-driven workflows."
                  />
                )}
              </Card>
            </div>

            <Card elevated className="section-panel">
              <div className="section-panel__title">
                <strong>Setup checklist</strong>
                <Badge tone="neutral">{setupChecklist.filter(item => item.done).length}/{setupChecklist.length} complete</Badge>
              </div>
              <div className="workspace-checklist">
                {setupChecklist.map(item => (
                  <div key={item.key} className={classNames('workspace-checklist__item', item.done && 'workspace-checklist__item--done')}>
                    <StatusIndicator tone={item.done ? 'success' : 'warning'} label={item.done ? 'Complete' : 'Pending'} />
                    <div>
                      <strong>{item.label}</strong>
                      <p>{item.note}</p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>

            <div className="card-grid card-grid--two">
              <Card elevated className="section-panel">
                <div className="section-panel__title">
                  <strong>Missing setup states</strong>
                  <Badge tone="neutral">Honest empty states</Badge>
                </div>
                <ul className="workspace-summary__list">
                  <li>If any count is zero, the workspace stays honest and shows the missing record type instead of fake values.</li>
                  <li>Forms and filters stay disabled or scoped until the selected institution is known.</li>
                  <li>Every error message is phrased for real users, not for developers.</li>
                </ul>
              </Card>

              <Card elevated className="section-panel">
                <div className="section-panel__title">
                  <strong>Next steps</strong>
                  <Badge tone="neutral">Workflow ready</Badge>
                </div>
                <div className="workspace-next-steps">
                  <Button as={Link} variant="secondary" to={`${APP_ROUTES.dashboard}/institution-setup/departments`}>
                    Open departments
                  </Button>
                  <Button as={Link} variant="secondary" to={`${APP_ROUTES.dashboard}/institution-setup/programmes`}>
                    Review programmes
                  </Button>
                  <Button as={Link} variant="secondary" to={`${APP_ROUTES.dashboard}/institution-setup/academic-years`}>
                    Check academic years
                  </Button>
                </div>
              </Card>
            </div>
          </>
        ) : (
          <Card elevated>
            <EmptyState
              title="Select an institution"
              description="Use the selector above to view setup progress for one college."
            />
          </Card>
        )}
      </>
    );
  }

  if (!canAccessWorkspace) {
    return (
      <div className="dashboard-page">
        <Card elevated>
          <EmptyState
            title="Access restricted"
            description="Institution setup is available to administrator accounts only. Use the dashboard navigation or contact your system administrator if you expected access."
            actionLabel="Back to dashboard"
            onAction={() => window.location.assign(APP_ROUTES.dashboard)}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="dashboard-page institution-setup-page">
      <section className="dashboard-page__hero workspace-hero">
        <div>
          <Badge tone="neutral">Institution setup</Badge>
          <h1>Multi-college master data workspace</h1>
          <p>Maintain colleges, departments, programmes, semesters, and sections.</p>
        </div>
        <div className="dashboard-page__hero-actions">
          <Badge tone={catalogError ? 'warning' : 'success'}>{catalogLoading ? 'Loading' : 'Ready'}</Badge>
          <RoleBadge role={roleCode} />
          <Button as={Link} variant="secondary" size="sm" to={APP_ROUTES.dashboard}>
            Dashboard
          </Button>
        </div>
      </section>

      <Card className="section-panel">
        <div className="tabs tabs--scrollable" role="tablist" aria-label="Institution setup sections">
          {MASTER_DATA_TABS.map(tab => (
            <NavLink key={tab.path} to={tab.path} className={({ isActive }) => classNames('tabs__tab', isActive && 'tabs__tab--active')}>
              {tab.label}
            </NavLink>
          ))}
        </div>
      </Card>

      {isOverview ? renderOverviewWorkspace() : renderResourceWorkspace()}

      <div className="toast-stack" aria-live="polite" aria-atomic="true">
        {toasts.map(toast => (
          <Toast key={toast.id} message={toast.message} tone={toast.tone} />
        ))}
      </div>
    </div>
  );
}
