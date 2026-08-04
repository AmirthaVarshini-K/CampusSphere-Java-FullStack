import { useEffect, useMemo, useState } from 'react';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import SearchBar from '../components/SearchBar';
import SectionHeading from '../components/SectionHeading';
import Tabs from '../components/Tabs';
import { Toast } from '../components/Toast';
import NotificationItem from '../components/registrations/NotificationItem';
import { registrationApi } from '../services/registrationApi';
import useToastQueue from '../hooks/useToastQueue';
import { getApiErrorMessage } from '../utils/apiErrors';

const FILTER_TABS = [
  { key: 'all', label: 'All' },
  { key: 'unread', label: 'Unread' },
  { key: 'registrations', label: 'Registrations' },
  { key: 'teams', label: 'Teams' },
  { key: 'events', label: 'Events' }
];

function groupByDay(items) {
  const today = new Date();
  return items.reduce((groups, item) => {
    const date = item.createdAt ? new Date(item.createdAt) : null;
    const diff = date
      ? Math.floor((Date.UTC(today.getFullYear(), today.getMonth(), today.getDate()) - Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())) / 86400000)
      : 2;
    const label = diff <= 0 ? 'Today' : diff === 1 ? 'Yesterday' : 'Earlier';
    if (!groups[label]) {
      groups[label] = [];
    }
    groups[label].push(item);
    return groups;
  }, {});
}

export default function NotificationCenterPage() {
  const { toasts, pushToast } = useToastQueue();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('all');

  async function loadNotifications() {
    setLoading(true);
    setError('');
    try {
      const response = await registrationApi.listNotifications();
      setNotifications(response?.data?.data ?? []);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Unable to load notifications right now.'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadNotifications();
  }, []);

  const unreadCount = notifications.filter(item => !item.readAt).length;
  const filtered = useMemo(() => {
    return notifications.filter(item => {
      if (filter === 'unread' && item.readAt) return false;
      if (filter === 'registrations' && item.relatedEntityType !== 'EventRegistration') return false;
      if (filter === 'teams' && item.relatedEntityType !== 'TeamInvitation') return false;
      if (filter === 'events' && item.relatedEntityType === 'TeamInvitation') return false;
      if (query && !`${item.title} ${item.message}`.toLowerCase().includes(query.toLowerCase())) return false;
      return true;
    });
  }, [filter, notifications, query]);

  const groups = groupByDay(filtered);

  async function handleMarkRead(id) {
    try {
      const response = await registrationApi.markNotificationRead(id);
      const updated = response?.data?.data;
      setNotifications(current => current.map(item => (item.id === updated?.id ? updated : item)));
      pushToast('Notification marked as read.');
    } catch (err) {
      pushToast(getApiErrorMessage(err, 'Unable to update the notification.'), 'error');
    }
  }

  async function handleMarkAllRead() {
    try {
      await registrationApi.markAllNotificationsRead();
      setNotifications(current => current.map(item => (item.readAt ? item : { ...item, readAt: new Date().toISOString() })));
      pushToast('All notifications marked as read.');
    } catch (err) {
      pushToast(getApiErrorMessage(err, 'Unable to update the notifications.'), 'error');
    }
  }

  return (
    <div className="page-stack notification-center">
      <SectionHeading
        eyebrow="Notifications"
        title="Notification centre"
        description="Track registration updates, team invitations, and status changes in one calm surface."
        action={<Badge tone={unreadCount ? 'warning' : 'neutral'}>{unreadCount} unread</Badge>}
      />

      <Card elevated className="notification-center__panel">
        <div className="notification-center__toolbar">
          <SearchBar value={query} onChange={setQuery} placeholder="Search notifications" />
          <Button variant="secondary" size="sm" onClick={handleMarkAllRead} disabled={!unreadCount}>
            Mark all read
          </Button>
        </div>
        <Tabs items={FILTER_TABS} activeKey={filter} onChange={setFilter} />

        {loading ? (
          <LoadingSkeleton lines={5} />
        ) : error ? (
          <ErrorState title="Notifications unavailable" description={error} onRetry={loadNotifications} />
        ) : filtered.length ? (
          <div className="notification-center__groups">
            {Object.entries(groups).map(([label, items]) => (
              <section key={label} className="notification-center__group">
                <div className="notification-center__group-header">
                  <h3>{label}</h3>
                  <Badge tone="neutral">{items.length}</Badge>
                </div>
                <div className="notification-center__list">
                  {items.map(item => (
                    <NotificationItem key={item.id} item={item} onMarkRead={handleMarkRead} />
                  ))}
                </div>
              </section>
            ))}
          </div>
        ) : (
          <EmptyState
            title="No notifications match the current filter"
            description="Try a different tab or clear the search field to review recent updates."
            actionLabel="Clear filters"
            onAction={() => {
              setFilter('all');
              setQuery('');
            }}
          />
        )}
      </Card>

      {toasts.map(toast => (
        <Toast key={toast.id} message={toast.message} tone={toast.tone} />
      ))}
    </div>
  );
}
