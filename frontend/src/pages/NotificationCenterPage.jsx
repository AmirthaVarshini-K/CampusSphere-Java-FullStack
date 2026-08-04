import { useEffect, useMemo, useState } from 'react';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingSkeleton from '../components/LoadingSkeleton';
import SearchBar from '../components/SearchBar';
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
  const dayKey = value => {
    const itemDate = new Date(value);
    const diff = Math.floor((Date.UTC(today.getFullYear(), today.getMonth(), today.getDate()) - Date.UTC(itemDate.getFullYear(), itemDate.getMonth(), itemDate.getDate())) / 86400000);
    if (diff <= 0) {
      return 'Today';
    }
    if (diff === 1) {
      return 'Yesterday';
    }
    return 'Earlier';
  };
  const groups = new Map();
  items.forEach(item => {
    const dateKey = item.createdAt ? dayKey(item.createdAt) : 'Earlier';
    if (!groups.has(dateKey)) {
      groups.set(dateKey, []);
    }
    groups.get(dateKey).push(item);
  });
  return [...groups.entries()];
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
    <div className="dashboard-page notification-center">
      <section className="dashboard-page__hero workspace-hero">
        <div>
          <Badge tone="neutral">Notifications</Badge>
          <h1>Notification centre</h1>
          <p>Track registration updates, team invitations, and status changes in one place.</p>
        </div>
        <div className="workspace-actions">
          <Badge tone={unreadCount ? 'warning' : 'neutral'}>{unreadCount} unread</Badge>
          <Button variant="secondary" size="sm" onClick={handleMarkAllRead} disabled={!unreadCount}>
            Mark all read
          </Button>
        </div>
      </section>

      <Card elevated className="workspace-table-card">
        <div className="workspace-table-card__meta">
          <div>
            <h2>Inbox</h2>
            <p>Use filters to narrow messages by category or unread state. Recent items are grouped by day.</p>
          </div>
        </div>
        <div className="workspace-filters">
          <SearchBar value={query} onChange={setQuery} placeholder="Search notifications" />
        </div>
        <Tabs items={FILTER_TABS} activeKey={filter} onChange={setFilter} />

        {loading ? (
          <LoadingSkeleton lines={5} />
        ) : error ? (
          <ErrorState title="Notifications unavailable" description={error} onRetry={loadNotifications} />
        ) : filtered.length ? (
          <div className="notification-center__groups">
            {groups.map(([label, items]) => (
              <section key={label} className="notification-center__group">
                <div className="notification-center__group-title">
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
            onAction={() => { setFilter('all'); setQuery(''); }}
          />
        )}
      </Card>

      {toasts.map(toast => <Toast key={toast.id} message={toast.message} tone={toast.tone} />)}
    </div>
  );
}
