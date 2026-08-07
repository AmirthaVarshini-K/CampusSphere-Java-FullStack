import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import Input from '../components/Input';
import LoadingButton from '../components/LoadingButton';
import LoadingSkeleton from '../components/LoadingSkeleton';
import SectionHeading from '../components/SectionHeading';
import { Toast } from '../components/Toast';
import { certificateApi } from '../services/certificateApi';
import useToastQueue from '../hooks/useToastQueue';
import { getApiErrorMessage, isNetworkError } from '../utils/apiErrors';

function unwrap(response) {
  return response?.data?.data ?? null;
}

function formatDateTime(value) {
  return value ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '-';
}

export default function CertificateVerificationPage() {
  const { token: routeToken } = useParams();
  const navigate = useNavigate();
  const { toasts, pushToast } = useToastQueue();
  const [token, setToken] = useState(routeToken ?? '');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(Boolean(routeToken));
  const [error, setError] = useState('');

  useEffect(() => {
    if (routeToken) {
      verifyToken(routeToken);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [routeToken]);

  async function verifyToken(value) {
    const cleaned = (value ?? token).trim();
    if (!cleaned) {
      setError('Enter a verification token to continue.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await certificateApi.verifyCertificate(cleaned);
      setResult(unwrap(response));
      if (routeToken !== cleaned) {
        navigate(`/verify/${cleaned}`, { replace: true });
      }
      pushToast('Certificate verification completed.', 'success');
    } catch (apiError) {
      setResult(unwrap(apiError?.response) ?? null);
      setError(getApiErrorMessage(apiError, 'Unable to verify the certificate right now.'));
      pushToast(getApiErrorMessage(apiError, 'Unable to verify the certificate right now.'), isNetworkError(apiError) ? 'warning' : 'danger');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-stack certificate-verify-page">
      <SectionHeading
        eyebrow="Public verification"
        title="Verify a CampusSphere certificate"
        description="Enter a token or open the QR link printed on the certificate."
      />

      <Card elevated className="certificate-verify__panel">
        <form className="certificate-verify__form" onSubmit={event => { event.preventDefault(); verifyToken(token); }}>
          <Input label="Verification token" value={token} onChange={event => setToken(event.target.value)} placeholder="Paste the certificate token" />
          <LoadingButton loading={loading} type="submit">Verify certificate</LoadingButton>
        </form>

        {loading ? (
          <LoadingSkeleton lines={5} />
        ) : error ? (
          <ErrorState title="Verification unavailable" description={error} onRetry={() => verifyToken(token)} />
        ) : result ? (
          <div className="certificate-verify__result">
            <div className="certificate-verify__header">
              <div>
                <h3>{result.certificateNumber ?? 'Verification result'}</h3>
                <p>{result.message}</p>
              </div>
              <Badge tone={result.valid ? 'success' : result.revoked ? 'danger' : 'warning'}>{result.valid ? 'Valid' : result.revoked ? 'Revoked' : 'Invalid'}</Badge>
            </div>
            <dl className="certificate-verify__details">
              <div><dt>Recipient</dt><dd>{result.recipientName ?? '-'}</dd></div>
              <div><dt>Institution</dt><dd>{result.institutionName ?? '-'}</dd></div>
              <div><dt>Event</dt><dd>{result.eventTitle ?? '-'}</dd></div>
              <div><dt>Type</dt><dd>{result.certificateType ? result.certificateType.replace('_', ' ') : '-'}</dd></div>
              <div><dt>Issued</dt><dd>{formatDateTime(result.verifiedAt)}</dd></div>
              <div><dt>Status</dt><dd>{result.verificationStatus ?? '-'}</dd></div>
            </dl>
            {!result.valid && !result.revoked && <EmptyState title="Token not recognised" description="Double-check the token or scan the QR code again." actionLabel="Try again" onAction={() => verifyToken(token)} />}
          </div>
        ) : (
          <EmptyState title="Ready to verify" description="Paste a token from a CampusSphere certificate to confirm its authenticity." />
        )}
      </Card>

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
