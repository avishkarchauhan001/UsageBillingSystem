import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';

export const SignupPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [role, setRole] = useState('CUSTOMER');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [securityQuestion, setSecurityQuestion] = useState('');
  const [securityAnswer, setSecurityAnswer] = useState('');
  
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    const roleParam = searchParams.get('role');
    if (roleParam && ['ADMIN', 'OPERATOR', 'CUSTOMER'].includes(roleParam.toUpperCase())) {
      setRole(roleParam.toUpperCase());
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    try {
      const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username,
          password,
          role,
          securityQuestion,
          securityAnswer,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'Registration failed');
        return;
      }

      // Display in-page success message instead of browser alert
      setSuccessMsg('Registration successful! Redirecting to login page...');

      // Redirect to login page after 2 seconds
      setTimeout(() => {
        navigate('/login');
      }, 2000);

    } catch (err) {
      setError('Server unreachable. Make sure backend service is running on port 8081.');
    }
  };

  return (
    <div className="container min-vh-100 d-flex flex-column justify-content-center align-items-center py-4">
      <div className="card p-4 rounded shadow border-0" style={{ width: '400px' }}>
        <h3 className="text-center fw-bold mb-3">SIGN UP</h3>

        <div className="alert alert-info text-center py-1 fw-bold small mb-3">
          SELECTED ROLE: <span className="text-primary">{role}</span>
        </div>

        {/* Success Banner directly below SIGN UP */}
        {successMsg && (
          <div className="alert alert-success py-2 text-center small fw-semibold mb-3">
            {successMsg}
          </div>
        )}

        {/* Error Banner */}
        {error && (
          <div className="alert alert-danger py-2 text-center small fw-semibold mb-3">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label fw-bold small">Role</label>
            <input type="text" className="form-control bg-light" value={role} readOnly />
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold small">Username</label>
            <input
              type="text"
              className="form-control"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold small">Password</label>
            <input
              type="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold small">Security Question</label>
            <input
              type="text"
              className="form-control"
              value={securityQuestion}
              onChange={(e) => setSecurityQuestion(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label fw-bold small">Security Answer</label>
            <input
              type="text"
              className="form-control"
              value={securityAnswer}
              onChange={(e) => setSecurityAnswer(e.target.value)}
              required
            />
          </div>

          <button 
            type="submit" 
            className="btn btn-primary fw-bold w-100 mt-2"
            disabled={!!successMsg}
          >
            REGISTER
          </button>
        </form>

        <div className="text-center mt-3 pt-2 border-top">
          <span className="small text-muted">Already a user? </span>
          <Link to="/login" className="small fw-bold text-decoration-none">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};