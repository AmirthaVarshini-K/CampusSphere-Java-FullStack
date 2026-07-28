export default function Input({ label, helperText, id, className = '', ...props }) {
  return (
    <label className="field" htmlFor={id}>
      {label && <span className="field__label">{label}</span>}
      <input id={id} className={`input ${className}`} {...props} />
      {helperText && <span className="field__help">{helperText}</span>}
    </label>
  );
}
