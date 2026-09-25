import React, { useState } from 'react';
import { ForgotPasswordModal } from '../components/ForgotPasswordModal';

interface LoginPageProps {
  onLoginSuccess: (username: string, role: string) => void;
  logoutMessage?: string;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess, logoutMessage }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [resetSuccessMsg, setResetSuccessMsg] = useState('');
  const [showForgotPassword, setShowForgotPassword] = useState(false);

  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const cleanUsername = username.trim();
    const cleanPassword = password.trim();

    try {
      const response = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: cleanUsername, password: cleanPassword })
      });

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        setError((data && data.message) ? data.message : 'Wrong username or password');
        return;
      }

      onLoginSuccess(data.username, data.role);
    } catch (err: any) {
      console.error('Login error:', err);
      setError('Unable to connect to the backend server at http://localhost:8081. Please ensure the backend is running.');
    }
  };

  return (
    <div className="container min-vh-100 d-flex flex-column justify-content-center align-items-center">
      {logoutMessage && (
        <div className="alert alert-success fw-bold text-center mb-3">
          {logoutMessage}
        </div>
      )}

      {resetSuccessMsg && (
        <div className="alert alert-success fw-bold text-center mb-3" style={{ width: '400px' }}>
          {resetSuccessMsg}
        </div>
      )}

      <div className="cyan-box p-4 rounded shadow" style={{ width: '400px' }}>
        <h3 className="text-center fw-bold mb-4">BILLING SYSTEM</h3>

        {error && <div className="alert alert-danger py-2 text-center small fw-semibold">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3 row">
            <label className="col-4 col-form-label fw-bold">Username:</label>
            <div className="col-8">
              <input
                type="text"
                className="form-control"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. customer1"
                required
              />
            </div>
          </div>

          <div className="mb-2 row">
            <label className="col-4 col-form-label fw-bold">Password:</label>
            <div className="col-8">
              <div className="input-group">
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password"
                  required
                />
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>
          </div>

          <div className="text-end mb-3">
            <button 
              type="button" 
              className="btn btn-link p-0 small text-primary text-decoration-none border-0 bg-transparent"
              onClick={() => setShowForgotPassword(true)}
            >
              Forgot Password ?
            </button>
          </div>

          <div className="text-center">
            <button type="submit" className="btn btn-primary fw-bold px-4">
              LOGIN
            </button>
          </div>
        </form>
      </div>

      {showForgotPassword && (
        <ForgotPasswordModal
          onClose={() => setShowForgotPassword(false)}
          onSuccess={(msg) => {
            setResetSuccessMsg(msg);
            setError('');
          }}
        />
      )}
    </div>
  );
};