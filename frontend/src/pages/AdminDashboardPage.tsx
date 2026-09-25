import React, { useState } from 'react';
import { Header } from '../components/Header';
import { UserDetailsTable } from '../components/UserDetailsTable';
import { AddUserForm } from '../components/AddUserForm';
import { ChangeRoleModal } from '../components/ChangeRoleModal';
import { DeleteUserModal } from '../components/DeleteUserModal';
import { ChangePasswordModal } from '../components/ChangePasswordModal';

interface AdminDashboardProps {
  username: string;
  activeTab: 'DETAILS' | 'ADD' | 'ROLE' | 'DELETE' | 'REPORT';
  onSelectTab: (tab: 'DETAILS' | 'ADD' | 'ROLE' | 'DELETE' | 'REPORT') => void;
  onLogout: () => void;
  onHomeClick: () => void;
}


export const AdminDashboardPage: React.FC<AdminDashboardProps> = ({
  username,
  activeTab,
  onSelectTab,
  onLogout,
  onHomeClick,
}) => {
  const [showChangePassword, setShowChangePassword] = useState(false);

  return (
    <div>
      <Header
        username={username}
        onOpenChangePassword={() => setShowChangePassword(true)}
        onLogout={onLogout}
        onHomeClick={onHomeClick}
      />

      {/* Navigation Tab Bar */}
      <div className="bg-light p-2 d-flex gap-1 border-bottom">
        <button
          className={`nav-tab-btn ${activeTab === 'DETAILS' ? 'active' : ''}`}
          onClick={() => onSelectTab('DETAILS')}
        >
          USER DETAILS
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'ADD' ? 'active' : ''}`}
          onClick={() => onSelectTab('ADD')}
        >
          ADD USER
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'ROLE' ? 'active' : ''}`}
          onClick={() => onSelectTab('ROLE')}
        >
          CHANGE ROLE
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'DELETE' ? 'active' : ''}`}
          onClick={() => onSelectTab('DELETE')}
        >
          DELETE USER
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'REPORT' ? 'active' : ''}`}
          onClick={() => onSelectTab('REPORT')}
        >
          REPORT
        </button>
      </div>

      {/* Active Tab Content */}
      <div className="container mt-4">
        {activeTab === 'DETAILS' && <UserDetailsTable />}
        {activeTab === 'ADD' && <AddUserForm onSuccess={() => onSelectTab('DETAILS')} />}
        {activeTab === 'ROLE' && <ChangeRoleModal onRoleUpdated={() => onSelectTab('DETAILS')} />}
        {activeTab === 'DELETE' && <DeleteUserModal onDeleted={() => onSelectTab('DETAILS')} />}
        {activeTab === 'REPORT' && (
          <div className="text-center p-5">
            <h4 className="fw-bold mb-4">SALES REPORT</h4>
            <div className="d-flex justify-content-center">
              <div
                className="rounded-circle border border-primary d-flex align-items-center justify-content-center"
                style={{ width: '250px', height: '250px', backgroundColor: '#e0f7fa' }}
              >
                <span className="fw-bold text-primary">[ Sales Pie Chart ]</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {showChangePassword && (
        <ChangePasswordModal
          username={username}
          onClose={() => setShowChangePassword(false)}
          onSuccess={() => {
            setShowChangePassword(false);
            onLogout();
          }}
        />
      )}
    </div>
  );
};