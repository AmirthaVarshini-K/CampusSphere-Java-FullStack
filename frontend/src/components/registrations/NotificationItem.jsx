import Button from '../Button';
import Badge from '../Badge';
import { classNames } from '../../utils/classNames';

export default function NotificationItem({ item, onMarkRead }) {
  const createdAt = item.createdAt ? new Date(item.createdAt) : null;
  const icon = item.severity === 'success' ? '+' : item.severity === 'warning' ? '!' : '*';

  return (
    <article className={classNames('notification-item', !item.readAt && 'notification-item--unread')}>
      <div className="notification-item__icon" aria-hidden="true">
        {icon}
      </div>
      <div className="notification-item__body">
        <div className="notification-item__top">
          <strong>{item.title}</strong>
          <Badge tone={item.readAt ? 'neutral' : 'warning'}>{item.readAt ? 'Read' : 'Unread'}</Badge>
        </div>
        <p>{item.message}</p>
        <div className="notification-item__meta">
          <span>{createdAt ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(createdAt) : '-'}</span>
          <span>{item.targetRoute ?? '/dashboard/notifications'}</span>
        </div>
      </div>
      {!item.readAt && (
        <Button variant="secondary" size="sm" onClick={() => onMarkRead(item.id)}>
          Mark read
        </Button>
      )}
    </article>
  );
}
