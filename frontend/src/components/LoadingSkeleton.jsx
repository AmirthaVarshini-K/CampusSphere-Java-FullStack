export default function LoadingSkeleton({ lines = 3 }) {
  return (
    <div className="skeleton" aria-busy="true" aria-live="polite">
      {Array.from({ length: lines }).map((_, index) => (
        <span key={index} className="skeleton__line" />
      ))}
    </div>
  );
}
