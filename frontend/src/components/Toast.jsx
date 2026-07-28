export function Toast({ message, tone = 'success' }) {
  return (
    <div className={`toast toast--${tone}`} role="status" aria-live="polite">
      {message}
    </div>
  );
}
