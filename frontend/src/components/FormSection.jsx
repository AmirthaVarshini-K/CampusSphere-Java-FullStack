export default function FormSection({ title, description, children }) {
  return (
    <section className="form-section">
      <header className="form-section__header">
        <h3>{title}</h3>
        {description && <p>{description}</p>}
      </header>
      <div className="form-section__body">{children}</div>
    </section>
  );
}
