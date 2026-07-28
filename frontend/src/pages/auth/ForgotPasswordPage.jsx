import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import LoadingButton from '../../components/LoadingButton';
import { ErrorBanner, SuccessBanner } from '../../components/Banner';
import { APP_ROUTES } from '../../constants/routes';
import { authApi } from '../../services/authApi';
import { extractValidationMessages, getApiErrorMessage } from '../../utils/apiErrors';
import ValidationMessages from '../../components/ValidationMessages';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [validationMessages, setValidationMessages] = useState([]);
  const [resetToken, setResetToken] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    const nextValidationMessages = validateEmail(email);
    setValidationMessages(nextValidationMessages);
    setErrorMessage('');
    setSuccessMessage('');

    if (nextValidationMessages.length > 0) {
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.forgotPassword({ email: email.trim() });
      const nextToken = response.data.data?.resetToken ?? '';
      setResetToken(nextToken);
      setSuccessMessage('Password reset instructions are ready. Use the token below to continue in this demo environment.');
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'Unable to process the reset request right now.'));
      setValidationMessages(extractValidationMessages(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Forgot password"
      eyebrow="CampusSphere recovery"
      description="Request a secure reset token to recover your account."
    >
      <SuccessBanner message={successMessage} />
      <ErrorBanner message={errorMessage} />
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="field" htmlFor="email">
          <span className="field__label">Email</span>
          <input id="email" type="email" className="input" value={email} onChange={event => setEmail(event.target.value)} autoComplete="email" />
        </label>
        <ValidationMessages messages={validationMessages} />
        <LoadingButton loading={loading} type="submit" fullWidth>
          Generate reset token
        </LoadingButton>
      </form>
      {resetToken && (
        <div className="token-panel">
          <p className="token-panel__label">Reset token</p>
          <p className="field__help">This token is shown for the current demo flow only.</p>
          <code>{resetToken}</code>
        </div>
      )}
      <div className="auth-card__footer">
        <Link to={APP_ROUTES.login}>Back to sign in</Link>
        <Link to={APP_ROUTES.resetPassword}>Have a token already?</Link>
      </div>
      <div className="auth-card__subnote">
        <Button variant="secondary" as={Link} to={APP_ROUTES.home} size="sm">
          Back to home
        </Button>
      </div>
    </AuthCard>
  );
}

function validateEmail(email) {
  const messages = [];
  if (!email.trim()) {
    messages.push('Enter your email address.');
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
    messages.push('Enter a valid email address.');
  }
  return messages;
}
