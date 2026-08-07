import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import LoadingButton from '../../components/LoadingButton';
import PasswordInput from '../../components/PasswordInput';
import ValidationMessages from '../../components/ValidationMessages';
import { ErrorBanner, SuccessBanner } from '../../components/Banner';
import { APP_ROUTES } from '../../constants/routes';
import { authApi } from '../../services/authApi';
import { useAuth } from '../../context/AuthContext';
import { consumeSessionNotice } from '../../services/session';
import { extractValidationMessages, getApiErrorMessage } from '../../utils/apiErrors';
import { getDashboardRoute } from '../../utils/auth';

const initialForm = {
  identifier: '',
  password: '',
  rememberMe: false
};

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [validationMessages, setValidationMessages] = useState([]);

  const targetPath = location.state?.from ?? getDashboardRoute();

  useEffect(() => {
    const notice = consumeSessionNotice();
    if (notice) {
      setErrorMessage(notice);
    }
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    const nextValidationMessages = validateLoginForm(form);
    setValidationMessages(nextValidationMessages);
    setSuccessMessage('');
    setErrorMessage('');

    if (nextValidationMessages.length > 0) {
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.login({
        identifier: form.identifier.trim(),
        password: form.password,
        rememberMe: form.rememberMe
      });
      signIn({ ...response.data.data, rememberMe: form.rememberMe });
      setSuccessMessage('Signed in successfully. Redirecting to your dashboard.');
      window.setTimeout(() => navigate(targetPath, { replace: true }), 250);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'Unable to sign in at the moment.'));
      setValidationMessages(extractValidationMessages(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Sign in"
      eyebrow="CampusSphere sign in"
      description="Use your email, student register number, or employee ID to access your CampusSphere workspace."
    >
      <SuccessBanner message={successMessage} />
      <ErrorBanner message={errorMessage} />
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="field" htmlFor="identifier">
          <span className="field__label">Email, register number, or employee ID</span>
          <input
            id="identifier"
            name="identifier"
            className="input"
            value={form.identifier}
            onChange={event => setForm(current => ({ ...current, identifier: event.target.value }))}
            autoComplete="username"
            inputMode="email"
            placeholder="admin@campussphere.local"
          />
          <span className="field__help">Use the identifier issued by your institution. Passwords stay out of plain text at every step.</span>
        </label>
        <PasswordInput
          id="password"
          name="password"
          value={form.password}
          onChange={event => setForm(current => ({ ...current, password: event.target.value }))}
          helperText="Passwords are encrypted and never stored in plain text."
        />
        <label className="checkbox">
          <input
            type="checkbox"
            checked={form.rememberMe}
            onChange={event => setForm(current => ({ ...current, rememberMe: event.target.checked }))}
          />
          <span>Remember me on this device</span>
        </label>
        <ValidationMessages messages={validationMessages} />
        <LoadingButton loading={loading} type="submit" fullWidth>
          Sign in
        </LoadingButton>
      </form>
      <div className="auth-card__footer">
        <Link to={APP_ROUTES.forgotPassword}>Forgot password?</Link>
        <Link to={APP_ROUTES.register}>Student registration</Link>
      </div>
      <div className="auth-card__subnote">
        <Button variant="secondary" as={Link} to={APP_ROUTES.home} size="sm">
          Back to home
        </Button>
      </div>
    </AuthCard>
  );
}

function validateLoginForm(form) {
  const messages = [];
  const identifier = form.identifier.trim();

  if (!identifier) {
    messages.push('Enter an email address, register number, or employee ID.');
  } else if (identifier.length > 160) {
    messages.push('The login identifier is too long.');
  }

  if (!form.password.trim()) {
    messages.push('Enter your password.');
  } else if (form.password.length < 8) {
    messages.push('Password must be at least 8 characters long.');
  }

  return messages;
}
