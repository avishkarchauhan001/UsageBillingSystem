import React, { useState, useEffect } from 'react';

interface CaptchaProps {
  onValidate: (captchaValue: string) => void;
}

export const Captcha: React.FC<CaptchaProps> = ({ onValidate }) => {
  const [captchaText, setCaptchaText] = useState('');

  const generateCaptcha = () => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 6; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setCaptchaText(result);
    onValidate(result);
  };

  useEffect(() => {
    generateCaptcha();
  }, []);

  return (
    <div className="d-flex align-items-center gap-2">
      <span
        className="p-2 border border-secondary fw-bold text-decoration-line-through bg-white"
        style={{ letterSpacing: '4px', fontFamily: 'monospace' }}
      >
        {captchaText}
      </span>
      <button type="button" className="btn btn-sm btn-outline-secondary" onClick={generateCaptcha}>
        Refresh
      </button>
    </div>
  );
};