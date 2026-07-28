export default function Select({ label, helperText, id, children, className = '', ...props }) {
  return (
    <label className="field" htmlFor={id}>
      {label && <span className="field__label">{label}</span>}
      <select id={id} className={`select ${className}`} {...props}>
        {children}
      </select>
      {helperText && <span className="field__help">{helperText}</span>}
    </label>
  );
}
