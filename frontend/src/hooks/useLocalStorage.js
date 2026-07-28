import { useEffect, useState } from 'react';
import { readStorage, writeStorage } from '../utils/storage';

export function useLocalStorage(key, fallbackValue) {
  const [value, setValue] = useState(() => readStorage(key, fallbackValue));

  useEffect(() => {
    writeStorage(key, value);
  }, [key, value]);

  return [value, setValue];
}
