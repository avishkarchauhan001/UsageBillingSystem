import React from 'react';

interface HeaderProps {
  username: string;
  onOpenChangePassword: () => void;
  onLogout: () => void;
  onHomeClick?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ username, onOpenChangePassword, onLogout, onHomeClick }) => {
  return (
    <div className="bg-primary text-white p-3 d-flex justify-content-between align-items-center">
      <div className="d-flex align-items-center gap-3">
        {onHomeClick && (
          <button className="btn btn-sm btn-light" onClick={onHomeClick} title="Home">
            🏠 Home
          </button>
        )}
        <h3 className="m-0 fw-bold">BILLING SYSTEM</h3>
      </div>
      <div className="text-end">
        <div>WELCOME <span className="fw-bold text-warning">{username.toUpperCase()}</span></div>
        <div className="mt-1">
          <button 
            className="btn btn-link text-white text-decoration-underline p-0 me-3 border-0 bg-transparent" 
            onClick={onOpenChangePassword}
          >
            Change password
          </button>
          <button className="btn btn-danger btn-sm fw-bold" onClick={onLogout}>
            LOGOUT
          </button>
        </div>
      </div>
    </div>
  );
};