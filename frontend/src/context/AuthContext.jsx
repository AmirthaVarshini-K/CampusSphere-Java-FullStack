import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../services/authApi';
import { userApi } from '../services/userApi';
import { clearSession, readSession, setSessionNotice, writeSession } from '../services/session';
import { getApiErrorMessage } from '../utils/apiErrors';

const AuthContext = createContext(null);

function normalizeSession(session) {
  if (!session) {
    return null;
  }

  return {
    ...session,
    rememberMe: Boolean(session.rememberMe),
    user: session.user ?? null
  };
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => normalizeSession(readSession()));
  const [isAuthReady, setAuthReady] = useState(false);

  useEffect(() => {
    let active = true;

    async function hydrateSession() {
      const currentSession = normalizeSession(readSession());
      if (!currentSession?.accessToken) {
        if (active) {
          setSession(null);
          setAuthReady(true);
        }
        return;
      }

      try {
        const response = await userApi.me();
        if (!active) {
          return;
        }

        const nextSession = {
          ...currentSession,
          user: response.data.data
        };
        writeSession(nextSession);
        setSession(nextSession);
      } catch (error) {
        if (!active) {
          return;
        }

        setSessionNotice('Your session has expired. Please sign in again.');
        clearSession();
        setSession(null);
        console.warn(getApiErrorMessage(error, 'Unable to restore the active session.'));
      } finally {
        if (active) {
          setAuthReady(true);
        }
      }
    }

    hydrateSession();

    const onSessionChanged = () => {
      setSession(normalizeSession(readSession()));
    };

    window.addEventListener('campussphere:session-changed', onSessionChanged);

    return () => {
      active = false;
      window.removeEventListener('campussphere:session-changed', onSessionChanged);
    };
  }, []);

  const value = useMemo(() => {
    const isAuthenticated = Boolean(session?.accessToken);

    return {
      isAuthenticated,
      isAuthReady,
      user: session?.user ?? null,
      accessToken: session?.accessToken ?? null,
      refreshToken: session?.refreshToken ?? null,
      signIn: nextSession => {
        const normalizedSession = normalizeSession({
          ...nextSession,
          issuedAt: new Date().toISOString()
        });
        writeSession(normalizedSession);
        setSession(normalizedSession);
        setAuthReady(true);
      },
      refreshCurrentUser: async () => {
        if (!session?.accessToken) {
          return null;
        }

        const response = await userApi.me();
        const nextSession = {
          ...session,
          user: response.data.data
        };
        writeSession(nextSession);
        setSession(nextSession);
        return response.data.data;
      },
      renewSession: async () => {
        if (!session?.refreshToken) {
          return null;
        }

        const response = await authApi.refreshToken({ refreshToken: session.refreshToken });
        const nextSession = normalizeSession({
          ...response.data.data,
          rememberMe: session.rememberMe
        });
        writeSession(nextSession);
        setSession(nextSession);
        return nextSession;
      },
      signOut: async () => {
        try {
          if (session?.refreshToken) {
            await authApi.logout({ refreshToken: session.refreshToken });
          }
        } catch (error) {
          console.warn(getApiErrorMessage(error, 'Logout request could not be completed.'));
        } finally {
          clearSession();
          setSession(null);
          setAuthReady(true);
        }
      }
    };
  }, [isAuthReady, session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
