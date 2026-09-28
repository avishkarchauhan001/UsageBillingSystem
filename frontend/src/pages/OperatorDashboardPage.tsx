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

interface PlanReportItem {
  planId: number;
  packageName: string;
  dataAllowanceGb: number;
  monthlyChargeUsd: number;
  chargesAfterLimitPerMb: number;
  planState: string;
  subscriberCount: number;
  totalUsageGb: number;
  totalRevenueUsd: number;
  subscriberPercentage: number;
}

export const OperatorDashboardPage: React.FC<Props> = ({ username, onLogout }) => {
  const [activeTab, setActiveTab] = useState<'PLANS' | 'ADD' | 'EDIT' | 'DELETE' | 'REPORT'>('PLANS');
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(false);

  // Pagination state (SRS US11: 10 plans per screen)
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  // Add Plan form state (SRS US12)
  const [packageName, setPackageName] = useState('');
  const [dataAllowanceGb, setDataAllowanceGb] = useState('');
  const [monthlyChargeUsd, setMonthlyChargeUsd] = useState('');
  const [chargesAfterLimitPerMb, setChargesAfterLimitPerMb] = useState('');
  const [addMsg, setAddMsg] = useState('');
  const [addError, setAddError] = useState('');

  // Edit Plan state (SRS US13)
  const [selectedPlanForEdit, setSelectedPlanForEdit] = useState<Plan | null>(null);
  const [editPackageName, setEditPackageName] = useState('');
  const [editDataGb, setEditDataGb] = useState('');
  const [editMonthlyCharge, setEditMonthlyCharge] = useState('');
  const [editChargesAfterLimit, setEditChargesAfterLimit] = useState('');
  const [showEditConfirmation, setShowEditConfirmation] = useState(false);
  const [editMsg, setEditMsg] = useState('');
  const [editError, setEditError] = useState('');

  // Deactivate state (SRS US14)
  const [selectedPlanForDeactivate, setSelectedPlanForDeactivate] = useState<Plan | null>(null);
  const [deactivateMsg, setDeactivateMsg] = useState('');

  // Report state (SRS US07)
  const [reportData, setReportData] = useState<PlanReportItem[]>([]);
  const [reportLoading, setReportLoading] = useState(false);

  const fetchPlans = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch('http://localhost:8081/api/operator/plans?activeOnly=true', {
        headers: { 'X-User-Role': 'OPERATOR' }
      });
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

  const fetchReport = useCallback(async () => {
    setReportLoading(true);
    try {
      const res = await fetch('http://localhost:8081/api/operator/report', {
        headers: { 'X-User-Role': 'OPERATOR' }
      });
      if (res.ok) {
        const data = await res.json();
        setReportData(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setReportLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPlans();
  }, [fetchPlans]);

  useEffect(() => {
    if (activeTab === 'REPORT') {
      fetchReport();
    }
  }, [activeTab, fetchReport]);

  // Alphanumeric validation rule for package name
  const validatePackageName = (name: string): boolean => {
    return /^[a-zA-Z0-9 ]+$/.test(name.trim());
  };

  const handleAddPlan = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddError('');
    setAddMsg('');

    if (!validatePackageName(packageName)) {
      setAddError('Package name must contain only alphanumeric characters.');
      return;
    }

    const dataGb = parseFloat(dataAllowanceGb);
    const monthly = parseFloat(monthlyChargeUsd);
    const afterLimit = parseFloat(chargesAfterLimitPerMb);

    if (isNaN(dataGb) || dataGb <= 0) {
      setAddError('Data in GB must be a positive number.');
      return;
    }
    if (isNaN(monthly) || monthly < 0) {
      setAddError('Monthly charge must be a non-negative number.');
      return;
    }
    if (isNaN(afterLimit) || afterLimit < 0) {
      setAddError('Charges after limit must be a non-negative number.');
      return;
    }

    try {
      const res = await fetch('http://localhost:8081/api/operator/plans', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Role': 'OPERATOR'
        },
        body: JSON.stringify({
          packageName: packageName.trim(),
          dataAllowanceGb: dataGb,
          monthlyChargeUsd: monthly,
          chargesAfterLimitPerMb: afterLimit
        })
      });
      const data = await res.json();
      if (!res.ok) {
        setAddError(data.message || 'Failed to add plan');
      } else {
        setAddMsg('plan configured successfully');
        setPackageName('');
        setDataAllowanceGb('');
        setMonthlyChargeUsd('');
        setChargesAfterLimitPerMb('');
        await fetchPlans();
        setTimeout(() => {
          setAddMsg('');
          setActiveTab('PLANS');
        }, 1200);
      }
    } catch (err) {
      setAddError('Unable to contact backend server');
    }
  };

  const executeUpdatePlan = async () => {
    if (!selectedPlanForEdit) return;
    setEditError('');
    setEditMsg('');

    if (!validatePackageName(editPackageName)) {
      setEditError('Package name must contain only alphanumeric characters.');
      return;
    }

    const dataGb = parseFloat(editDataGb);
    const monthly = parseFloat(editMonthlyCharge);
    const afterLimit = parseFloat(editChargesAfterLimit);

    if (isNaN(dataGb) || dataGb <= 0) {
      setEditError('Data in GB must be a positive number.');
      return;
    }
    if (isNaN(monthly) || monthly < 0) {
      setEditError('Monthly charge must be a non-negative number.');
      return;
    }
    if (isNaN(afterLimit) || afterLimit < 0) {
      setEditError('Charges after limit must be a non-negative number.');
      return;
    }

    try {
      const res = await fetch(`http://localhost:8081/api/operator/plans/${selectedPlanForEdit.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Role': 'OPERATOR'
        },
        body: JSON.stringify({
          packageName: editPackageName.trim(),
          dataAllowanceGb: dataGb,
          monthlyChargeUsd: monthly,
          chargesAfterLimitPerMb: afterLimit,
          planState: selectedPlanForEdit.planState
        })
      });
      const data = await res.json();
      if (!res.ok) {
        setEditError(data.message || 'Failed to update plan');
      } else {
        setShowEditConfirmation(false);
        setEditMsg('plan updated successfully');
        await fetchPlans();
        setTimeout(() => {
          setSelectedPlanForEdit(null);
          setEditMsg('');
          setActiveTab('PLANS');
        }, 1200);
      }
    } catch (err) {
      setEditError('Unable to contact backend server');
    }
  };

  const handleDeactivate = async () => {
    if (!selectedPlanForDeactivate) return;
    try {
      const res = await fetch(`http://localhost:8081/api/operator/plans/${selectedPlanForDeactivate.id}`, {
        method: 'DELETE',
        headers: { 'X-User-Role': 'OPERATOR' }
      });
      const data = await res.json();
      setDeactivateMsg(data.message || 'plan deactivated successfully');
      setSelectedPlanForDeactivate(null);
      await fetchPlans();
      setTimeout(() => {
        setDeactivateMsg('');
        setActiveTab('PLANS');
      }, 1500);
    } catch (err) {
      console.error(err);
    }
  };

  // Pagination calculation
  const totalPages = Math.ceil(plans.length / pageSize) || 1;
  const paginatedPlans = plans.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  // Colors for SVG Pie Chart
  const pieColors = ['#1976d2', '#00acc1', '#26a69a', '#ff9800', '#ab47bc', '#78909c'];

  return (
    <div>
      <Header
        username={username}
        onOpenChangePassword={() => setShowChangePassword(true)}
        onLogout={onLogout}
        onHomeClick={() => setActiveTab('PLANS')}
      />

      {/* Operator Navigation Tab Bar matching Infosys SRS */}
      <div className="bg-light p-2 d-flex gap-1 border-bottom shadow-sm">
        <button
          className={`nav-tab-btn ${activeTab === 'PLANS' ? 'active' : ''}`}
          onClick={() => { setActiveTab('PLANS'); setCurrentPage(1); }}
        >
          PLANS
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'ADD' ? 'active' : ''}`}
          onClick={() => { setActiveTab('ADD'); setAddMsg(''); setAddError(''); }}
        >
          ADD PLAN
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'EDIT' ? 'active' : ''}`}
          onClick={() => { setActiveTab('EDIT'); setEditMsg(''); setEditError(''); }}
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
        {deactivateMsg && <div className="alert alert-success py-2 text-center fw-bold">{deactivateMsg}</div>}

        {/* TAB 1: PLANS (SRS US11) */}
        {activeTab === 'PLANS' && (
          <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h4 className="fw-bold text-primary mb-0">ACTIVE BROADBAND PLANS</h4>
              <span className="badge bg-primary fs-6">{plans.length} Available Plans</span>
            </div>

            {loading ? (
              <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>
            ) : (
              <div className="table-responsive bg-white rounded shadow-sm border">
                <table className="table table-hover align-middle mb-0 text-center">
                  <thead className="table-primary">
                    <tr>
                      <th style={{ width: '25%' }}>PACKAGE</th>
                      <th style={{ width: '25%' }}>DATA IN GB</th>
                      <th style={{ width: '25%' }}>MONTHLY CHARGE IN USD</th>
                      <th style={{ width: '25%' }}>CHARGES AFTER LIMIT</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedPlans.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="py-4 text-muted">No active plans found.</td>
                      </tr>
                    ) : (
                      paginatedPlans.map((p) => (
                        <tr key={p.id}>
                          <td className="fw-bold text-dark">{p.packageName}</td>
                          <td>{p.dataAllowanceGb} GB</td>
                          <td>${p.monthlyChargeUsd.toFixed(2)}</td>
                          <td>${p.chargesAfterLimitPerMb.toFixed(4)} / MB</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>

                {/* Pagination Controls (SRS US11: Only 10 plans per screen, maintained in pages) */}
                {totalPages > 1 && (
                  <div className="d-flex justify-content-center align-items-center gap-1 p-3 border-top bg-light">
                    <button
                      className="btn btn-sm btn-outline-secondary"
                      disabled={currentPage === 1}
                      onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                    >
                      &laquo; Prev
                    </button>
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((num) => (
                      <button
                        key={num}
                        className={`btn btn-sm ${currentPage === num ? 'btn-primary fw-bold' : 'btn-outline-primary'}`}
                        style={{ minWidth: '36px' }}
                        onClick={() => setCurrentPage(num)}
                      >
                        {num}
                      </button>
                    ))}
                    <button
                      className="btn btn-sm btn-outline-secondary"
                      disabled={currentPage === totalPages}
                      onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                    >
                      Next &raquo;
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: ADD PLAN (SRS US12) */}
        {activeTab === 'ADD' && (
          <div className="d-flex justify-content-center">
            <div className="cyan-box p-4 rounded shadow" style={{ width: '100%', maxWidth: '520px' }}>
              <h4 className="text-center fw-bold mb-3 text-primary">NEW PLAN</h4>

              {addMsg && <div className="alert alert-success py-2 text-center small fw-semibold">{addMsg}</div>}
              {addError && <div className="alert alert-danger py-2 text-center small fw-semibold">{addError}</div>}

              <form onSubmit={handleAddPlan}>
                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">PACKAGE:</label>
                  <div className="col-8">
                    <input
                      type="text"
                      className="form-control form-control-sm"
                      value={packageName}
                      onChange={(e) => setPackageName(e.target.value)}
                      placeholder="e.g. UltraSpeed"
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
                      min="0.1"
                      className="form-control form-control-sm"
                      value={dataAllowanceGb}
                      onChange={(e) => setDataAllowanceGb(e.target.value)}
                      placeholder="e.g. 10"
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
                      min="0"
                      className="form-control form-control-sm"
                      value={monthlyChargeUsd}
                      onChange={(e) => setMonthlyChargeUsd(e.target.value)}
                      placeholder="e.g. 25.00"
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
                      min="0"
                      className="form-control form-control-sm"
                      value={chargesAfterLimitPerMb}
                      onChange={(e) => setChargesAfterLimitPerMb(e.target.value)}
                      placeholder="e.g. 0.0050"
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
                          className="btn btn-sm btn-outline-primary px-3 fw-bold"
                          onClick={() => {
                            setSelectedPlanForEdit(p);
                            setEditPackageName(p.packageName);
                            setEditDataGb(p.dataAllowanceGb.toString());
                            setEditMonthlyCharge(p.monthlyChargeUsd.toString());
                            setEditChargesAfterLimit(p.chargesAfterLimitPerMb.toString());
                            setEditError('');
                            setEditMsg('');
                            setShowEditConfirmation(false);
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

            {/* Edit Modal (SRS US13) */}
            {selectedPlanForEdit && !showEditConfirmation && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content cyan-box p-3 shadow-lg">
                    <div className="modal-header border-0 pb-0">
                      <h5 className="modal-title fw-bold text-primary">EDIT PLAN: {selectedPlanForEdit.packageName}</h5>
                      <button type="button" className="btn-close" onClick={() => setSelectedPlanForEdit(null)}></button>
                    </div>
                    <div className="modal-body">
                      {editMsg && <div className="alert alert-success py-1 small fw-bold">{editMsg}</div>}
                      {editError && <div className="alert alert-danger py-1 small">{editError}</div>}

                      <form onSubmit={(e) => { e.preventDefault(); setShowEditConfirmation(true); }}>
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
                            min="0.1"
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
                            min="0"
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
                            min="0"
                            className="form-control form-control-sm"
                            value={editChargesAfterLimit}
                            onChange={(e) => setEditChargesAfterLimit(e.target.value)}
                            required
                          />
                        </div>
                        <div className="d-flex justify-content-center gap-2">
                          <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold">
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

            {/* Edit Confirmation Box (SRS US13: Confirmation box to update plan) */}
            {selectedPlanForEdit && showEditConfirmation && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content text-center p-4 shadow-lg">
                    <h5 className="fw-bold mb-3 text-primary">CONFIRM PLAN UPDATE</h5>
                    <p>Are you sure you want to update plan <strong>{selectedPlanForEdit.packageName}</strong> with the new values?</p>
                    <div className="text-start bg-light p-3 rounded mb-3 small">
                      <div><strong>New Package Name:</strong> {editPackageName}</div>
                      <div><strong>Data Allowance:</strong> {editDataGb} GB</div>
                      <div><strong>Monthly Charge:</strong> ${parseFloat(editMonthlyCharge || '0').toFixed(2)}</div>
                      <div><strong>Charges After Limit:</strong> ${parseFloat(editChargesAfterLimit || '0').toFixed(4)} / MB</div>
                    </div>
                    {editMsg && <div className="alert alert-success py-1 small fw-bold">{editMsg}</div>}
                    {editError && <div className="alert alert-danger py-1 small">{editError}</div>}
                    <div className="d-flex justify-content-center gap-3">
                      <button className="btn btn-primary px-4 fw-bold" onClick={executeUpdatePlan}>
                        CONFIRM
                      </button>
                      <button className="btn btn-secondary px-4" onClick={() => setShowEditConfirmation(false)}>
                        CANCEL
                      </button>
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
                            className="btn btn-sm btn-danger px-3 fw-bold"
                            onClick={() => setSelectedPlanForDeactivate(p)}
                          >
                            ❌ DEACTIVATE
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

            {/* Deactivation Confirmation Modal (SRS US14) */}
            {selectedPlanForDeactivate && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content text-center p-4 shadow-lg">
                    <h5 className="fw-bold mb-3 text-danger">ARE YOU SURE YOU WANT TO DELETE {selectedPlanForDeactivate.packageName} ?</h5>
                    <p className="text-muted small">
                      This will change the plan state to <strong>Deactivated</strong>. The plan will be safely removed from the active plans list and will no longer be offered to customers for new plan selections. Historical bills remain completely intact.
                    </p>
                    <div className="d-flex justify-content-center gap-3 mt-4">
                      <button className="btn btn-danger px-4 fw-bold" onClick={handleDeactivate}>
                        DELETE
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

        {/* TAB 5: REPORT (SRS US07 Plan Report) */}
        {activeTab === 'REPORT' && (
          <div className="bg-white p-4 rounded shadow-sm border">
            <h4 className="fw-bold mb-3 text-primary text-center">PLAN REPORT & SALES ANALYSIS</h4>
            <p className="text-muted text-center small mb-4">
              Real-time pictorial representation of sales and subscriber distribution across active broadband packages.
            </p>

            {reportLoading ? (
              <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>
            ) : (
              <div className="row align-items-center">
                {/* Pictorial Pie Chart (SVG) */}
                <div className="col-md-5 d-flex flex-column align-items-center justify-content-center mb-4 mb-md-0">
                  <h6 className="fw-bold text-secondary mb-3">SALES DISTRIBUTION</h6>
                  <svg width="220" height="220" viewBox="0 0 220 220">
                    {(() => {
                      let cumulative = 0;
                      return reportData.map((item, idx) => {
                        const pct = item.subscriberPercentage || 0;
                        const startAngle = (cumulative * 360) / 100;
                        cumulative += pct;
                        const endAngle = (cumulative * 360) / 100;

                        if (pct <= 0) return null;

                        const x1 = 110 + 90 * Math.cos((Math.PI * (startAngle - 90)) / 180);
                        const y1 = 110 + 90 * Math.sin((Math.PI * (startAngle - 90)) / 180);
                        const x2 = 110 + 90 * Math.cos((Math.PI * (endAngle - 90)) / 180);
                        const y2 = 110 + 90 * Math.sin((Math.PI * (endAngle - 90)) / 180);
                        const largeArc = pct > 50 ? 1 : 0;
                        const pathData = `M 110 110 L ${x1} ${y1} A 90 90 0 ${largeArc} 1 ${x2} ${y2} Z`;

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
                      PLANS
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

                {/* Sales & Metric Breakdown Table */}
                <div className="col-md-7">
                  <div className="table-responsive">
                    <table className="table table-bordered align-middle text-center small mb-0">
                      <thead className="table-light">
                        <tr>
                          <th>PACKAGE</th>
                          <th>ALLOWANCE</th>
                          <th>MONTHLY FEE</th>
                          <th>SUBSCRIBERS</th>
                          <th>TOTAL USAGE</th>
                          <th>TOTAL REVENUE</th>
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