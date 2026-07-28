import { INSTITUTION_TYPE_OPTIONS, PROGRAMME_LEVEL_OPTIONS, STATUS_FILTER_OPTIONS } from '../../constants/masterData';

const allActiveFilter = STATUS_FILTER_OPTIONS[0];

function selectLabel(row, labelKeys) {
  return labelKeys.map(key => row?.[key]).filter(Boolean).join(' - ');
}

function activeLabel(active) {
  if (active === true || active === 'true') return 'Active';
  if (active === false || active === 'false') return 'Inactive';
  return 'All records';
}

function toInstitutionOption(row) {
  return {
    value: String(row.id),
    label: `${row.institutionName} (${row.institutionCode})`
  };
}

function toDepartmentOption(row) {
  return {
    value: String(row.id),
    label: `${row.departmentName} (${row.departmentCode})`
  };
}

function toProgrammeOption(row) {
  return {
    value: String(row.id),
    label: `${row.programmeName} (${row.programmeCode})`
  };
}

function toAcademicYearOption(row) {
  return {
    value: String(row.id),
    label: row.yearLabel
  };
}

function toSemesterOption(row) {
  return {
    value: String(row.id),
    label: `${row.displayName} - Semester ${row.semesterNumber}`
  };
}

function toMappingOption(row) {
  return {
    value: String(row.id),
    label: `${row.departmentName} - ${row.programmeName}`
  };
}

function buildOptionGroup(label, options) {
  return { label, options };
}

export const MASTER_DATA_TABS = [
  { label: 'Overview', path: '/dashboard/institution-setup' },
  { label: 'Institution', path: '/dashboard/institution-setup/institution' },
  { label: 'Departments', path: '/dashboard/institution-setup/departments' },
  { label: 'Academic Years', path: '/dashboard/institution-setup/academic-years' },
  { label: 'Programmes', path: '/dashboard/institution-setup/programmes' },
  { label: 'Mappings', path: '/dashboard/institution-setup/programme-mappings' },
  { label: 'Semesters', path: '/dashboard/institution-setup/semesters' },
  { label: 'Sections', path: '/dashboard/institution-setup/sections' }
];

const commonStatusFilter = [allActiveFilter, ...STATUS_FILTER_OPTIONS.slice(1)];

