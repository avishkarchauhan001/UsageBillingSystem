import React, { useState, useEffect, useCallback } from 'react';
import { Header } from '../components/Header';
import { ChangePasswordModal } from '../components/ChangePasswordModal';

interface Props {
  username: string;
  onLogout: () => void;
}

interface Plan {
  id: number;
  packageName: string;
  dataAllowanceGb: number;
  monthlyChargeUsd: number;
  chargesAfterLimitPerMb: number;
  planState: string;
}

export const OperatorDashboardPage: React.FC<Props> = ({ username, onLogout }) => {
  const [activeTab, setActiveTab] = useState<'PLANS' | 'ADD' | 'EDIT' | 'DELETE' | 'REPORT'>('PLANS');
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(false);

  // Add Plan form state
  const [packageName, setPackageName] = useState('');
  const [dataAllowanceGb, setDataAllowanceGb] = useState('');
  const [monthlyChargeUsd, setMonthlyChargeUsd] = useState('');
  const [chargesAfterLimitPerMb, setChargesAfterLimitPerMb] = useState('');
  const [addMsg, setAddMsg] = useState('');
  const [addError, setAddError] = useState('');

  // Edit Plan state
  const [selectedPlanForEdit, setSelectedPlanForEdit] = useState<Plan | null>(null);
  const [editPackageName, setEditPackageName] = useState('');
  const [editDataGb, setEditDataGb] = useState('');
  const [editMonthlyCharge, setEditMonthlyCharge] = useState('');
  const [editChargesAfterLimit, setEditChargesAfterLimit] = useState('');
  const [editMsg, setEditMsg] = useState('');
  const [editError, setEditError] = useState('');

  // Deactivate state
  const [selectedPlanForDeactivate, setSelectedPlanForDeactivate] = useState<Plan | null>(null);
  const [deactivateMsg, setDeactivateMsg] = useState('');

  const fetchPlans = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch('http://localhost:8081/api/operator/plans');
      if (res.ok) {
        const data = await res.json();
        setPlans(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPlans();
  }, [fetchPlans]);

  const handleAddPlan = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddError('');
    setAddMsg('');

    try {
      const res = await fetch('http://localhost:8081/api/operator/plans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          packageName,
          dataAllowanceGb: parseFloat(dataAllowanceGb),
          monthlyChargeUsd: parseFloat(monthlyChargeUsd),
          chargesAfterLimitPerMb: parseFloat(chargesAfterLimitPerMb)
        })
      });
      const data = await res.json();
      if (!res.ok) {
        setAddError(data.message || 'Failed to add plan');
      } else {
        setAddMsg('Plan configured successfully');
        setPackageName('');
        setDataAllowanceGb('');
        setMonthlyChargeUsd('');
        setChargesAfterLimitPerMb('');
        fetchPlans();
        setTimeout(() => setActiveTab('PLANS'), 1200);
      }
    } catch (err) {
      setAddError('Failed to contact server');
    }
  };

  const handleUpdatePlan = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPlanForEdit) return;
    setEditError('');
    setEditMsg('');

    try {
      const res = await fetch(`http://localhost:8081/api/operator/plans/${selectedPlanForEdit.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          packageName: editPackageName,
          dataAllowanceGb: parseFloat(editDataGb),
          monthlyChargeUsd: parseFloat(editMonthlyCharge),
          chargesAfterLimitPerMb: parseFloat(editChargesAfterLimit),
          planState: selectedPlanForEdit.planState
        })
      });
      const data = await res.json();
      if (!res.ok) {
        setEditError(data.message || 'Failed to update plan');
      } else {
        setEditMsg('Plan updated successfully');
        fetchPlans();
        setTimeout(() => {
          setSelectedPlanForEdit(null);
          setActiveTab('PLANS');
        }, 1200);
      }
    } catch (err) {
      setEditError('Failed to contact server');
    }
  };

  const handleDeactivate = async () => {
    if (!selectedPlanForDeactivate) return;
    try {
      const res = await fetch(`http://localhost:8081/api/operator/plans/${selectedPlanForDeactivate.id}`, {
        method: 'DELETE'
      });
      const data = await res.json();
      setDeactivateMsg(data.message || 'Plan deactivated successfully');
      setSelectedPlanForDeactivate(null);
      fetchPlans();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div>
      <Header
        username={username}
        onOpenChangePassword={() => setShowChangePassword(true)}
        onLogout={onLogout}
        onHomeClick={() => setActiveTab('PLANS')}
      />

      {/* Operator Navigation Tab Bar matching Infosys SRS */}
      <div className="bg-light p-2 d-flex gap-1 border-bottom">
        <button
          className={`nav-tab-btn ${activeTab === 'PLANS' ? 'active' : ''}`}
          onClick={() => setActiveTab('PLANS')}
        >
          PLANS
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'ADD' ? 'active' : ''}`}
          onClick={() => setActiveTab('ADD')}
        >
          ADD PLAN
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'EDIT' ? 'active' : ''}`}
          onClick={() => setActiveTab('EDIT')}
        >
          EDIT PLAN
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'DELETE' ? 'active' : ''}`}
          onClick={() => setActiveTab('DELETE')}
        >
          DELETE PLAN
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'REPORT' ? 'active' : ''}`}
          onClick={() => setActiveTab('REPORT')}
        >
          REPORT
        </button>
      </div>

      <div className="container mt-4">
        {deactivateMsg && <div className="alert alert-success py-2 text-center">{deactivateMsg}</div>}

        {/* TAB 1: PLANS (SRS US11) */}
        {activeTab === 'PLANS' && (
          <div>
            <h4 className="fw-bold mb-3 text-primary">AVAILABLE BROADBAND PLANS</h4>
            {loading ? (
              <div className="text-center py-4"><div className="spinner-border text-primary"></div></div>
            ) : (
              <div className="table-responsive bg-white rounded shadow-sm border">
                <table className="table table-hover align-middle mb-0 text-center">
                  <thead className="table-primary">
                    <tr>
                      <th>PACKAGE</th>
                      <th>DATA IN GB</th>
                      <th>MONTHLY CHARGE IN USD</th>
                      <th>CHARGES AFTER LIMIT</th>
                      <th>STATUS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {plans.map((p) => (
                      <tr key={p.id}>
                        <td className="fw-bold">{p.packageName}</td>
                        <td>{p.dataAllowanceGb} GB</td>
                        <td>${p.monthlyChargeUsd.toFixed(2)}</td>
                        <td>${p.chargesAfterLimitPerMb.toFixed(4)} / MB</td>
                        <td>
                          <span className={`badge ${p.planState === 'Activated' ? 'bg-success' : 'bg-secondary'}`}>
                            {p.planState}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: ADD PLAN (SRS US12) */}
        {activeTab === 'ADD' && (
          <div className="d-flex justify-content-center">
            <div className="cyan-box p-4 rounded shadow" style={{ width: '100%', maxWidth: '560px' }}>
              <h4 className="text-center fw-bold mb-3 text-primary">NEW PLAN</h4>

              {addMsg && <div className="alert alert-success py-1 text-center small">{addMsg}</div>}
              {addError && <div className="alert alert-danger py-1 text-center small">{addError}</div>}

              <form onSubmit={handleAddPlan}>
                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">PACKAGE:</label>
                  <div className="col-8">
                    <input
                      type="text"
                      className="form-control form-control-sm"
                      value={packageName}
                      onChange={(e) => setPackageName(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">DATA IN GB:</label>
                  <div className="col-8">
                    <input
                      type="number"
                      step="0.1"
                      className="form-control form-control-sm"
                      value={dataAllowanceGb}
                      onChange={(e) => setDataAllowanceGb(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">MONTHLY CHARGES (USD):</label>
                  <div className="col-8">
                    <input
                      type="number"
                      step="0.01"
                      className="form-control form-control-sm"
                      value={monthlyChargeUsd}
                      onChange={(e) => setMonthlyChargeUsd(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">CHARGES AFTER LIMIT ($/MB):</label>
                  <div className="col-8">
                    <input
                      type="number"
                      step="0.0001"
                      className="form-control form-control-sm"
                      value={chargesAfterLimitPerMb}
                      onChange={(e) => setChargesAfterLimitPerMb(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="d-flex justify-content-center gap-3 mt-4">
                  <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold">
                    ADD
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm px-4" onClick={() => setActiveTab('PLANS')}>
                    CANCEL
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* TAB 3: EDIT PLAN (SRS US13) */}
        {activeTab === 'EDIT' && (
          <div>
            <h4 className="fw-bold mb-3 text-primary">EDIT PLAN</h4>
            <div className="table-responsive bg-white rounded shadow-sm border mb-4">
              <table className="table table-hover align-middle mb-0 text-center">
                <thead className="table-primary">
                  <tr>
                    <th>PACKAGE</th>
                    <th>DATA IN GB</th>
                    <th>MONTHLY CHARGE</th>
                    <th>CHARGES AFTER LIMIT</th>
                    <th>ACTION</th>
                  </tr>
                </thead>
                <tbody>
                  {plans.map((p) => (
                    <tr key={p.id}>
                      <td className="fw-bold">{p.packageName}</td>
                      <td>{p.dataAllowanceGb} GB</td>
                      <td>${p.monthlyChargeUsd.toFixed(2)}</td>
                      <td>${p.chargesAfterLimitPerMb.toFixed(4)} / MB</td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary px-3"
                          onClick={() => {
                            setSelectedPlanForEdit(p);
                            setEditPackageName(p.packageName);
                            setEditDataGb(p.dataAllowanceGb.toString());
                            setEditMonthlyCharge(p.monthlyChargeUsd.toString());
                            setEditChargesAfterLimit(p.chargesAfterLimitPerMb.toString());
                            setEditError('');
                            setEditMsg('');
                          }}
                        >
                          ✏️ Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {selectedPlanForEdit && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content cyan-box p-3">
                    <div className="modal-header border-0 pb-0">
                      <h5 className="modal-title fw-bold text-primary">EDIT PLAN: {selectedPlanForEdit.packageName}</h5>
                      <button type="button" className="btn-close" onClick={() => setSelectedPlanForEdit(null)}></button>
                    </div>
                    <div className="modal-body">
                      {editMsg && <div className="alert alert-success py-1 small">{editMsg}</div>}
                      {editError && <div className="alert alert-danger py-1 small">{editError}</div>}

                      <form onSubmit={handleUpdatePlan}>
                        <div className="mb-2">
                          <label className="form-label fw-bold small">PACKAGE NAME:</label>
                          <input
                            type="text"
                            className="form-control form-control-sm"
                            value={editPackageName}
                            onChange={(e) => setEditPackageName(e.target.value)}
                            required
                          />
                        </div>
                        <div className="mb-2">
                          <label className="form-label fw-bold small">DATA IN GB:</label>
                          <input
                            type="number"
                            step="0.1"
                            className="form-control form-control-sm"
                            value={editDataGb}
                            onChange={(e) => setEditDataGb(e.target.value)}
                            required
                          />
                        </div>
                        <div className="mb-2">
                          <label className="form-label fw-bold small">MONTHLY CHARGES (USD):</label>
                          <input
                            type="number"
                            step="0.01"
                            className="form-control form-control-sm"
                            value={editMonthlyCharge}
                            onChange={(e) => setEditMonthlyCharge(e.target.value)}
                            required
                          />
                        </div>
                        <div className="mb-3">
                          <label className="form-label fw-bold small">CHARGES AFTER LIMIT ($/MB):</label>
                          <input
                            type="number"
                            step="0.0001"
                            className="form-control form-control-sm"
                            value={editChargesAfterLimit}
                            onChange={(e) => setEditChargesAfterLimit(e.target.value)}
                            required
                          />
                        </div>
                        <div className="d-flex justify-content-center gap-2">
                          <button type="submit" className="btn btn-primary btn-sm px-4">
                            CHANGE
                          </button>
                          <button type="button" className="btn btn-secondary btn-sm px-4" onClick={() => setSelectedPlanForEdit(null)}>
                            CANCEL
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 4: DELETE / DEACTIVATE PLAN (SRS US14) */}
        {activeTab === 'DELETE' && (
          <div>
            <h4 className="fw-bold mb-3 text-primary">DEACTIVATE PLANS</h4>
            <div className="table-responsive bg-white rounded shadow-sm border mb-4">
              <table className="table table-hover align-middle mb-0 text-center">
                <thead className="table-primary">
                  <tr>
                    <th>PACKAGE</th>
                    <th>DATA IN GB</th>
                    <th>MONTHLY CHARGE</th>
                    <th>STATUS</th>
                    <th>ACTION</th>
                  </tr>
                </thead>
                <tbody>
                  {plans.map((p) => (
                    <tr key={p.id}>
                      <td className="fw-bold">{p.packageName}</td>
                      <td>{p.dataAllowanceGb} GB</td>
                      <td>${p.monthlyChargeUsd.toFixed(2)}</td>
                      <td>
                        <span className={`badge ${p.planState === 'Activated' ? 'bg-success' : 'bg-secondary'}`}>
                          {p.planState}
                        </span>
                      </td>
                      <td>
                        {p.planState === 'Activated' ? (
                          <button
                            className="btn btn-sm btn-danger px-3"
                            onClick={() => setSelectedPlanForDeactivate(p)}
                          >
                            DEACTIVATE
                          </button>
                        ) : (
                          <span className="small text-muted">Already Deactivated</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {selectedPlanForDeactivate && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content text-center p-4">
                    <h5 className="fw-bold mb-3">DEACTIVATE PLAN</h5>
                    <p>Are you sure you want to deactivate plan <strong>{selectedPlanForDeactivate.packageName}</strong>?</p>
                    <p className="small text-muted mb-0">Deactivated plans will no longer be available for customers to choose for new plan changes.</p>
                    <div className="d-flex justify-content-center gap-3 mt-4">
                      <button className="btn btn-danger px-4 fw-bold" onClick={handleDeactivate}>
                        CONFIRM
                      </button>
                      <button className="btn btn-secondary px-4" onClick={() => setSelectedPlanForDeactivate(null)}>
                        CANCEL
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 5: REPORT */}
        {activeTab === 'REPORT' && (
          <div className="text-center p-5 bg-white rounded shadow-sm border">
            <h4 className="fw-bold mb-4 text-primary">PLAN USAGE & DISTRIBUTION REPORT</h4>
            <div className="d-flex justify-content-center">
              <div
                className="rounded-circle border border-primary d-flex flex-column align-items-center justify-content-center"
                style={{ width: '260px', height: '260px', backgroundColor: '#e0f7fa' }}
              >
                <span className="fw-bold text-primary fs-5">[ Plans Distribution ]</span>
                <small className="text-muted mt-2">{plans.filter(p => p.planState === 'Activated').length} Active Plans</small>
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