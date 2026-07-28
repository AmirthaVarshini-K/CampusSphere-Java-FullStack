import { readStorage, writeStorage } from '../utils/storage';

const SESSION_KEY = 'campussphere.session.local';
const TEMP_SESSION_KEY = 'campussphere.session.temp';

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
