import { readStorage, writeStorage } from '../utils/storage';

const SESSION_KEY = 'campussphere.session.local';
const TEMP_SESSION_KEY = 'campussphere.session.temp';
const NOTICE_KEY = 'campussphere.session.notice';

export function readSession() {
  return readStorage(SESSION_KEY, null, window.localStorage) ?? readStorage(TEMP_SESSION_KEY, null, window.sessionStorage);
}

export function writeSession(session) {
  const storageKey = session?.rememberMe ? SESSION_KEY : TEMP_SESSION_KEY;
  const storage = session?.rememberMe ? window.localStorage : window.sessionStorage;
  writeStorage(storageKey, session, storage);
  const otherStorage = session?.rememberMe ? window.sessionStorage : window.localStorage;
  otherStorage.removeItem(session?.rememberMe ? TEMP_SESSION_KEY : SESSION_KEY);
  window.dispatchEvent(new Event('campussphere:session-changed'));
}

export function clearSession() {
  window.localStorage.removeItem(SESSION_KEY);
  window.sessionStorage.removeItem(TEMP_SESSION_KEY);
  window.dispatchEvent(new Event('campussphere:session-changed'));
}

export function setSessionNotice(message) {
  if (!message) {
    window.sessionStorage.removeItem(NOTICE_KEY);
    return;
  }

  writeStorage(NOTICE_KEY, message, window.sessionStorage);
}

export function consumeSessionNotice() {
  const message = readStorage(NOTICE_KEY, '', window.sessionStorage);
  window.sessionStorage.removeItem(NOTICE_KEY);
  return typeof message === 'string' ? message : '';
}
