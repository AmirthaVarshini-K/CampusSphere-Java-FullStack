export default function FilterPanel({ children, title = 'Filters' }) {
  return (
    <aside className="filter-panel" aria-label={title}>
      <div className="filter-panel__header">
        <h3>{title}</h3>
      </div>
      <div className="filter-panel__body">{children}</div>
    </aside>
  );
}
