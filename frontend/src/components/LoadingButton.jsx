import Button from './Button';

export default function LoadingButton({ loading, children, ...props }) {
  return (
    <Button {...props} disabled={loading || props.disabled}>
      {loading ? 'Please wait...' : children}
    </Button>
  );
}
