export default function Textarea({ label, helperText, id, className = '', ...props }) {
  return (
    <label className="field" htmlFor={id}>
      {label && <span className="field__label">{label}</span>}
      <textarea id={id} className={`textarea ${className}`} {...props} />
      {helperText && <span className="field__help">{helperText}</span>}
    </label>
  );
}
