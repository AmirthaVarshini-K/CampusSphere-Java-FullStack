export default function Avatar({ src, name, size = 'md' }) {
  const initials = name
    ? name.split(' ').slice(0, 2).map(part => part[0]?.toUpperCase()).join('')
    : 'CS';

  return (
    <div className={`avatar avatar--${size}`}>
      {src ? <img src={src} alt={name ?? 'Profile picture'} /> : <span aria-hidden="true">{initials}</span>}
    </div>
  );
}
