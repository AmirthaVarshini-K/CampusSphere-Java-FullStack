import Button from './Button';

export default function ErrorState({ title = 'Something went wrong', description, onRetry }) {
  return (
    <div className="state-message state-message--error">
      <h3>{title}</h3>
      <p>{description}</p>
      {onRetry && <Button onClick={onRetry}>Retry</Button>}
    </div>
  );
}
