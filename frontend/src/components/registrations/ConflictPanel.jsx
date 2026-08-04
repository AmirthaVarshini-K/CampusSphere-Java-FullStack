import Badge from '../Badge';
import Card from '../Card';
import EmptyState from '../EmptyState';

export default function ConflictPanel({ preview, loading = false }) {
  if (loading) {
    return (
      <Card className="conflict-panel">
        <Badge tone="neutral">Checking</Badge>
        <p className="conflict-panel__title">Checking registration details</p>
        <p className="conflict-panel__description">We are validating your registration against the current schedule and capacity.</p>
      </Card>
    );
  }

  if (!preview) {
    return (
      <EmptyState
        title="No validation summary yet"
        description="Use the registration form to preview conflicts, seat availability, and waitlist behavior before submitting."
      />
    );
  }

  return (
    <Card className="conflict-panel">
      <div className="conflict-panel__header">
        <Badge tone={preview.duplicateRegistration || preview.conflicts?.length ? 'warning' : preview.expectedStatus === 'APPROVED' ? 'success' : 'neutral'}>
          {preview.expectedStatus ? preview.expectedStatus.replace('_', ' ') : 'Preview'}
        </Badge>
        <strong>{preview.eventTitle}</strong>
      </div>

      <div className="conflict-panel__summary">
        <div>
          <span>Registration</span>
          <strong>{preview.registrationOpen ? 'Open' : 'Closed'}</strong>
        </div>
        <div>
          <span>Seats</span>
          <strong>{preview.capacityAvailable ? 'Available' : 'Full'}</strong>
        </div>
        <div>
          <span>Waitlist</span>
          <strong>{preview.waitlistEnabled ? 'Enabled' : 'Disabled'}</strong>
        </div>
        <div>
          <span>Queue position</span>
          <strong>{preview.waitlistPosition ?? '-'}</strong>
        </div>
      </div>

      {preview.messages?.length ? (
        <ul className="conflict-panel__messages">
          {preview.messages.map(message => <li key={message}>{message}</li>)}
        </ul>
      ) : null}

      {preview.conflicts?.length ? (
        <div className="conflict-panel__conflicts">
          {preview.conflicts.map(conflict => (
            <article key={`${conflict.eventId}-${conflict.eventStartDateTime}`} className="conflict-panel__conflict">
              <strong>{conflict.eventTitle}</strong>
              <span>{new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium' }).format(new Date(conflict.eventStartDateTime))}</span>
              <span>{new Intl.DateTimeFormat('en-IN', { timeStyle: 'short' }).format(new Date(conflict.eventStartDateTime))} to {new Intl.DateTimeFormat('en-IN', { timeStyle: 'short' }).format(new Date(conflict.eventEndDateTime))}</span>
              <p>{conflict.explanation}</p>
            </article>
          ))}
        </div>
      ) : null}
    </Card>
  );
}
