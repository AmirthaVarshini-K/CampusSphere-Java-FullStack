import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthCard from '../../components/AuthCard';
import Button from '../../components/Button';
import LoadingButton from '../../components/LoadingButton';
import PasswordInput from '../../components/PasswordInput';
import { ErrorBanner, SuccessBanner } from '../../components/Banner';
import { APP_ROUTES } from '../../constants/routes';
import { authApi } from '../../services/authApi';
import { extractValidationMessages, getApiErrorMessage } from '../../utils/apiErrors';
import ValidationMessages from '../../components/ValidationMessages';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState(searchParams.get('token') ?? '');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [validationMessages, setValidationMessages] = useState([]);

  async function handleSubmit(event) {
    event.preventDefault();
    const nextValidationMessages = validateResetForm({ token, newPassword, confirmPassword });
    setValidationMessages(nextValidationMessages);
    setErrorMessage('');
    setSuccessMessage('');

    if (nextValidationMessages.length > 0) {
      return;
    }

    setLoading(true);
    try {
      await authApi.resetPassword({ token: token.trim(), newPassword, confirmPassword });
      setSuccessMessage('Password has been reset successfully. You can sign in again with the new password.');
      setToken('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'Unable to reset the password right now.'));
      setValidationMessages(extractValidationMessages(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Reset password"
      eyebrow="CampusSphere recovery"
      description="Enter the token you received and choose a new strong password."
    >
      <SuccessBanner message={successMessage} />
      <ErrorBanner message={errorMessage} />
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="field" htmlFor="token">
          <span className="field__label">Reset token</span>
          <textarea
            id="token"
            className="textarea textarea--token"
            value={token}
            onChange={event => setToken(event.target.value)}
            placeholder="Paste the reset token here"
          />
        </label>
        <PasswordInput
          id="newPassword"
          autoComplete="new-password"
          label="New password"
          value={newPassword}
          onChange={event => setNewPassword(event.target.value)}
          helperText="Choose a password that you have not used on this account before."
        />
        <PasswordInput
          id="confirmPassword"
          autoComplete="new-password"
          label="Confirm new password"
          value={confirmPassword}
          onChange={event => setConfirmPassword(event.target.value)}
        />
        <ValidationMessages messages={validationMessages} />
        <LoadingButton loading={loading} type="submit" fullWidth>
          Reset password
        </LoadingButton>
      </form>
      <div className="auth-card__footer">
        <Link to={APP_ROUTES.forgotPassword}>Request another token</Link>
        <Link to={APP_ROUTES.login}>Back to sign in</Link>
      </div>
      <div className="auth-card__subnote">
        <Button variant="secondary" as={Link} to={APP_ROUTES.home} size="sm">
          Back to home
        </Button>
      </div>
    </AuthCard>
  );
}

function validateResetForm({ token, newPassword, confirmPassword }) {
  const messages = [];

  if (!token.trim()) messages.push('Reset token is required.');
  if (!newPassword) messages.push('New password is required.');
  if (!confirmPassword) messages.push('Confirm password is required.');
  if (newPassword && confirmPassword && newPassword !== confirmPassword) messages.push('Password confirmation does not match.');

  return messages;
}
