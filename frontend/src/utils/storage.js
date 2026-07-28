export function readStorage(key, fallback = null, storage = window.localStorage) {
  try {
    const value = storage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch {
    return fallback;
  }
}

export function writeStorage(key, value, storage = window.localStorage) {
  storage.setItem(key, JSON.stringify(value));
}
