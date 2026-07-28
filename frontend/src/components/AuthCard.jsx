import Card from './Card';

export default function AuthCard({ children, title, description, className = '', eyebrow = 'CampusSphere secure access' }) {
  return (
    <Card elevated className={`auth-card ${className}`}>
      <span className="auth-card__eyebrow">{eyebrow}</span>
      <div className="auth-card__header">
        <h2>{title}</h2>
        {description && <p>{description}</p>}
      </div>
      {children}
    </Card>
  );
}
