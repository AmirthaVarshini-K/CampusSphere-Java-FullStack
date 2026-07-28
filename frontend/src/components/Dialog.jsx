import Button from './Button';

export default function Dialog({ open, title, description, confirmLabel = 'Continue', cancelLabel = 'Cancel', onConfirm, onClose }) {
  if (!open) {
    return null;
  }

  return (
    <div className="dialog-backdrop" role="presentation" onClick={onClose}>
      <div className="dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title" onClick={event => event.stopPropagation()}>
        <h2 id="dialog-title" className="dialog__title">{title}</h2>
        <p className="dialog__description">{description}</p>
        <div className="dialog__actions">
          <Button variant="secondary" onClick={onClose}>{cancelLabel}</Button>
          <Button onClick={onConfirm}>{confirmLabel}</Button>
        </div>
      </div>
    </div>
  );
}
