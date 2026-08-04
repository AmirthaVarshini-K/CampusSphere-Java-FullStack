import Badge from './Badge';

export default function SectionHeading({ eyebrow, title, description, action }) {
  return (
    <div className="section-heading">
      {eyebrow && <Badge tone="neutral">{eyebrow}</Badge>}
      <div className="section-heading__copy">
        <h2>{title}</h2>
        {description && <p>{description}</p>}
      </div>
      {action && <div className="section-heading__action">{action}</div>}
    </div>
  );
}
