import Card from './Card';
import Avatar from './Avatar';
import RoleBadge from './RoleBadge';

export default function ProfileCard({ user }) {
  if (!user) {
    return null;
  }

  return (
    <Card className="profile-card" elevated>
      <div className="profile-card__header">
        <Avatar src={user.profilePictureUrl} name={user.fullName} size="lg" />
        <div>
          <h3>{user.fullName}</h3>
          <p>{user.email}</p>
        </div>
      </div>
      <div className="profile-card__meta">
        <RoleBadge role={user.roles?.[0]?.code ?? 'STUDENT'} />
        <span>Status: {user.status}</span>
      </div>
    </Card>
  );
}
