import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import FormSection from '../../components/FormSection';
import LoadingButton from '../../components/LoadingButton';
import PasswordInput from '../../components/PasswordInput';
import ValidationMessages from '../../components/ValidationMessages';
import { ErrorBanner, SuccessBanner } from '../../components/Banner';
import { APP_ROUTES } from '../../constants/routes';
import { authApi } from '../../services/authApi';
import { useAuth } from '../../context/AuthContext';
import { extractValidationMessages, getApiErrorMessage } from '../../utils/apiErrors';
import { getDashboardRoute } from '../../utils/auth';

const initialState = {
  firstName: '',
  lastName: '',
  registerNumber: '',
  department: '',
  academicYear: 'I',
  section: 'A',
  email: '',
  phoneNumber: '',
  password: '',
  confirmPassword: '',
  profilePictureUrl: '',
  termsAccepted: false
};

const yearOptions = [
  { value: 'I', label: 'I Year' },
  { value: 'II', label: 'II Year' },
  { value: 'III', label: 'III Year' },
  { value: 'IV', label: 'IV Year' }
];

const sectionOptions = ['A', 'B', 'C', 'D'];

export default function RegisterPage() {
  const navigate = useNavigate();
  const { signIn } = useAuth();
  const [form, setForm] = useState(initialState);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [validationMessages, setValidationMessages] = useState([]);

  const completion = useMemo(() => calculateCompletion(form), [form]);

  async function handleSubmit(event) {
    event.preventDefault();
    const nextValidationMessages = validateRegistrationForm(form);
    setValidationMessages(nextValidationMessages);
    setSuccessMessage('');
    setErrorMessage('');

    if (nextValidationMessages.length > 0) {
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.registerStudent({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        registerNumber: form.registerNumber.trim(),
        department: form.department.trim(),
        academicYear: form.academicYear,
        section: form.section.trim(),
        email: form.email.trim(),
        phoneNumber: form.phoneNumber.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
        profilePictureUrl: form.profilePictureUrl.trim() || null,
        termsAccepted: form.termsAccepted
      });

      signIn({ ...response.data.data, rememberMe: false });
      setSuccessMessage('Student account created successfully. Redirecting to your dashboard.');
      window.setTimeout(() => navigate(getDashboardRoute(), { replace: true }), 250);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'Unable to complete the registration right now.'));
      setValidationMessages(extractValidationMessages(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Student registration"
      eyebrow="CampusSphere registration"
      description="Create a new account with academic details and the password you will use to sign in."
    >
      <SuccessBanner message={successMessage} />
      <ErrorBanner message={errorMessage} />
      <div className="progress-pill">Profile completion {completion}%</div>
      <form className="auth-form auth-form--stacked" onSubmit={handleSubmit} noValidate>
        <FormSection title="Personal details" description="Match the name used in your official college records.">
          <div className="grid grid--2">
            <label className="field" htmlFor="firstName">
              <span className="field__label">First name</span>
              <input id="firstName" className="input" value={form.firstName} onChange={event => setForm(current => ({ ...current, firstName: event.target.value }))} />
            </label>
            <label className="field" htmlFor="lastName">
              <span className="field__label">Last name</span>
              <input id="lastName" className="input" value={form.lastName} onChange={event => setForm(current => ({ ...current, lastName: event.target.value }))} />
            </label>
          </div>
        </FormSection>

        <FormSection title="Academic information">
          <div className="grid grid--3">
            <label className="field" htmlFor="registerNumber">
              <span className="field__label">Register number</span>
              <input id="registerNumber" className="input" value={form.registerNumber} onChange={event => setForm(current => ({ ...current, registerNumber: event.target.value }))} />
            </label>
            <label className="field" htmlFor="department">
              <span className="field__label">Department</span>
              <input id="department" className="input" value={form.department} onChange={event => setForm(current => ({ ...current, department: event.target.value }))} />
            </label>
            <label className="field" htmlFor="academicYear">
              <span className="field__label">Year</span>
              <select id="academicYear" className="select" value={form.academicYear} onChange={event => setForm(current => ({ ...current, academicYear: event.target.value }))}>
                {yearOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <div className="grid grid--2">
            <label className="field" htmlFor="section">
              <span className="field__label">Section</span>
              <select id="section" className="select" value={form.section} onChange={event => setForm(current => ({ ...current, section: event.target.value }))}>
                {sectionOptions.map(option => (
                  <option key={option} value={option}>
                    Section {option}
                  </option>
                ))}
              </select>
            </label>
            <label className="field" htmlFor="profilePictureUrl">
              <span className="field__label">Profile picture URL</span>
              <input
                id="profilePictureUrl"
                className="input"
                value={form.profilePictureUrl}
                onChange={event => setForm(current => ({ ...current, profilePictureUrl: event.target.value }))}
                placeholder="Optional"
              />
            </label>
          </div>
        </FormSection>

        <FormSection title="Contact information">
          <div className="grid grid--2">
            <label className="field" htmlFor="email">
              <span className="field__label">Email</span>
              <input id="email" type="email" className="input" value={form.email} onChange={event => setForm(current => ({ ...current, email: event.target.value }))} />
            </label>
            <label className="field" htmlFor="phoneNumber">
              <span className="field__label">Phone number</span>
              <input id="phoneNumber" className="input" value={form.phoneNumber} onChange={event => setForm(current => ({ ...current, phoneNumber: event.target.value }))} />
            </label>
          </div>
        </FormSection>

        <FormSection title="Account information">
          <div className="grid grid--2">
            <PasswordInput
              id="password"
              name="password"
              autoComplete="new-password"
              value={form.password}
              onChange={event => setForm(current => ({ ...current, password: event.target.value }))}
              helperText="Use 8+ characters with a mix of letters, numbers, and symbols."
            />
            <PasswordInput
              id="confirmPassword"
              name="confirmPassword"
              autoComplete="new-password"
              label="Confirm password"
              value={form.confirmPassword}
              onChange={event => setForm(current => ({ ...current, confirmPassword: event.target.value }))}
            />
          </div>
          <label className="checkbox">
            <input type="checkbox" checked={form.termsAccepted} onChange={event => setForm(current => ({ ...current, termsAccepted: event.target.checked }))} />
            <span>I accept the CampusSphere terms and student portal usage policy.</span>
          </label>
        </FormSection>

        <ValidationMessages messages={validationMessages} />

        <div className="auth-card__subnote">
          <span className="field__help">Use the same email and register number your college has on file.</span>
        </div>

        <LoadingButton loading={loading} type="submit" fullWidth>
          Create student account
        </LoadingButton>
      </form>
      <div className="auth-card__footer">
        <Link to={APP_ROUTES.login}>Already have an account?</Link>
        <Link to={APP_ROUTES.forgotPassword}>Need password help?</Link>
      </div>
      <div className="auth-card__subnote">
        <Button variant="secondary" as={Link} to={APP_ROUTES.home} size="sm">
          Back to home
        </Button>
      </div>
    </AuthCard>
  );
}

function validateRegistrationForm(form) {
  const messages = [];

  if (!form.firstName.trim()) messages.push('First name is required.');
  if (!form.lastName.trim()) messages.push('Last name is required.');
  if (!form.registerNumber.trim()) messages.push('Register number is required.');
  if (!form.department.trim()) messages.push('Department is required.');
  if (!form.email.trim()) messages.push('Email is required.');
  if (!form.phoneNumber.trim()) messages.push('Phone number is required.');
  if (!form.password) messages.push('Password is required.');
  if (!form.confirmPassword) messages.push('Confirm password is required.');
  if (form.password && form.confirmPassword && form.password !== form.confirmPassword) messages.push('Password confirmation does not match.');
  if (!form.termsAccepted) messages.push('Terms acceptance is required.');

  return messages;
}

function calculateCompletion(form) {
  const trackedFields = ['firstName', 'lastName', 'registerNumber', 'department', 'academicYear', 'section', 'email', 'phoneNumber', 'password', 'confirmPassword'];
  const filled = trackedFields.filter(field => String(form[field] ?? '').trim().length > 0).length;
  return Math.round((filled / trackedFields.length) * 100);
}
