import React, { useState } from 'react';
import { AppRoutes } from './routes/AppRoutes';

export const App: React.FC = () => {
  // Read initial user session from localStorage
  const [currentUser, setCurrentUser] = useState<{ username: string; role: string } | null>(() => {
    const savedUser = localStorage.getItem('username');
    const savedRole = localStorage.getItem('role');
    return savedUser && savedRole ? { username: savedUser, role: savedRole } : null;
  });

  const [logoutMsg, setLogoutMsg] = useState<string>('');

  const handleLoginSuccess = (username: string, role: string) => {
    // Save state locally and update React state
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
    setLogoutMsg('');
    setCurrentUser({ username, role });
  };

  const handleLogout = () => {
    // Clear storage and state
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('token');
    setCurrentUser(null);
    setLogoutMsg('You have successfully logged out.');
  };

  return (
    <AppRoutes
      currentUser={currentUser}
      logoutMsg={logoutMsg}
      onLoginSuccess={handleLoginSuccess}
      onLogout={handleLogout}
    />
  );
};

export default App;