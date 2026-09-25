import React, { useState } from 'react';
import { Captcha } from './Captcha';

interface ChangePasswordProps {
  username: string;
  onClose: () => void;
  onSuccess: () => void;
}

export const ChangePasswordModal: React.FC<ChangePasswordProps> = ({ username, onClose, onSuccess }) => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [retypePassword, setRetypePassword] = useState('');
  const [captchaInput, setCaptchaInput] = useState('');
  const [generatedCaptcha, setGeneratedCaptcha] = useState('');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (captchaInput !== generatedCaptcha) {
      setError('Invalid Captcha');
      return;
    }

    if (newPassword !== retypePassword) {
      setError('Passwords provided do not match');
      return;
    }

    try {
      const res = await fetch('http://localhost:8081/api/auth/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username,
          oldPassword: currentPassword,
          newPassword
        })
      });

      const data = await res.json();
      if (!res.ok) {
        setError(data.message || 'Failed to change password');
      } else {
        // Display success message directly inside the modal
        setSuccessMsg('Password changed successfully! Please login again.');
        
        // Clear fields
        setCurrentPassword('');
        setNewPassword('');
        setRetypePassword('');
        setCaptchaInput('');

        // Trigger parent callback after a brief delay so user sees the message
        setTimeout(() => {
          onSuccess();
        }, 1500);
      }
    } catch (err) {
      setError('Failed to connect to the server');
    }
  };

  return (
    <div className="modal d-block bg-dark bg-opacity-50">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content cyan-box">
          <div className="modal-header border-0">
            <h5 className="modal-title fw-bold">CHANGE PASSWORD</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            {/* Display error message inside modal if present */}
            {error && <div className="alert alert-danger py-1 text-center small">{error}</div>}

            {/* Display success message inside modal if present */}
            {successMsg && <div className="alert alert-success py-1 text-center small">{successMsg}</div>}

            <form onSubmit={handleSubmit}>
              <div className="mb-2">
                <label className="form-label fw-bold small">CURRENT PASSWORD:</label>
                <input 
                  type="password" 
                  className="form-control form-control-sm" 
                  value={currentPassword} 
                  onChange={e => setCurrentPassword(e.target.value)} 
                  required 
                  disabled={!!successMsg}
                />
              </div>

              <div className="mb-2">
                <label className="form-label fw-bold small">NEW PASSWORD:</label>
                <input 
                  type="password" 
                  className="form-control form-control-sm" 
                  value={newPassword} 
                  onChange={e => setNewPassword(e.target.value)} 
                  required 
                  disabled={!!successMsg}
                />
              </div>

              <div className="mb-2">
                <label className="form-label fw-bold small">RE-ENTER NEW PASSWORD:</label>
                <input 
                  type="password" 
                  className="form-control form-control-sm" 
                  value={retypePassword} 
                  onChange={e => setRetypePassword(e.target.value)} 
                  required 
                  disabled={!!successMsg}
                />
              </div>

              <div className="mb-3 d-flex align-items-center justify-content-between mt-3">
                <Captcha onValidate={(val) => setGeneratedCaptcha(val)} />
                <input
                  type="text"
                  className="form-control form-control-sm ms-2"
                  placeholder="ENTER CAPTCHA"
                  value={captchaInput}
                  onChange={e => setCaptchaInput(e.target.value)}
                  required
                  disabled={!!successMsg}
                />
              </div>

              <div className="d-flex justify-content-center gap-2 mt-4">
                <button type="submit" className="btn btn-primary btn-sm px-4" disabled={!!successMsg}>
                  Change
                </button>
                <button type="button" className="btn btn-secondary btn-sm px-4" onClick={onClose}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};