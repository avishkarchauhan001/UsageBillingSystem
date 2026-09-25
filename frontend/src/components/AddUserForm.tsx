import React, { useState } from 'react';

interface AddUserFormProps {
  onSuccess: () => void;
}

export const AddUserForm: React.FC<AddUserFormProps> = ({ onSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('ADMIN');
  const [securityQuestion, setSecurityQuestion] = useState('What is your pet name?');
  const [securityAnswer, setSecurityAnswer] = useState('');

  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const getPasswordStrength = (pass: string) => {
    if (pass.length < 4) return { text: 'Not Accepted', class: 'text-danger' };
    if (pass.length <= 6) return { text: 'Weak', class: 'strength-weak' };
    if (pass.length <= 8) return { text: 'Medium', class: 'strength-medium' };
    return { text: 'Strong', class: 'strength-strong' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (password !== confirmPassword) {
      setErrorMsg("Password doesn't match");
      return;
    }

    try {
      const res = await fetch('http://localhost:8081/api/admin/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, role, securityQuestion, securityAnswer })
      });

      const data = await res.json();
      if (!res.ok) {
        setErrorMsg(data.message);
      } else {
        setSuccessMsg('User added successfully');
        setTimeout(() => onSuccess(), 1500);
      }
    } catch (err) {
      setErrorMsg('Failed to add user');
    }
  };

  const strength = getPasswordStrength(password);

  return (
    <div className="p-4" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h5 className="text-center fw-bold mb-4">ENTER DETAILS TO ADD NEW USER</h5>

      {errorMsg && <div className="alert alert-danger py-1">{errorMsg}</div>}
      {successMsg && <div className="alert alert-success py-1">{successMsg}</div>}

      <form onSubmit={handleSubmit}>
        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">USER NAME:</label>
          <div className="col-8">
            <input type="text" className="form-control" value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
        </div>

        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">PASSWORD:</label>
          <div className="col-8">
            <input type="password" className="form-control" value={password} onChange={e => setPassword(e.target.value)} required />
            {password && <small className={strength.class}>Strength: {strength.text}</small>}
          </div>
        </div>

        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">CONFIRM PASSWORD:</label>
          <div className="col-8">
            <input type="password" className="form-control" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
          </div>
        </div>

        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">Role:</label>
          <div className="col-8">
            <select className="form-select" value={role} onChange={e => setRole(e.target.value)}>
              <option value="ADMIN">ADMIN</option>
              <option value="OPERATOR">OPERATOR</option>
              <option value="CUSTOMER">CUSTOMER</option>
            </select>
          </div>
        </div>

        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">SECURITY QUESTION:</label>
          <div className="col-8">
            <select className="form-select" value={securityQuestion} onChange={e => setSecurityQuestion(e.target.value)}>
              <option value="What is your pet name?">What is your pet name?</option>
              <option value="What is your birthplace?">What is your birthplace?</option>
            </select>
          </div>
        </div>

        <div className="mb-3 row">
          <label className="col-4 col-form-label fw-bold">ANSWER:</label>
          <div className="col-8">
            <input type="text" className="form-control" value={securityAnswer} onChange={e => setSecurityAnswer(e.target.value)} required />
          </div>
        </div>

        <div className="text-center mt-4">
          <button type="submit" className="btn btn-primary fw-bold px-4">ADD USER</button>
        </div>
      </form>
    </div>
  );
};