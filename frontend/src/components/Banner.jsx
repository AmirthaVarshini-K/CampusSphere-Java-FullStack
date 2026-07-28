export function SuccessBanner({ message }) {
  return message ? <div className="banner banner--success">{message}</div> : null;
}

export function ErrorBanner({ message }) {
  return message ? <div className="banner banner--error">{message}</div> : null;
}
