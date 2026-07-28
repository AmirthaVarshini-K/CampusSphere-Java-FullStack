import { classNames } from '../utils/classNames';

export default function StatusIndicator({ label, tone = 'success' }) {
  return (
    <span className={classNames('status-indicator', `status-indicator--${tone}`)}>
      <span className="status-indicator__dot" aria-hidden="true" />
      {label}
    </span>
  );
}
