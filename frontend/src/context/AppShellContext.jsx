import React, { createContext, useContext, useMemo, useState } from 'react';

const AppShellContext = createContext(null);

export function AppShellProvider({ children }) {
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  const value = useMemo(() => ({
    isSidebarOpen,
    setSidebarOpen,
    toggleSidebar: () => setSidebarOpen(current => !current),
    closeSidebar: () => setSidebarOpen(false)
  }), [isSidebarOpen]);

  return <AppShellContext.Provider value={value}>{children}</AppShellContext.Provider>;
}

export function useAppShell() {
  const context = useContext(AppShellContext);
  if (!context) {
    throw new Error('useAppShell must be used inside AppShellProvider');
  }
  return context;
}
