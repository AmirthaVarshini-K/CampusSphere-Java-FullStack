import { useEffect, useRef, useState } from 'react';

export default function useToastQueue() {
  const [toasts, setToasts] = useState([]);
  const timers = useRef(new Map());

  useEffect(() => {
    return () => {
      timers.current.forEach(timerId => window.clearTimeout(timerId));
      timers.current.clear();
    };
  }, []);

  function pushToast(message, tone = 'success') {
    if (!message) {
      return;
    }

    const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    setToasts(current => [...current, { id, message, tone }]);
    const timerId = window.setTimeout(() => {
      setToasts(current => current.filter(toast => toast.id !== id));
      timers.current.delete(id);
    }, 3500);
    timers.current.set(id, timerId);
  }

  return { toasts, pushToast };
}