export const MASTER_DATA_RESOURCES = {
  institution: {
    key: 'institution',
    label: 'Institution',
    path: '/dashboard/institution-setup/institution',
    title: 'Institution profile',
    description: 'Manage each college profile, active state, and branding details.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: true,
    supportsOverview: true,
    listApi: 'listInstitutions',
    createApi: 'createInstitution',
    updateApi: 'updateInstitution',
    statusApi: 'updateInstitutionStatus',
    listQuery: filters => ({
      search: filters.search || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'search', type: 'search', label: 'Search institutions', placeholder: 'Search code, name, city, or country' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'institutionCode', header: 'Code' },
      { key: 'institutionName', header: 'Institution' },
      { key: 'institutionType', header: 'Type' },
      { key: 'city', header: 'City' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} institution`,
    defaultValues: {
      institutionCode: '',
      institutionName: '',
      shortName: '',
      institutionType: 'COLLEGE',
      affiliation: '',
      accreditation: '',
      email: '',
      phone: '',
      website: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      country: '',
      postalCode: '',
      logoUrl: '',
      timezone: ''
    },
    formSections: [
      {
        title: 'Identity',
        description: 'Core registry details that identify the institution across CampusSphere.',
        fields: ['institutionCode', 'institutionName', 'shortName', 'institutionType', 'affiliation', 'accreditation']
      },
      {
        title: 'Contact',
        description: 'Official contact channels used in admin workflows and correspondence.',
        fields: ['email', 'phone', 'website']
      },
      {
        title: 'Location',
        description: 'Postal and geographic details for reports and headers.',
        fields: ['addressLine1', 'addressLine2', 'city', 'state', 'country', 'postalCode', 'logoUrl', 'timezone']
      }
    ],
    fields: [
      { name: 'institutionCode', label: 'Institution code', type: 'text', required: true, maxLength: 40, helpText: 'Used in identifiers and internal references.' },
      { name: 'institutionName', label: 'Institution name', type: 'text', required: true, maxLength: 160 },
      { name: 'shortName', label: 'Short name', type: 'text', maxLength: 80 },
      { name: 'institutionType', label: 'Institution type', type: 'select', required: true, options: INSTITUTION_TYPE_OPTIONS },
      { name: 'affiliation', label: 'Affiliation', type: 'text', maxLength: 160 },
      { name: 'accreditation', label: 'Accreditation', type: 'text', maxLength: 160 },
      { name: 'email', label: 'Official email', type: 'email', maxLength: 160 },
      { name: 'phone', label: 'Official phone', type: 'tel', maxLength: 24 },
      { name: 'website', label: 'Website', type: 'url', maxLength: 255 },
      { name: 'addressLine1', label: 'Address line 1', type: 'text', maxLength: 255 },
      { name: 'addressLine2', label: 'Address line 2', type: 'text', maxLength: 255 },
      { name: 'city', label: 'City', type: 'text', maxLength: 100 },
      { name: 'state', label: 'State', type: 'text', maxLength: 100 },
      { name: 'country', label: 'Country', type: 'text', maxLength: 100 },
      { name: 'postalCode', label: 'Postal code', type: 'text', maxLength: 20 },
      { name: 'logoUrl', label: 'Logo URL', type: 'url', maxLength: 512 },
      { name: 'timezone', label: 'Timezone', type: 'text', maxLength: 64, helpText: 'Example: Asia/Calcutta' }
    ],
    buildPayload: values => ({ ...values }),
    rowToForm: row => ({ ...row })
  },
  departments: {
    key: 'department',
    label: 'Department',
    path: '/dashboard/institution-setup/departments',
    title: 'Departments',
    description: 'Create and maintain departments for the selected institution.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: true,
    listApi: 'listDepartments',
    createApi: 'createDepartment',
    updateApi: 'updateDepartment',
    statusApi: 'updateDepartmentStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      search: filters.search || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'search', type: 'search', label: 'Search departments', placeholder: 'Search code or name' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'departmentCode', header: 'Code' },
      { key: 'departmentName', header: 'Department' },
      { key: 'institutionName', header: 'Institution' },
      { key: 'headOfDepartmentName', header: 'Head of department' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} department`,
    defaultValues: {
      institutionId: '',
      departmentCode: '',
      departmentName: '',
      shortName: '',
      description: '',
      departmentEmail: '',
      departmentPhone: ''
    },
    formSections: [
      {
        title: 'Identity',
        description: 'The department’s internal code and display name.',
        fields: ['institutionId', 'departmentCode', 'departmentName', 'shortName']
      },
      {
        title: 'Details',
        description: 'Extra context for faculty coordinators and administrators.',
        fields: ['description', 'departmentEmail', 'departmentPhone']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'departmentCode', label: 'Department code', type: 'text', required: true, maxLength: 40 },
      { name: 'departmentName', label: 'Department name', type: 'text', required: true, maxLength: 160 },
      { name: 'shortName', label: 'Short name', type: 'text', maxLength: 80 },
      { name: 'description', label: 'Description', type: 'textarea', maxLength: 255, rows: 4 },
      { name: 'departmentEmail', label: 'Department email', type: 'email', maxLength: 160 },
      { name: 'departmentPhone', label: 'Department phone', type: 'tel', maxLength: 24 }
    ],
    buildPayload: values => ({ ...values }),
    rowToForm: row => ({ ...row })
  },
  academicYears: {
    key: 'academicYear',
    label: 'Academic year',
    path: '/dashboard/institution-setup/academic-years',
    title: 'Academic years',
    description: 'Manage yearly timelines and current-year status.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: true,
    listApi: 'listAcademicYears',
    createApi: 'createAcademicYear',
    updateApi: 'updateAcademicYear',
    currentApi: 'setCurrentAcademicYear',
    statusApi: 'updateAcademicYearStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      search: filters.search || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'search', type: 'search', label: 'Search academic years', placeholder: 'Search label like 2026-2027' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'yearLabel', header: 'Academic year' },
      { key: 'startDate', header: 'Start date' },
      { key: 'endDate', header: 'End date' },
      { key: 'currentYear', header: 'Current', render: row => (row.currentYear ? 'Current' : 'No') },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} academic year`,
    defaultValues: {
      institutionId: '',
      yearLabel: '',
      startDate: '',
      endDate: '',
      currentYear: false,
      registrationOpen: false
    },
    formSections: [
      {
        title: 'Timeline',
        description: 'Define the academic cycle and its active state.',
        fields: ['institutionId', 'yearLabel', 'startDate', 'endDate']
      },
      {
        title: 'Controls',
        description: 'Mark the year as current and open registrations when required.',
        fields: ['currentYear', 'registrationOpen']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'yearLabel', label: 'Year label', type: 'text', required: true, maxLength: 24, helpText: 'Example: 2026-2027' },
      { name: 'startDate', label: 'Start date', type: 'date', required: true },
      { name: 'endDate', label: 'End date', type: 'date', required: true },
      { name: 'currentYear', label: 'Current academic year', type: 'checkbox' },
      { name: 'registrationOpen', label: 'Registration open', type: 'checkbox' }
    ],
    buildPayload: values => ({
      ...values,
      currentYear: Boolean(values.currentYear),
      registrationOpen: Boolean(values.registrationOpen)
    }),
    rowToForm: row => ({ ...row })
  },
  programmes: {
    key: 'programme',
    label: 'Programme',
    path: '/dashboard/institution-setup/programmes',
    title: 'Programmes',
    description: 'Define degree and diploma programmes for the institution.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: true,
    listApi: 'listProgrammes',
    createApi: 'createProgramme',
    updateApi: 'updateProgramme',
    statusApi: 'updateProgrammeStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      search: filters.search || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'search', type: 'search', label: 'Search programmes', placeholder: 'Search code, name, or level' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'programmeCode', header: 'Code' },
      { key: 'programmeName', header: 'Programme' },
      { key: 'programmeLevel', header: 'Level' },
      { key: 'durationSemesters', header: 'Semesters' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} programme`,
    defaultValues: {
      institutionId: '',
      programmeCode: '',
      programmeName: '',
      programmeLevel: 'UNDERGRADUATE',
      durationYears: 3,
      durationSemesters: 6
    },
    formSections: [
      {
        title: 'Programme identity',
        description: 'Programme code, label, and academic level.',
        fields: ['institutionId', 'programmeCode', 'programmeName', 'programmeLevel']
      },
      {
        title: 'Duration',
        description: 'Configure the normal duration for the programme.',
        fields: ['durationYears', 'durationSemesters']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'programmeCode', label: 'Programme code', type: 'text', required: true, maxLength: 40 },
      { name: 'programmeName', label: 'Programme name', type: 'text', required: true, maxLength: 160 },
      { name: 'programmeLevel', label: 'Programme level', type: 'select', required: true, options: PROGRAMME_LEVEL_OPTIONS },
      { name: 'durationYears', label: 'Duration in years', type: 'number', required: true, min: 1, step: 1 },
      { name: 'durationSemesters', label: 'Duration in semesters', type: 'number', required: true, min: 1, step: 1 }
    ],
    buildPayload: values => ({
      ...values,
      durationYears: Number(values.durationYears),
      durationSemesters: Number(values.durationSemesters)
    }),
    rowToForm: row => ({ ...row })
  },
  departmentProgrammes: {
    key: 'departmentProgramme',
    label: 'Department-Programme mapping',
    path: '/dashboard/institution-setup/programme-mappings',
    title: 'Department-programme mappings',
    description: 'Define which departments offer which programmes.',
    canCreate: true,
    canEdit: false,
    canToggleStatus: true,
    supportsSearch: false,
    listApi: 'listDepartmentProgrammes',
    createApi: 'createDepartmentProgramme',
    updateApi: null,
    statusApi: 'updateDepartmentProgrammeStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      departmentId: filters.departmentId || undefined,
      programmeId: filters.programmeId || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'departmentId', type: 'select', label: 'Department', optionsKey: 'departments' },
      { name: 'programmeId', type: 'select', label: 'Programme', optionsKey: 'programmes' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'departmentName', header: 'Department' },
      { key: 'programmeName', header: 'Programme' },
      { key: 'academicYearLabel', header: 'Academic year' },
      { key: 'intakeCapacity', header: 'Capacity' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: () => 'Create department-programme mapping',
    defaultValues: {
      institutionId: '',
      departmentId: '',
      programmeId: '',
      academicYearId: '',
      intakeCapacity: ''
    },
    formSections: [
      {
        title: 'Association',
        description: 'Choose the department, programme, and institution that should be linked.',
        fields: ['institutionId', 'departmentId', 'programmeId', 'academicYearId']
      },
      {
        title: 'Capacity',
        description: 'Optionally define the intake capacity for reporting and planning.',
        fields: ['intakeCapacity']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'departmentId', label: 'Department', type: 'select', required: true, optionsKey: 'departments' },
      { name: 'programmeId', label: 'Programme', type: 'select', required: true, optionsKey: 'programmes' },
      { name: 'academicYearId', label: 'Academic year', type: 'select', optionsKey: 'academicYears' },
      { name: 'intakeCapacity', label: 'Intake capacity', type: 'number', min: 1, step: 1 }
    ],
    buildPayload: values => ({
      ...values,
      institutionId: values.institutionId || null,
      departmentId: values.departmentId || null,
      programmeId: values.programmeId || null,
      academicYearId: values.academicYearId || null,
      intakeCapacity: values.intakeCapacity === '' ? null : Number(values.intakeCapacity)
    }),
    rowToForm: row => ({ ...row })
  },
  semesters: {
    key: 'semester',
    label: 'Semester',
    path: '/dashboard/institution-setup/semesters',
    title: 'Semesters',
    description: 'Create semester slots for each programme.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: false,
    listApi: 'listSemesters',
    createApi: 'createSemester',
    updateApi: 'updateSemester',
    statusApi: 'updateSemesterStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      programmeId: filters.programmeId || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'programmeId', type: 'select', label: 'Programme', optionsKey: 'programmes' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'programmeName', header: 'Programme' },
      { key: 'semesterNumber', header: 'Semester' },
      { key: 'displayName', header: 'Display name' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} semester`,
    defaultValues: {
      institutionId: '',
      programmeId: '',
      semesterNumber: '',
      displayName: ''
    },
    formSections: [
      {
        title: 'Programme context',
        description: 'Select the institution and programme that own the semester.',
        fields: ['institutionId', 'programmeId']
      },
      {
        title: 'Semester details',
        description: 'Define the semester number and the name shown in the UI.',
        fields: ['semesterNumber', 'displayName']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'programmeId', label: 'Programme', type: 'select', required: true, optionsKey: 'programmes' },
      { name: 'semesterNumber', label: 'Semester number', type: 'number', required: true, min: 1, step: 1 },
      { name: 'displayName', label: 'Display name', type: 'text', required: true, maxLength: 80 }
    ],
    buildPayload: values => ({
      ...values,
      semesterNumber: Number(values.semesterNumber)
    }),
    rowToForm: row => ({ ...row })
  },
  sections: {
    key: 'section',
    label: 'Section',
    path: '/dashboard/institution-setup/sections',
    title: 'Sections',
    description: 'Assign departments, programmes, academic years, and semesters to sections.',
    canCreate: true,
    canEdit: true,
    canToggleStatus: true,
    supportsSearch: true,
    listApi: 'listSections',
    createApi: 'createSection',
    updateApi: 'updateSection',
    statusApi: 'updateSectionStatus',
    listQuery: filters => ({
      institutionId: filters.institutionId || undefined,
      departmentId: filters.departmentId || undefined,
      programmeId: filters.programmeId || undefined,
      academicYearId: filters.academicYearId || undefined,
      semesterId: filters.semesterId || undefined,
      active: filters.active === '' ? undefined : filters.active === 'true',
      page: filters.page,
      size: filters.size
    }),
    filters: [
      { name: 'institutionId', type: 'select', label: 'Institution', optionsKey: 'institutions' },
      { name: 'departmentId', type: 'select', label: 'Department', optionsKey: 'departments' },
      { name: 'programmeId', type: 'select', label: 'Programme', optionsKey: 'programmes' },
      { name: 'academicYearId', type: 'select', label: 'Academic year', optionsKey: 'academicYears' },
      { name: 'semesterId', type: 'select', label: 'Semester', optionsKey: 'semesters' },
      { name: 'search', type: 'search', label: 'Search sections', placeholder: 'Search by section name' },
      { name: 'active', type: 'select', label: 'Status', options: commonStatusFilter }
    ],
    columns: [
      { key: 'sectionName', header: 'Section' },
      { key: 'departmentName', header: 'Department' },
      { key: 'programmeName', header: 'Programme' },
      { key: 'academicYearLabel', header: 'Academic year' },
      { key: 'semesterDisplayName', header: 'Semester' },
      { key: 'status', header: 'Status', render: row => activeLabel(row.active) }
    ],
    formTitle: mode => `${mode === 'edit' ? 'Edit' : 'Create'} section`,
    defaultValues: {
      institutionId: '',
      departmentId: '',
      programmeId: '',
      academicYearId: '',
      semesterId: '',
      sectionName: '',
      capacity: '',
      studyYear: ''
    },
    formSections: [
      {
        title: 'Allocation',
        description: 'Choose the institutional context for this section.',
        fields: ['institutionId', 'departmentId', 'programmeId', 'academicYearId', 'semesterId']
      },
      {
        title: 'Section details',
        description: 'Name and capacity used in student-facing workflows later.',
        fields: ['sectionName', 'capacity', 'studyYear']
      }
    ],
    fields: [
      { name: 'institutionId', label: 'Institution', type: 'select', required: true, optionsKey: 'institutions' },
      { name: 'departmentId', label: 'Department', type: 'select', required: true, optionsKey: 'departments' },
      { name: 'programmeId', label: 'Programme', type: 'select', required: true, optionsKey: 'sectionProgrammes' },
      { name: 'academicYearId', label: 'Academic year', type: 'select', required: true, optionsKey: 'academicYears' },
      { name: 'semesterId', label: 'Semester', type: 'select', required: true, optionsKey: 'sectionSemesters' },
      { name: 'sectionName', label: 'Section name', type: 'text', required: true, maxLength: 80 },
      { name: 'capacity', label: 'Capacity', type: 'number', min: 1, step: 1 },
      { name: 'studyYear', label: 'Study year', type: 'number', min: 1, step: 1 }
    ],
    buildPayload: values => ({
      ...values,
      capacity: values.capacity === '' ? null : Number(values.capacity),
      studyYear: values.studyYear === '' ? null : Number(values.studyYear)
    }),
    rowToForm: row => ({ ...row })
  }
};

export function getResourceByPath(pathname) {
  return Object.values(MASTER_DATA_RESOURCES).find(resource => pathname.startsWith(resource.path));
}

export function getResourceLabel(pathname) {
  const resource = getResourceByPath(pathname);
  return resource?.label ?? 'Overview';
}

export function mapInstitutions(rows = []) {
  return rows.map(toInstitutionOption);
}

export function mapDepartments(rows = []) {
  return rows.map(toDepartmentOption);
}

export function mapProgrammes(rows = []) {
  return rows.map(toProgrammeOption);
}

export function mapAcademicYears(rows = []) {
  return rows.map(toAcademicYearOption);
}

export function mapSemesters(rows = []) {
  return rows.map(toSemesterOption);
}

export function mapMappings(rows = []) {
  return rows.map(toMappingOption);
}

export function buildSelectGroups(items = []) {
  return items.map(group => buildOptionGroup(group.label, group.options));
}
