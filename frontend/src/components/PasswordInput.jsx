import { useState } from 'react';
import Input from './Input';
import Button from './Button';

export default function PasswordInput({ label = 'Password', id, helperText, autoComplete = 'current-password', ...props }) {
  const [visible, setVisible] = useState(false);

  return (
    <div className="password-input">
      <Input
        id={id}
        label={label}
        helperText={helperText}
        type={visible ? 'text' : 'password'}
        autoComplete={autoComplete}
        {...props}
      />
      <Button
        type="button"
        variant="secondary"
        size="sm"
        className="password-input__toggle"
        onClick={() => setVisible(current => !current)}
        aria-label={visible ? 'Hide password' : 'Show password'}
      >
        {visible ? 'Hide' : 'Show'}
      </Button>
    </div>
  );
}
