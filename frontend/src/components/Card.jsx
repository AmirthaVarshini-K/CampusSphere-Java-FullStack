import { classNames } from '../utils/classNames';

export default function Card({ children, className = '', elevated = false, ...props }) {
  return (
    <section className={classNames('card', elevated && 'card--elevated', className)} {...props}>
      {children}
    </section>
  );
}
