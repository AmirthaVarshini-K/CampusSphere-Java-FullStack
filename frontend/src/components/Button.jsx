import { Link } from 'react-router-dom';
import { classNames } from '../utils/classNames';

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  as,
  type = 'button',
  className = '',
  ...props
}) {
  const Component = as || 'button';
  const resolvedType = Component === 'button' ? type : undefined;

  return (
    <Component
      type={resolvedType}
      className={classNames('button', `button--${variant}`, `button--${size}`, fullWidth && 'button--full', className)}
      {...props}
    >
      {children}
    </Component>
  );
}
