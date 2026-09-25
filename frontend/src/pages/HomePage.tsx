import React from 'react';
import { useNavigate } from 'react-router-dom';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();

  const roles = [
    {
      name: 'Admin',
      roleKey: 'ADMIN',
      iconClass: 'bi-shield-lock-fill',
      badgeClass: 'bg-primary',
      description: 'System management, user privileges, and configuration.',
    },
    {
      name: 'Operator',
      roleKey: 'OPERATOR',
      iconClass: 'bi-gear-wide-connected',
      badgeClass: 'bg-success',
      description: 'Usage tracking, bill processing, and operational tasks.',
    },
    {
      name: 'Customer',
      roleKey: 'CUSTOMER',
      iconClass: 'bi-person-badge-fill',
      badgeClass: 'bg-warning text-dark',
      description: 'View usage, manage active plans, and pay bills.',
    },
  ];

  return (
    <div className="container min-vh-100 d-flex flex-column justify-content-center align-items-center py-5">
      <div className="text-center mb-5">
        <h1 className="fw-bold text-uppercase display-5 text-primary">
          Usage Mediation Billing System
        </h1>
        <p className="text-muted fs-5">Select a role portal to continue</p>
      </div>

      <div className="row g-4 w-100 justify-content-center" style={{ maxWidth: '1000px' }}>
        {roles.map((item) => (
          <div className="col-md-4" key={item.roleKey}>
            <div
              className="card h-100 shadow-sm border-0 text-center p-4 card-hover"
              style={{ cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s' }}
              onClick={() => navigate(`/signup?role=${item.roleKey}`)}
            >
              <div className="card-body d-flex flex-column align-items-center justify-content-between">
                <div>
                  <div className={`rounded-circle d-flex align-items-center justify-content-center mb-3 mx-auto ${item.badgeClass}`} style={{ width: '70px', height: '70px' }}>
                    <i className={`bi ${item.iconClass} fs-1`}></i>
                  </div>
                  <h3 className="card-title fw-bold">{item.name}</h3>
                  <p className="card-text text-muted small mt-2">{item.description}</p>
                </div>
                <button className="btn btn-outline-primary btn-sm mt-4 fw-bold w-100">
                  Select {item.name}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};