import React, { useState } from 'react';

interface Props {
  onClose: () => void;
  onSuccess: (message: string) => void;
}

export const ForgotPasswordModal: React.FC<Props> = ({ onClose, onSuccess }) => {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [username, setUsername] = useState('');
  const [question, setQuestion] = useState('');
  const [securityAnswer, setSecurityAnswer] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Step 1: Fetch security question
  const handleFetchQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!username.trim()) {
      setError('Please enter username');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch(`http://localhost:8081/api/auth/security-question/${encodeURIComponent(username.trim())}`);
      const data = await res.json();
      if (!res.ok) {
        setError(data.message || 'User does not exist');
      } else {
        setQuestion(data.securityQuestion);
        setStep(2);
      }
    } catch (err) {
      setError('Failed to contact server');
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify security answer
  const handleVerifyAnswer = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!securityAnswer.trim()) {
      setError('Please enter security answer');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('http://localhost:8081/api/auth/forgot-password/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username.trim(), securityAnswer: securityAnswer.trim() })
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.message || 'Security answer is incorrect');
      } else {
        setStep(3);
      }
    } catch (err) {
      setError('Failed to contact server');
    } finally {
      setLoading(false);
    }
  };

  // Step 3: Reset password
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('Passwords provided do not match');
      return;
    }

    if (newPassword.length < 1) {
      setError('Password cannot be empty');
      return;
    }

    setLoading(true);
    try {
      const res = await fetch('http://localhost:8081/api/auth/forgot-password/reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: username.trim(),
          securityAnswer: securityAnswer.trim(),
          newPassword
        })
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.message || 'Failed to reset password');
      } else {
        onSuccess('Password reset successfully! Please login with your new password.');
        onClose();
      }
    } catch (err) {
      setError('Failed to contact server');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal d-block bg-dark bg-opacity-50">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content cyan-box">
          <div className="modal-header border-0 pb-0">
            <h5 className="modal-title fw-bold text-primary">
              {step === 1 && 'FORGOT PASSWORD'}
              {step === 2 && 'SECURITY VERIFICATION'}
              {step === 3 && 'RESET PASSWORD'}
            </h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>

          <div className="modal-body">
            {error && <div className="alert alert-danger py-1 text-center small">{error}</div>}

            {step === 1 && (
              <form onSubmit={handleFetchQuestion}>
                <p className="small text-muted mb-3">
                  Enter your username to retrieve your registered security question.
                </p>
                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">USERNAME:</label>
                  <div className="col-8">
                    <input
                      type="text"
                      className="form-control form-control-sm"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      required
                      autoFocus
                    />
                  </div>
                </div>
                <div className="d-flex justify-content-center gap-2 mt-4">
                  <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold" disabled={loading}>
                    {loading ? 'Checking...' : 'NEXT'}
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm px-4" onClick={onClose}>
                    CANCEL
                  </button>
                </div>
              </form>
            )}

            {step === 2 && (
              <form onSubmit={handleVerifyAnswer}>
                <div className="mb-3 p-2 bg-white rounded border">
                  <span className="small text-muted d-block">SECURITY QUESTION:</span>
                  <span className="fw-bold text-dark">{question}</span>
                </div>
                <div className="mb-3 row">
                  <label className="col-4 col-form-label fw-bold small">YOUR ANSWER:</label>
                  <div className="col-8">
                    <input
                      type="text"
                      className="form-control form-control-sm"
                      value={securityAnswer}
                      onChange={(e) => setSecurityAnswer(e.target.value)}
                      required
                      autoFocus
                    />
                  </div>
                </div>
                <div className="d-flex justify-content-center gap-2 mt-4">
                  <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold" disabled={loading}>
                    {loading ? 'Verifying...' : 'NEXT'}
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm px-4" onClick={() => setStep(1)}>
                    BACK
                  </button>
                </div>
              </form>
            )}

            {step === 3 && (
              <form onSubmit={handleResetPassword}>
                <div className="mb-2 row">
                  <label className="col-5 col-form-label fw-bold small">NEW PASSWORD:</label>
                  <div className="col-7">
                    <input
                      type="password"
                      className="form-control form-control-sm"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                      autoFocus
                    />
                  </div>
                </div>
                <div className="mb-3 row">
                  <label className="col-5 col-form-label fw-bold small">CONFIRM PASSWORD:</label>
                  <div className="col-7">
                    <input
                      type="password"
                      className="form-control form-control-sm"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="d-flex justify-content-center gap-2 mt-4">
                  <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold" disabled={loading}>
                    {loading ? 'Saving...' : 'RESET PASSWORD'}
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm px-4" onClick={onClose}>
                    CANCEL
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
