import React, { useState, useEffect, useCallback } from 'react';
import { Header } from '../components/Header';
import { ChangePasswordModal } from '../components/ChangePasswordModal';

interface Props {
  username: string;
  onLogout: () => void;
}

interface CustomerHomeData {
  username: string;
  planId: number;
  packageName: string;
  billingStartDate: string;
  billingEndDate: string;
  usageInGb: number;
  remainingDataMb: number;
  dataAllowanceGb: number;
  dataAfterLimitGb: number;
  monthlyChargeUsd: number;
  excessChargeUsd: number;
  totalAmountUsd: number;
  pendingBillsCount: number;
  currentBillId: number | null;
  currentBillNumber: string | null;
  hasScheduledPlan: boolean;
  scheduledPackageName: string | null;
  scheduledActivationDate: string | null;
}

interface Bill {
  id: number;
  billNumber: string;
  planName: string;
  billingStartDate: string;
  billingEndDate: string;
  usageInGb: number;
  dataAllowanceGb: number;
  remainingDataMb: number;
  dataAfterLimitGb: number;
  baseChargeUsd: number;
  excessChargeUsd: number;
  totalAmountUsd: number;
  status: 'PENDING' | 'PAID';
  paymentMode: string | null;
  remark: string | null;
  generatedDate: string;
  dueDate: string;
  paidDate: string | null;
}

interface PlanOption {
  id: number;
  packageName: string;
  dataAllowanceGb: number;
  monthlyChargeUsd: number;
  chargesAfterLimitPerMb: number;
  planState: string;
}

interface DailyUsage {
  date: string;
  uploadMb: number;
  downloadMb: number;
  totalMb: number;
  totalGb: number;
}

interface ReportData {
  username: string;
  fromDate: string;
  toDate: string;
  totalUsageGb: number;
  totalUploadGb: number;
  totalDownloadGb: number;
  dailyUsage: DailyUsage[];
}

