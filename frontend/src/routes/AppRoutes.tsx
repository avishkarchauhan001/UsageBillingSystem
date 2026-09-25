import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { HomePage } from '../pages/HomePage';
import { SignupPage } from '../pages/SignupPage';
import { LoginPage } from '../pages/LoginPage';
import { AdminDashboardPage } from '../pages/AdminDashboardPage';
import { CustomerDashboardPage } from '../pages/CustomerDashboardPage';
import { OperatorDashboardPage } from '../pages/OperatorDashboardPage';

interface AppRoutesProps {
  currentUser: { username: string; role: string } | null;
  logoutMsg: string;
  onLoginSuccess: (username: string, role: string) => void;
  onLogout: () => void;
}

export const AppRoutes: React.FC<AppRoutesProps> = ({
  currentUser,
  logoutMsg,
  onLoginSuccess,
  onLogout,
}) => {
  const [activeAdminTab, setActiveAdminTab] = useState<'DETAILS' | 'ADD' | 'ROLE' | 'DELETE' | 'REPORT'>('DETAILS');

  const renderDashboard = () => {
    if (!currentUser) return <Navigate to="/login" replace />;

    const role = (currentUser.role || '').toUpperCase();
    if (role === 'CUSTOMER') {
      return (
        <CustomerDashboardPage
          username={currentUser.username}
          onLogout={onLogout}
        />
      );
    }
    if (role === 'OPERATOR') {
      return (
        <OperatorDashboardPage
          username={currentUser.username}
          onLogout={onLogout}
        />
      );
    }
    return (
      <AdminDashboardPage
        username={currentUser.username}
        activeTab={activeAdminTab}
        onSelectTab={(tab) => setActiveAdminTab(tab)}
        onLogout={onLogout}
        onHomeClick={() => setActiveAdminTab('DETAILS')}
      />
    );
  };

  return (
    <BrowserRouter>
      <Routes>
        {/* 1. Public Home Page */}
        <Route path="/" element={<HomePage />} />

        {/* 2. Public Signup Page */}
        <Route path="/signup" element={<SignupPage />} />

        {/* 3. Public Login Page */}
        <Route
          path="/login"
          element={
            currentUser ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <LoginPage
                onLoginSuccess={onLoginSuccess}
                logoutMessage={logoutMsg}
              />
            )
          }
        />

        {/* 4. Protected Role-Based Dashboard Route */}
        <Route path="/dashboard" element={renderDashboard()} />

        {/* Fallback route */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};