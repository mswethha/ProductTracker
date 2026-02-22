import { useState } from 'react';

export default function Profile() {
  const [code, setCode] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const getCode = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await fetch('/api/auth/link-code', {
        headers: { Authorization: `Basic ${sessionStorage.getItem('auth')}` }
      });
      const data = await res.json();
      setCode(data.code);
    } catch (e) {
      setError('Failed to generate code. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '0 auto' }}>
      <div className="card">
        <h2>Link Telegram</h2>
        <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
          Connect your Telegram account to receive instant stock alerts.
        </p>
        <ol style={{ color: '#a1a1aa', marginBottom: '1.5rem', paddingLeft: '1.2rem' }}>
          <li>Click "Generate Code" below</li>
          <li>Open Telegram and message your bot</li>
          <li>Send the 6-digit code to the bot</li>
          <li>Done! You'll receive alerts automatically</li>
        </ol>

        {code ? (
          <div style={{
            background: '#27272a', border: '1px solid #6366f1',
            borderRadius: '8px', padding: '1.5rem', textAlign: 'center',
            marginBottom: '1rem'
          }}>
            <p style={{ color: '#a1a1aa', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
              Your link code (valid for one use):
            </p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#6366f1', letterSpacing: '0.3rem' }}>
              {code}
            </p>
            <p style={{ color: '#a1a1aa', fontSize: '0.85rem' }}>
              Send this to your Telegram bot now
            </p>
          </div>
        ) : null}

        {error && <p className="error-msg">{error}</p>}

        <button
          className="btn btn-primary"
          onClick={getCode}
          disabled={loading}
          style={{ width: '100%' }}
        >
          {loading ? 'Generating...' : code ? 'Generate New Code' : 'Generate Code'}
        </button>
      </div>
    </div>
  );
}