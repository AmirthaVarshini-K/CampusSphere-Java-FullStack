import Button from './Button';
import { classNames } from '../utils/classNames';

export default function Modal({
  open,
  title,
  description,
  children,
  onClose,
  onSubmit,
  submitLabel = 'Save',
  cancelLabel = 'Cancel',
  loading = false,
  size = 'md'
}) {
  if (!open) {
    return null;
  }

  return (
    <div className="dialog-backdrop" role="presentation" onClick={onClose}>
      <div
        className={classNames('dialog', size === 'lg' && 'dialog--lg')}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onClick={event => event.stopPropagation()}
      >
        <div className="dialog__header">
          <div>
            <h2 id="modal-title" className="dialog__title">
              {title}
            </h2>
            {description && <p className="dialog__description">{description}</p>}
          </div>
          <Button variant="secondary" size="sm" onClick={onClose} aria-label="Close dialog">
            Close
          </Button>
        </div>

        <form className="dialog__form" onSubmit={onSubmit}>
          <div className="dialog__content">{children}</div>
          <div className="dialog__actions">
            <Button variant="secondary" onClick={onClose} type="button">
              {cancelLabel}
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Saving...' : submitLabel}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
