import { classNames } from '../utils/classNames';

const ICONS = {
  home: (
    <path d="M4 11.5L12 5l8 6.5V20a1 1 0 0 1-1 1h-4.5v-6h-5v6H5a1 1 0 0 1-1-1z" />
  ),
  calendar: (
    <>
      <path d="M6 3v3M18 3v3" />
      <rect x="3" y="5.5" width="18" height="15" rx="2.5" />
      <path d="M3 9h18" />
    </>
  ),
  chart: (
    <>
      <path d="M4 19V5" />
      <path d="M4 19h16" />
      <path d="M8 16V11" />
      <path d="M12 16V8" />
      <path d="M16 16v-6" />
    </>
  ),
  pieChart: (
    <>
      <path d="M12 3a9 9 0 1 0 9 9h-9z" />
      <path d="M12 3v9h9" />
    </>
  ),
  filter: (
    <path d="M4 6h16l-6 7v5l-4 2v-7z" />
  ),
  refresh: (
    <>
      <path d="M20 12a8 8 0 0 1-13.4 5.9" />
      <path d="M4 12a8 8 0 0 1 13.4-5.9" />
      <path d="M4 4v4h4" />
      <path d="M20 20v-4h-4" />
    </>
  ),
  users: (
    <>
      <path d="M17 20c0-3-2.5-5-5-5s-5 2-5 5" />
      <path d="M12 12.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z" />
      <path d="M20.5 20c0-2.1-1.3-3.8-3.2-4.5" />
      <path d="M16.8 6.8a3 3 0 0 1 0 5.9" />
    </>
  ),
  bell: (
    <>
      <path d="M9 19a3 3 0 0 0 6 0" />
      <path d="M5 17h14c-1.2-1.2-2-2.8-2-4.5V10a5 5 0 0 0-10 0v2.5C7 14.2 6.2 15.8 5 17Z" />
    </>
  ),
  search: (
    <>
      <circle cx="11" cy="11" r="5.5" />
      <path d="m15 15 4 4" />
    </>
  ),
  menu: (
    <>
      <path d="M4 7h16M4 12h16M4 17h16" />
    </>
  ),
  chevronRight: (
    <path d="m9 6 6 6-6 6" />
  ),
  arrows: (
    <>
      <path d="M7 7h10l-3-3M17 17H7l3 3" />
      <path d="M7 7l3 3M17 17l-3-3" />
    </>
  ),
  grid: (
    <>
      <rect x="4" y="4" width="7" height="7" rx="1.5" />
      <rect x="13" y="4" width="7" height="7" rx="1.5" />
      <rect x="4" y="13" width="7" height="7" rx="1.5" />
      <rect x="13" y="13" width="7" height="7" rx="1.5" />
    </>
  ),
  building: (
    <>
      <path d="M5 21V5.5l7-3 7 3V21" />
      <path d="M9 21v-5h6v5" />
      <path d="M9 9h.01M15 9h.01M9 13h.01M15 13h.01" />
    </>
  ),
  shield: (
    <path d="M12 3 19 6v5c0 5-3.3 8.7-7 10-3.7-1.3-7-5-7-10V6z" />
  ),
  usersSquare: (
    <>
      <rect x="3.5" y="3.5" width="17" height="17" rx="3" />
      <path d="M8 16c0-2 1.8-3.5 4-3.5s4 1.5 4 3.5" />
      <path d="M10 10.5a2 2 0 1 0 4 0 2 2 0 0 0-4 0Z" />
    </>
  ),
  plus: (
    <>
      <path d="M12 5v14M5 12h14" />
    </>
  ),
  logout: (
    <>
      <path d="M10 5H6.5A1.5 1.5 0 0 0 5 6.5v11A1.5 1.5 0 0 0 6.5 19H10" />
      <path d="M15 9l4 3-4 3" />
      <path d="M19 12H10" />
    </>
  ),
  pulse: (
    <path d="M3 12h4l2-6 4 12 2-6h6" />
  ),
  clock: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  arrowLeft: (
    <path d="M11 6 5 12l6 6M5 12h14" />
  ),
  chevronDown: (
    <path d="m6 9 6 6 6-6" />
  ),
  award: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="m9 12-1 8 4-2 4 2-1-8" />
    </>
  ),
  fileText: (
    <>
      <path d="M7 3h7l5 5v13H7z" />
      <path d="M14 3v5h5" />
      <path d="M10 12h4M10 16h6" />
    </>
  ),
  qrcode: (
    <>
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <path d="M14 14h2v2h-2zM18 14h2v2h-2zM14 18h2v2h-2zM18 18h2v2h-2z" />
    </>
  ),
  download: (
    <>
      <path d="M12 3v10" />
      <path d="m8 9 4 4 4-4" />
      <path d="M5 19h14" />
    </>
  ),
  eye: (
    <>
      <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z" />
      <circle cx="12" cy="12" r="2.5" />
    </>
  ),
  settings: (
    <>
      <circle cx="12" cy="12" r="3.2" />
      <path d="M19.4 15a8.5 8.5 0 0 0 .1-6l2-1.2-2-3.4-2.2.8a8.5 8.5 0 0 0-5.3-3l-.4-2.2H8.4l-.4 2.2a8.5 8.5 0 0 0-5.3 3L.5 4.4 2.5 7.8 4.6 9a8.5 8.5 0 0 0 0 6l-2.1 1.2 2 3.4 2.2-.8a8.5 8.5 0 0 0 5.3 3l.4 2.2h3.2l.4-2.2a8.5 8.5 0 0 0 5.3-3l2.2.8 2-3.4-2-1.2Z" />
    </>
  )
};

export default function Icon({ name, size = 20, className = '' }) {
  const content = ICONS[name] ?? ICONS.grid;

  return (
    <svg
      className={classNames('icon', className)}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {content}
    </svg>
  );
}
