export default function ValidationMessages({ messages = [] }) {
  if (!messages.length) {
    return null;
  }

  return (
    <ul className="validation-messages" aria-live="polite">
      {messages.map(message => (
        <li key={message}>{message}</li>
      ))}
    </ul>
  );
}
