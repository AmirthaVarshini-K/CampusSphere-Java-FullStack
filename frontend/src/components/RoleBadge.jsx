import Badge from './Badge';
import { getRoleLabel } from '../utils/auth';

export default function RoleBadge({ role }) {
  const isPrivileged = ['SUPER_ADMIN', 'INSTITUTION_ADMIN', 'ADMINISTRATOR'].includes(role);

  return <Badge tone={isPrivileged ? 'success' : 'neutral'}>{getRoleLabel(role)}</Badge>;
}