export const CustomerDashboardPage: React.FC<Props> = ({ username, onLogout }) => {
  const [activeTab, setActiveTab] = useState<'HOME' | 'BILLS' | 'PENDING' | 'CHANGE_PLAN' | 'REPORT'>('HOME');
  const [showChangePassword, setShowChangePassword] = useState(false);

  // Home state
  const [homeData, setHomeData] = useState<CustomerHomeData | null>(null);
  const [homeLoading, setHomeLoading] = useState(false);

  // Bills & Search state
  const [bills, setBills] = useState<Bill[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedBillForDetail, setSelectedBillForDetail] = useState<Bill | null>(null);

  // Pending Bills state
  const [pendingBills, setPendingBills] = useState<Bill[]>([]);

  // Payment state
  const [billToPay, setBillToPay] = useState<Bill | null>(null);
  const [paymentMode, setPaymentMode] = useState('PayTM');
  const [paymentMsg, setPaymentMsg] = useState('');
  const [paymentError, setPaymentError] = useState('');
  const [showPayConfirm, setShowPayConfirm] = useState(false);
  const [paying, setPaying] = useState(false);

  // Change Plan state
  const [availablePlans, setAvailablePlans] = useState<PlanOption[]>([]);
  const [selectedNewPlanId, setSelectedNewPlanId] = useState<number | ''>('');
  const [planChangeMsg, setPlanChangeMsg] = useState('');
  const [planChangeError, setPlanChangeError] = useState('');
  const [showPlanConfirm, setShowPlanConfirm] = useState(false);

  // Report state
  const [fromDate, setFromDate] = useState(() => {
    const d = new Date();
    d.setDate(1);
    return d.toISOString().split('T')[0];
  });
  const [toDate, setToDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [reportData, setReportData] = useState<ReportData | null>(null);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState('');

  // Simulator notification
  const [simMsg, setSimMsg] = useState('');

  const fetchHomeData = useCallback(async () => {
    setHomeLoading(true);
    try {
      const res = await fetch(`http://localhost:8081/api/customer/home?username=${encodeURIComponent(username)}`);
      if (res.ok) {
        const data = await res.json();
        setHomeData(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setHomeLoading(false);
    }
  }, [username]);

  const fetchBills = useCallback(async (query: string = '') => {
    try {
      const url = query.trim()
        ? `http://localhost:8081/api/customer/bills/search?username=${encodeURIComponent(username)}&query=${encodeURIComponent(query.trim())}`
        : `http://localhost:8081/api/customer/bills?username=${encodeURIComponent(username)}`;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        setBills(data);
      }
    } catch (err) {
      console.error(err);
    }
  }, [username]);

  const fetchPendingBills = useCallback(async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/customer/bills/pending?username=${encodeURIComponent(username)}`);
      if (res.ok) {
        const data = await res.json();
        setPendingBills(data);
      }
    } catch (err) {
      console.error(err);
    }
  }, [username]);

  const fetchPlans = useCallback(async () => {
    try {
      const res = await fetch('http://localhost:8081/api/customer/plans');
      if (res.ok) {
        const data = await res.json();
        setAvailablePlans(data);
        if (data.length > 0 && selectedNewPlanId === '') {
          setSelectedNewPlanId(data[0].id);
        }
      }
    } catch (err) {
      console.error(err);
    }
  }, [selectedNewPlanId]);

  const fetchReport = useCallback(async () => {
    setReportLoading(true);
    setReportError('');
    try {
      const res = await fetch(`http://localhost:8081/api/customer/report?username=${encodeURIComponent(username)}&fromDate=${fromDate}&toDate=${toDate}`);
      const data = await res.json();
      if (!res.ok) {
        setReportError(data.message || 'Failed to fetch report');
      } else {
        setReportData(data);
      }
    } catch (err) {
      setReportError('Failed to fetch report from server');
    } finally {
      setReportLoading(false);
    }
  }, [username, fromDate, toDate]);

  useEffect(() => {
    fetchHomeData();
    fetchBills();
    fetchPendingBills();
    fetchPlans();
  }, [fetchHomeData, fetchBills, fetchPendingBills, fetchPlans]);

  // Execute payment
  const handleExecutePayment = async () => {
    if (!billToPay) return;
    setPaying(true);
    setPaymentError('');
    setPaymentMsg('');

    try {
      const res = await fetch(`http://localhost:8081/api/customer/bills/${billToPay.id}/pay?username=${encodeURIComponent(username)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paymentMode })
      });
      const data = await res.json();
      if (!res.ok) {
        setPaymentError(data.message || 'Payment failed');
        setShowPayConfirm(false);
      } else {
        setPaymentMsg(data.message || 'Payment completed successfully!');
        setShowPayConfirm(false);
        setBillToPay(null);
        // Refresh state
        fetchHomeData();
        fetchPendingBills();
        fetchBills();
      }
    } catch (err) {
      setPaymentError('Failed to connect to server');
      setShowPayConfirm(false);
    } finally {
      setPaying(false);
    }
  };

  // Execute plan change (SRS US19)
  const handleExecutePlanChange = async () => {
    if (!selectedNewPlanId) return;
    setPlanChangeError('');
    setPlanChangeMsg('');

    try {
      const res = await fetch(`http://localhost:8081/api/customer/change-plan?username=${encodeURIComponent(username)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newPlanId: Number(selectedNewPlanId) })
      });
      const data = await res.json();
      if (!res.ok) {
        setPlanChangeError(data.message || 'Failed to schedule plan change');
      } else {
        setPlanChangeMsg(data.message);
        setShowPlanConfirm(false);
        fetchHomeData();
      }
    } catch (err) {
      setPlanChangeError('Failed to connect to server');
    } finally {
      setShowPlanConfirm(false);
    }
  };

  // Simulate IPDR usage traffic for demonstration
  const handleSimulateUsage = async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/ipdr/simulate/${encodeURIComponent(username)}?count=2`, {
        method: 'POST'
      });
      const data = await res.json();
      setSimMsg(data.message || 'Simulated usage successfully!');
      fetchHomeData();
      fetchPendingBills();
      fetchBills();
      setTimeout(() => setSimMsg(''), 4000);
    } catch (err) {
      setSimMsg('Failed to trigger simulation');
    }
  };

  return (
    <div>
      <Header
        username={username}
        onOpenChangePassword={() => setShowChangePassword(true)}
        onLogout={onLogout}
        onHomeClick={() => setActiveTab('HOME')}
      />

      {/* Navigation Tab Bar matching Infosys SRS Dashboard design */}
      <div className="bg-light p-2 d-flex gap-1 border-bottom">
        <button
          className={`nav-tab-btn ${activeTab === 'HOME' ? 'active' : ''}`}
          onClick={() => setActiveTab('HOME')}
        >
          🏠 HOME
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'BILLS' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('BILLS');
            fetchBills();
          }}
        >
          BILL DETAILS
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'PENDING' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('PENDING');
            fetchPendingBills();
          }}
        >
          PENDING BILLS {homeData && homeData.pendingBillsCount > 0 ? `(${homeData.pendingBillsCount})` : ''}
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'CHANGE_PLAN' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('CHANGE_PLAN');
            fetchPlans();
          }}
        >
          CHANGE PLAN
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'REPORT' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('REPORT');
            fetchReport();
          }}
        >
          REPORT
        </button>
      </div>

      <div className="container mt-4">
        {simMsg && <div className="alert alert-info py-2 text-center small fw-bold">{simMsg}</div>}
        {paymentMsg && <div className="alert alert-success py-2 text-center fw-bold">{paymentMsg}</div>}
        {paymentError && <div className="alert alert-danger py-2 text-center small">{paymentError}</div>}

        {/* ============================================================== */}
        {/* TAB 1: CUSTOMER HOME / CURRENT USAGE DETAILS (SRS US15)         */}
        {/* ============================================================== */}
        {activeTab === 'HOME' && (
          <div className="d-flex flex-column align-items-center">
            {homeLoading && <div className="spinner-border text-primary my-3"></div>}

            {homeData && (
              <div className="cyan-box p-4 rounded shadow mb-4" style={{ width: '100%', maxWidth: '650px' }}>
                {/* Plan Name Header */}
                <h3 className="text-center fw-bold text-primary mb-3 text-uppercase">
                  {homeData.packageName}
                </h3>

                {/* Scheduled plan change alert if present */}
                {homeData.hasScheduledPlan && (
                  <div className="alert alert-warning py-2 small mb-3 text-center">
                    <strong>Scheduled Plan Change:</strong> Your switch to <strong>{homeData.scheduledPackageName}</strong> is scheduled and will automatically activate on <strong>{homeData.scheduledActivationDate}</strong> when your current billing cycle expires.
                  </div>
                )}

                {/* Period & Usage Stats Grid */}
                <div className="bg-white p-3 rounded border mb-3">
                  <div className="row mb-2">
                    <div className="col-6">
                      <span className="small text-muted fw-bold">FROM: </span>
                      <span className="fw-semibold">{homeData.billingStartDate}</span>
                    </div>
                    <div className="col-6 text-end">
                      <span className="small text-muted fw-bold">TO: </span>
                      <span className="fw-semibold">{homeData.billingEndDate}</span>
                    </div>
                  </div>

                  <hr className="my-2" />

                  <div className="row g-3 py-2">
                    <div className="col-6">
                      <div className="small text-muted fw-bold">USAGE:</div>
                      <div className="fs-5 fw-bold text-primary">{homeData.usageInGb} GB</div>
                    </div>
                    <div className="col-6 text-end">
                      <div className="small text-muted fw-bold">REMAINING DATA:</div>
                      <div className="fs-5 fw-bold text-success">{homeData.remainingDataMb} MB</div>
                    </div>
                    <div className="col-6">
                      <div className="small text-muted fw-bold">CURRENT AMOUNT:</div>
                      <div className="fs-5 fw-bold text-dark">${homeData.totalAmountUsd.toFixed(2)} USD</div>
                      <small className="text-muted">
                        (Base: ${homeData.monthlyChargeUsd.toFixed(2)} + Excess: ${homeData.excessChargeUsd.toFixed(2)})
                      </small>
                    </div>
                    <div className="col-6 text-end">
                      <div className="small text-muted fw-bold">DATA AFTER LIMIT:</div>
                      <div className="fs-5 fw-bold text-danger">{homeData.dataAfterLimitGb} GB</div>
                    </div>
                  </div>
                </div>

                {/* Action buttons */}
                <div className="d-flex justify-content-between align-items-center pt-2">
                  <button
                    className="btn btn-outline-primary btn-sm fw-bold"
                    onClick={() => setActiveTab('PENDING')}
                  >
                    Pending Bills ({homeData.pendingBillsCount})
                  </button>

                  <div className="d-flex gap-2">
                    <button
                      className="btn btn-outline-secondary btn-sm"
                      onClick={handleSimulateUsage}
                      title="Trigger IPDR simulator session"
                    >
                      ⚡ Ingest Usage (Simulate)
                    </button>

                    {homeData.pendingBillsCount > 0 ? (
                      <button
                        className="btn btn-primary px-4 fw-bold"
                        onClick={() => {
                          const bill = pendingBills.find(b => b.id === homeData.currentBillId) || pendingBills[0];
                          if (bill) {
                            setBillToPay(bill);
                            setActiveTab('PENDING');
                          } else {
                            setActiveTab('PENDING');
                          }
                        }}
                      >
                        PAY
                      </button>
                    ) : (
                      <span className="badge bg-success p-2 align-self-center">All Bills Settled</span>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ============================================================== */}
        {/* TAB 2: BILL DETAILS & SEARCH (SRS US16)                         */}
        {/* ============================================================== */}
        {activeTab === 'BILLS' && (
          <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h4 className="fw-bold mb-0 text-primary">BILL & TRANSACTION HISTORY</h4>
              
              {/* Search Bar matching SRS */}
              <div className="d-flex gap-2 align-items-center" style={{ width: '320px' }}>
                <span className="fw-bold small text-muted">Search:</span>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="Enter Bill ID (e.g. T183)"
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    fetchBills(e.target.value);
                  }}
                />
              </div>
            </div>

            <div className="table-responsive bg-white rounded shadow-sm border">
              <table className="table table-hover align-middle mb-0 text-center">
                <thead className="table-primary">
                  <tr>
                    <th>TRANSACTION / BILL ID</th>
                    <th>BILLING PERIOD</th>
                    <th>PACKAGE</th>
                    <th>USAGE</th>
                    <th>AMOUNT IN USD</th>
                    <th>STATUS</th>
                    <th>REMARK / PAYMENT MODE</th>
                    <th>ACTION</th>
                  </tr>
                </thead>
                <tbody>
                  {bills.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="py-4 text-muted">
                        No billing transactions found.
                      </td>
                    </tr>
                  ) : (
                    bills.map((b) => (
                      <tr key={b.id}>
                        <td className="fw-bold text-primary">{b.billNumber}</td>
                        <td className="small">{b.billingStartDate} to {b.billingEndDate}</td>
                        <td>{b.planName}</td>
                        <td>{b.usageInGb} GB</td>
                        <td className="fw-bold">${b.totalAmountUsd.toFixed(2)}</td>
                        <td>
                          <span className={`badge ${b.status === 'PAID' ? 'bg-success' : 'bg-danger'}`}>
                            {b.status}
                          </span>
                        </td>
                        <td className="small text-muted">{b.paymentMode ? `${b.paymentMode}` : (b.remark || '—')}</td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline-info px-2 py-0"
                            onClick={() => setSelectedBillForDetail(b)}
                            title="View Full Bill Details"
                          >
                            🔍 Details
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Bill Details Modal */}
            {selectedBillForDetail && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content cyan-box">
                    <div className="modal-header border-0 pb-0">
                      <h5 className="modal-title fw-bold text-primary">
                        BILL DETAILS: {selectedBillForDetail.billNumber}
                      </h5>
                      <button type="button" className="btn-close" onClick={() => setSelectedBillForDetail(null)}></button>
                    </div>
                    <div className="modal-body">
                      <div className="bg-white p-3 rounded border">
                        <div className="row g-2 small">
                          <div className="col-6"><strong>Plan:</strong> {selectedBillForDetail.planName}</div>
                          <div className="col-6"><strong>Status:</strong> <span className={`badge ${selectedBillForDetail.status === 'PAID' ? 'bg-success' : 'bg-danger'}`}>{selectedBillForDetail.status}</span></div>
                          <div className="col-6"><strong>Period Start:</strong> {selectedBillForDetail.billingStartDate}</div>
                          <div className="col-6"><strong>Period End:</strong> {selectedBillForDetail.billingEndDate}</div>
                          <div className="col-6"><strong>Data Allowance:</strong> {selectedBillForDetail.dataAllowanceGb} GB</div>
                          <div className="col-6"><strong>Total Consumed:</strong> {selectedBillForDetail.usageInGb} GB</div>
                          <div className="col-6"><strong>Remaining Data:</strong> {selectedBillForDetail.remainingDataMb} MB</div>
                          <div className="col-6"><strong>Data After Limit:</strong> {selectedBillForDetail.dataAfterLimitGb} GB</div>
                          <hr className="my-2" />
                          <div className="col-6"><strong>Base Package Charge:</strong> ${selectedBillForDetail.baseChargeUsd.toFixed(2)}</div>
                          <div className="col-6"><strong>Excess Charge:</strong> ${selectedBillForDetail.excessChargeUsd.toFixed(2)}</div>
                          <div className="col-12 fs-6 fw-bold text-dark mt-2">
                            Total Payable: ${selectedBillForDetail.totalAmountUsd.toFixed(2)} USD
                          </div>
                          {selectedBillForDetail.paidDate && (
                            <div className="col-12 text-success mt-1">
                              <strong>Paid On:</strong> {selectedBillForDetail.paidDate} ({selectedBillForDetail.paymentMode})
                            </div>
                          )}
                        </div>
                      </div>
                      <div className="text-center mt-3">
                        <button className="btn btn-secondary btn-sm px-4" onClick={() => setSelectedBillForDetail(null)}>
                          CLOSE
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ============================================================== */}
        {/* TAB 3: PENDING BILLS & PAYMENT (SRS US17, US18)                 */}
        {/* ============================================================== */}
        {activeTab === 'PENDING' && (
          <div>
            <h4 className="fw-bold mb-3 text-primary">PENDING BILLS</h4>

            {pendingBills.length === 0 ? (
              <div className="alert alert-success p-4 text-center">
                <h5 className="fw-bold mb-1">No Pending Bills!</h5>
                <p className="small text-muted mb-0">All bills for your account are completely settled.</p>
              </div>
            ) : (
              <div className="table-responsive bg-white rounded shadow-sm border mb-4">
                <table className="table table-hover align-middle mb-0 text-center">
                  <thead className="table-primary">
                    <tr>
                      <th>BILL ID</th>
                      <th>DATE</th>
                      <th>PLAN</th>
                      <th>AMOUNT</th>
                      <th>STATUS</th>
                      <th>REMARK</th>
                      <th>ACTION</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingBills.map((b) => (
                      <tr key={b.id}>
                        <td className="fw-bold text-primary">{b.billNumber}</td>
                        <td>{b.generatedDate}</td>
                        <td>{b.planName}</td>
                        <td className="fw-bold text-danger">${b.totalAmountUsd.toFixed(2)}</td>
                        <td>
                          <span className="badge bg-warning text-dark">{b.status}</span>
                        </td>
                        <td className="small text-muted">{b.remark || 'Awaiting payment'}</td>
                        <td>
                          <button
                            className="btn btn-sm btn-primary px-3 fw-bold"
                            onClick={() => {
                              setBillToPay(b);
                              setPaymentMsg('');
                              setPaymentError('');
                            }}
                          >
                            PAY
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Payment Section / Card (SRS US17) */}
            {billToPay && (
              <div className="d-flex justify-content-center mt-3">
                <div className="cyan-box p-4 rounded shadow" style={{ width: '100%', maxWidth: '580px' }}>
                  <h4 className="text-center fw-bold mb-3 text-primary">PAYMENT</h4>
                  
                  <div className="bg-white p-3 rounded border mb-3">
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted fw-bold">BILL NUMBER:</span>
                      <span className="fw-bold text-primary">{billToPay.billNumber}</span>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted fw-bold">PLAN:</span>
                      <span className="fw-bold">{billToPay.planName}</span>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Billing Cycle:</span>
                      <span>{billToPay.billingStartDate} to {billToPay.billingEndDate}</span>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Data Usage:</span>
                      <span>{billToPay.usageInGb} GB</span>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Data After Limit:</span>
                      <span>{billToPay.dataAfterLimitGb} GB</span>
                    </div>
                    <hr className="my-2" />
                    <div className="d-flex justify-content-between fs-5 fw-bold">
                      <span>PAYABLE AMOUNT:</span>
                      <span className="text-danger">${billToPay.totalAmountUsd.toFixed(2)} USD</span>
                    </div>
                  </div>

                  <div className="mb-4 row align-items-center">
                    <label className="col-4 col-form-label fw-bold small">PAYMENT MODE:</label>
                    <div className="col-8">
                      <select
                        className="form-select form-select-sm"
                        value={paymentMode}
                        onChange={(e) => setPaymentMode(e.target.value)}
                      >
                        <option value="PayTM">PayTM</option>
                        <option value="Net banking">Net banking</option>
                        <option value="UPI">UPI</option>
                        <option value="Credit Card">Credit Card</option>
                        <option value="Debit Card">Debit Card</option>
                        <option value="Online">Online Banking</option>
                      </select>
                    </div>
                  </div>

                  <div className="d-flex justify-content-center gap-3">
                    <button
                      className="btn btn-primary px-4 fw-bold"
                      onClick={() => setShowPayConfirm(true)}
                    >
                      PAY
                    </button>
                    <button
                      className="btn btn-secondary px-4"
                      onClick={() => setBillToPay(null)}
                    >
                      CANCEL
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Payment Confirmation Popup */}
            {showPayConfirm && billToPay && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content text-center p-4">
                    <h5 className="fw-bold mb-3">CONFIRM PAYMENT</h5>
                    <p className="mb-2">
                      Are you sure you want to process payment of <strong>${billToPay.totalAmountUsd.toFixed(2)} USD</strong> for bill <strong>{billToPay.billNumber}</strong> via <strong>{paymentMode}</strong>?
                    </p>
                    <div className="d-flex justify-content-center gap-3 mt-4">
                      <button
                        className="btn btn-primary px-4 fw-bold"
                        onClick={handleExecutePayment}
                        disabled={paying}
                      >
                        {paying ? 'Processing...' : 'CONFIRM'}
                      </button>
                      <button
                        className="btn btn-secondary px-4"
                        onClick={() => setShowPayConfirm(false)}
                        disabled={paying}
                      >
                        CANCEL
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ============================================================== */}
        {/* TAB 4: CHANGE PLAN (SRS US19)                                   */}
        {/* ============================================================== */}
        {activeTab === 'CHANGE_PLAN' && (
          <div className="d-flex flex-column align-items-center">
            <div className="cyan-box p-4 rounded shadow" style={{ width: '100%', maxWidth: '600px' }}>
              <h4 className="text-center fw-bold mb-4 text-primary">CHANGE PLAN</h4>

              {planChangeMsg && <div className="alert alert-success py-2 text-center small fw-bold">{planChangeMsg}</div>}
              {planChangeError && <div className="alert alert-danger py-2 text-center small">{planChangeError}</div>}

              <div className="alert alert-info py-2 small mb-3">
                <strong>SRS Rule:</strong> When changing to a new plan, your current plan will remain active until its expiry date. The new plan will automatically activate only after the current plan expires.
              </div>

              {homeData && (
                <div className="bg-white p-3 rounded border mb-4">
                  <div className="mb-2">
                    <span className="fw-bold text-muted small">CURRENT ACTIVE PLAN: </span>
                    <span className="fw-bold text-primary fs-6">{homeData.packageName}</span>
                  </div>
                  <div className="mb-2">
                    <span className="fw-bold text-muted small">EXPIRY DATE: </span>
                    <span className="fw-semibold">{homeData.billingEndDate}</span>
                  </div>

                  {homeData.hasScheduledPlan && (
                    <div className="mt-2 pt-2 border-top">
                      <span className="fw-bold text-warning small">PENDING SCHEDULED PLAN: </span>
                      <span className="fw-bold text-dark">{homeData.scheduledPackageName}</span>
                      <small className="d-block text-muted">
                        (Activates on: {homeData.scheduledActivationDate})
                      </small>
                    </div>
                  )}
                </div>
              )}

              <div className="mb-4 row align-items-center">
                <label className="col-4 col-form-label fw-bold small">CHOOSE NEW PLAN:</label>
                <div className="col-8">
                  <select
                    className="form-select form-select-sm"
                    value={selectedNewPlanId}
                    onChange={(e) => setSelectedNewPlanId(Number(e.target.value))}
                  >
                    {availablePlans.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.packageName} ({p.dataAllowanceGb} GB, ${p.monthlyChargeUsd}/mo, ${p.chargesAfterLimitPerMb}/MB)
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="d-flex justify-content-center gap-3">
                <button
                  className="btn btn-primary px-4 fw-bold"
                  onClick={() => setShowPlanConfirm(true)}
                  disabled={!selectedNewPlanId}
                >
                  CHANGE
                </button>
                <button
                  className="btn btn-secondary px-4"
                  onClick={() => setActiveTab('HOME')}
                >
                  CANCEL
                </button>
              </div>
            </div>

            {/* Plan Change Confirmation Dialog */}
            {showPlanConfirm && (
              <div className="modal d-block bg-dark bg-opacity-50">
                <div className="modal-dialog modal-dialog-centered">
                  <div className="modal-content text-center p-4">
                    <h5 className="fw-bold mb-3">CONFIRM PLAN CHANGE</h5>
                    <p className="mb-2">
                      Are you sure you want to change your plan to{' '}
                      <strong>
                        {availablePlans.find(p => p.id === Number(selectedNewPlanId))?.packageName}
                      </strong>?
                    </p>
                    <p className="small text-muted mb-0">
                      As per policy, your current plan ({homeData?.packageName}) will remain active until{' '}
                      <strong>{homeData?.billingEndDate}</strong>. Your new plan will activate immediately upon expiry.
                    </p>
                    <div className="d-flex justify-content-center gap-3 mt-4">
                      <button className="btn btn-primary px-4 fw-bold" onClick={handleExecutePlanChange}>
                        CONFIRM
                      </button>
                      <button className="btn btn-secondary px-4" onClick={() => setShowPlanConfirm(false)}>
                        CANCEL
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ============================================================== */}
        {/* TAB 5: USER REPORT (SRS US20)                                   */}
        {/* ============================================================== */}
        {activeTab === 'REPORT' && (
          <div>
            <h4 className="fw-bold mb-3 text-primary">CUSTOMER USAGE REPORT</h4>

            {/* Date Selection Form */}
            <div className="bg-white p-3 rounded shadow-sm border mb-4">
              <div className="row g-3 align-items-end">
                <div className="col-md-4">
                  <label className="form-label fw-bold small text-muted">FROM DATE:</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={fromDate}
                    onChange={(e) => setFromDate(e.target.value)}
                  />
                </div>
                <div className="col-md-4">
                  <label className="form-label fw-bold small text-muted">TO DATE:</label>
                  <input
                    type="date"
                    className="form-control form-control-sm"
                    value={toDate}
                    onChange={(e) => setToDate(e.target.value)}
                  />
                </div>
                <div className="col-md-4">
                  <button
                    className="btn btn-primary btn-sm w-100 fw-bold"
                    onClick={fetchReport}
                    disabled={reportLoading}
                  >
                    {reportLoading ? 'Loading Report...' : 'GENERATE REPORT'}
                  </button>
                </div>
              </div>
            </div>

            {reportError && <div className="alert alert-danger py-2">{reportError}</div>}

            {reportData && (
              <div>
                {/* Usage Summary Cards */}
                <div className="row g-3 mb-4">
                  <div className="col-md-4">
                    <div className="card border-0 shadow-sm bg-primary text-white p-3 text-center">
                      <div className="small fw-semibold">TOTAL CONSUMED</div>
                      <h3 className="fw-bold mb-0">{reportData.totalUsageGb} GB</h3>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="card border-0 shadow-sm bg-light p-3 text-center">
                      <div className="small text-muted fw-semibold">UPLOAD TRAFFIC</div>
                      <h3 className="fw-bold text-info mb-0">{reportData.totalUploadGb} GB</h3>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="card border-0 shadow-sm bg-light p-3 text-center">
                      <div className="small text-muted fw-semibold">DOWNLOAD TRAFFIC</div>
                      <h3 className="fw-bold text-success mb-0">{reportData.totalDownloadGb} GB</h3>
                    </div>
                  </div>
                </div>

                {/* Graphical Representation (Bar Chart) matching SRS US20 */}
                <div className="card border-0 shadow-sm p-4 mb-4 bg-white">
                  <h6 className="fw-bold text-center text-muted mb-4">
                    DAILY USAGE BREAKDOWN (MB)
                  </h6>
                  
                  {/* CSS / SVG Bar Chart */}
                  <div
                    className="d-flex align-items-end justify-content-between px-2 pt-3 border-bottom"
                    style={{ height: '220px', gap: '8px', overflowX: 'auto' }}
                  >
                    {reportData.dailyUsage.map((item) => {
                      const maxMb = Math.max(...reportData.dailyUsage.map(d => d.totalMb), 100);
                      const heightPercent = Math.min(100, Math.max(6, Math.round((item.totalMb / maxMb) * 100)));
                      return (
                        <div
                          key={item.date}
                          className="d-flex flex-column align-items-center flex-fill"
                          style={{ minWidth: '38px' }}
                        >
                          <small className="fw-bold text-primary mb-1" style={{ fontSize: '10px' }}>
                            {item.totalMb > 0 ? `${item.totalMb.toFixed(0)}M` : ''}
                          </small>
                          <div
                            className="w-100 rounded-top"
                            style={{
                              height: `${heightPercent}%`,
                              backgroundColor: item.totalMb > 0 ? '#0288d1' : '#e0e0e0',
                              transition: 'height 0.4s ease'
                            }}
                            title={`${item.date}: ${item.totalMb} MB (Up: ${item.uploadMb} MB, Down: ${item.downloadMb} MB)`}
                          ></div>
                          <span className="text-muted mt-2" style={{ fontSize: '9px', whiteSpace: 'nowrap' }}>
                            {item.date.substring(5)}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                  <div className="d-flex justify-content-center gap-4 mt-3 small text-muted">
                    <span><span className="badge bg-primary me-1">&nbsp;</span> Total MB Consumed</span>
                  </div>
                </div>

                {/* Detailed Table */}
                <div className="table-responsive bg-white rounded shadow-sm border">
                  <table className="table table-sm table-hover align-middle mb-0 text-center">
                    <thead className="table-light">
                      <tr>
                        <th>DATE</th>
                        <th>UPLOAD (MB)</th>
                        <th>DOWNLOAD (MB)</th>
                        <th>TOTAL (MB)</th>
                        <th>TOTAL (GB)</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reportData.dailyUsage.map((u) => (
                        <tr key={u.date}>
                          <td className="fw-semibold">{u.date}</td>
                          <td>{u.uploadMb} MB</td>
                          <td>{u.downloadMb} MB</td>
                          <td className="fw-bold text-primary">{u.totalMb} MB</td>
                          <td>{u.totalGb} GB</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
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