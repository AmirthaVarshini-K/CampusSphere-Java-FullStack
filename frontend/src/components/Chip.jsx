import { classNames } from '../utils/classNames';

export default function Chip({ children, active = false, className = '' }) {
  return <span className={classNames('chip', active && 'chip--active', className)}>{children}</span>;
}
