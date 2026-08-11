import { classNames } from '../utils/classNames';

function BrandSymbol() {
  return (
    <svg
      className="brand-mark__symbol"
      viewBox="0 0 24 24"
      width="100%"
      height="100%"
      aria-hidden="true"
      focusable="false"
    >
      <circle
        cx="12"
        cy="12"
        r="8.65"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeDasharray="41.8 11.6"
        transform="rotate(-18 12 12)"
      />
      <path
        d="M7.2 15.65c1.15 1.28 2.65 1.98 4.4 1.98 2.95 0 5.3-1.86 5.3-4.26 0-1.69-.98-3.03-2.8-3.93"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="7.95" cy="7.9" r="1.05" fill="currentColor" />
      <circle cx="16.35" cy="8.8" r="0.95" fill="currentColor" />
      <circle cx="15.2" cy="16.25" r="1" fill="currentColor" />
    </svg>
  );
}

export default function BrandMark({ compact = false, className = '' }) {
  return (
    <div className={classNames('brand-mark', compact && 'brand-mark--compact', className)}>
      <span className="brand-mark__glyph">
        <BrandSymbol />
      </span>
      {compact && <span className="sr-only">CampusSphere</span>}
      <div className="brand-mark__copy">
        <strong>CampusSphere</strong>
        {!compact && <span>One platform for every event across every college.</span>}
      </div>
    </div>
  );
}
