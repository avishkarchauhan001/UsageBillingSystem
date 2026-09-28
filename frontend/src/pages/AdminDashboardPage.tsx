import React, { useState, useEffect, useCallback } from 'react';
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

interface PlanReportItem {
  planId: number;
  packageName: string;
  dataAllowanceGb: number;
  monthlyChargeUsd: number;
  chargesAfterLimitPerMb: number;
  subscriberCount: number;
  subscriberPercentage: number;
  totalUsageGb: number;
  totalRevenueUsd: number;
}

export const AdminDashboardPage: React.FC<AdminDashboardProps> = ({
  username,
  activeTab,
  onSelectTab,
  onLogout,
  onHomeClick,
}) => {
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [reportData, setReportData] = useState<PlanReportItem[]>([]);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState('');

  const fetchSalesReport = useCallback(async () => {
    setReportLoading(true);
    setReportError('');
    try {
      const res = await fetch('http://localhost:8081/api/admin/report');
      if (res.ok) {
        const data = await res.json();
        setReportData(data);
      } else {
        setReportError('Failed to load sales report');
      }
    } catch (err) {
      setReportError('Failed to connect to server for sales report');
    } finally {
      setReportLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'REPORT') {
      fetchSalesReport();
    }
  }, [activeTab, fetchSalesReport]);

  const pieColors = ['#1976d2', '#00acc1', '#43a047', '#ff9800', '#ab47bc', '#78909c'];

  const totalSubscribers = reportData.reduce((acc, curr) => acc + curr.subscriberCount, 0);
  const totalRevenue = reportData.reduce((acc, curr) => acc + curr.totalRevenueUsd, 0);

  return (
    <div>
      <Header
        username={username}
        onOpenChangePassword={() => setShowChangePassword(true)}
        onLogout={onLogout}
        onHomeClick={onHomeClick}
      />

      {/* Navigation Tab Bar */}
      <div className="bg-light p-2 d-flex gap-1 border-bottom shadow-sm">
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
          onClick={() => {
            onSelectTab('REPORT');
            fetchSalesReport();
          }}
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
          <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h4 className="fw-bold mb-0 text-primary">SALES REPORT</h4>
              <button
                className="btn btn-outline-primary btn-sm fw-semibold"
                onClick={fetchSalesReport}
                disabled={reportLoading}
              >
                {reportLoading ? 'Refreshing...' : '🔄 Refresh Report'}
              </button>
            </div>

            {reportError && <div className="alert alert-danger py-2">{reportError}</div>}

            {reportLoading ? (
              <div className="text-center py-5">
                <div className="spinner-border text-primary"></div>
              </div>
            ) : reportData.length === 0 ? (
              <div className="text-center py-5 text-muted">No sales report data available.</div>
            ) : (
              <div className="row g-4 align-items-center">
                {/* SVG Pie Chart (SRS US07) */}
                <div className="col-md-5 text-center">
                  <div className="card p-4 border-0 shadow-sm bg-white">
                    <h6 className="fw-bold mb-3 text-muted">PLAN SALES DISTRIBUTION</h6>
                    <svg viewBox="0 0 220 220" width="220" height="220" className="mx-auto">
                      {(() => {
                        let currentAngle = 0;
                        return reportData.map((item, idx) => {
                          const pct = item.subscriberPercentage;
                          if (pct <= 0) return null;
                          const sliceAngle = (pct / 100) * 360;
                          const startAngle = currentAngle;
                          const endAngle = currentAngle + sliceAngle;
                          currentAngle = endAngle;

                          const startRad = (startAngle - 90) * (Math.PI / 180);
                          const endRad = (endAngle - 90) * (Math.PI / 180);

                          const x1 = 110 + 95 * Math.cos(startRad);
                          const y1 = 110 + 95 * Math.sin(startRad);
                          const x2 = 110 + 95 * Math.cos(endRad);
                          const y2 = 110 + 95 * Math.sin(endRad);

                          const largeArc = sliceAngle > 180 ? 1 : 0;
                          const pathData = `M 110 110 L ${x1} ${y1} A 95 95 0 ${largeArc} 1 ${x2} ${y2} Z`;

                          return (
                            <path
                              key={item.planId}
                              d={pathData}
                              fill={pieColors[idx % pieColors.length]}
                              stroke="#fff"
                              strokeWidth="2"
                            >
                              <title>{`${item.packageName}: ${pct}%`}</title>
                            </path>
                          );
                        });
                      })()}
                      <circle cx="110" cy="110" r="35" fill="#ffffff" />
                      <text x="110" y="115" textAnchor="middle" fontSize="12" fontWeight="bold" fill="#333">
                        SALES
                      </text>
                    </svg>

                    {/* Legend */}
                    <div className="d-flex flex-wrap justify-content-center gap-2 mt-3">
                      {reportData.map((item, idx) => (
                        <span key={item.planId} className="badge" style={{ backgroundColor: pieColors[idx % pieColors.length] }}>
                          {item.packageName}: {item.subscriberPercentage}%
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Sales & Metric Breakdown Table */}
                <div className="col-md-7">
                  <div className="row g-2 mb-3">
                    <div className="col-6">
                      <div className="card p-2 bg-light border-0 text-center">
                        <small className="text-muted fw-bold">TOTAL SUBSCRIBERS</small>
                        <h4 className="fw-bold text-primary mb-0">{totalSubscribers}</h4>
                      </div>
                    </div>
                    <div className="col-6">
                      <div className="card p-2 bg-light border-0 text-center">
                        <small className="text-muted fw-bold">TOTAL REVENUE</small>
                        <h4 className="fw-bold text-success mb-0">${totalRevenue.toFixed(2)}</h4>
                      </div>
                    </div>
                  </div>

                  <div className="table-responsive bg-white rounded shadow-sm border">
                    <table className="table table-bordered align-middle text-center small mb-0">
                      <thead className="table-light">
                        <tr>
                          <th>PACKAGE</th>
                          <th>ALLOWANCE</th>
                          <th>MONTHLY FEE</th>
                          <th>SUBSCRIBERS</th>
                          <th>USAGE</th>
                          <th>REVENUE</th>
                        </tr>
                      </thead>
                      <tbody>
                        {reportData.map((item) => (
                          <tr key={item.planId}>
                            <td className="fw-bold">{item.packageName}</td>
                            <td>{item.dataAllowanceGb} GB</td>
                            <td>${item.monthlyChargeUsd.toFixed(2)}</td>
                            <td><span className="badge bg-primary">{item.subscriberCount}</span></td>
                            <td>{item.totalUsageGb.toFixed(2)} GB</td>
                            <td className="fw-bold text-success">${item.totalRevenueUsd.toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}
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