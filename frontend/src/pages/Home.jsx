import { useState, useEffect } from 'react';
import { getProductStatus, linkTelegram } from '../api';  // add linkTelegram
export default function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProductStatus()
      .then(setProducts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="card"><p>Loading product status…</p></div>;
  if (error) return <div className="card"><p className="error-msg">{error}</p></div>;

  return (
  <div>
    <div className="card">
      <h2>Product status</h2>
      <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
        Tracked products and current availability. Subscribe via Telegram for instant alerts when status changes.
      </p>
      <ul className="product-list">
        {products.length === 0 ? (
          <li>No products configured yet.</li>
        ) : (
          products.map((p) => (
            <li key={p.id}>
              <span>
                <strong>{p.name}</strong>
                {p.url && (
                  <span style={{ marginLeft: '0.5rem' }}>
                    <a href={p.url} target="_blank" rel="noopener noreferrer">View product</a>
                  </span>
                )}
              </span>
              <span className={`status-badge ${(p.status || '').toLowerCase().replace('_', '-')}`}>
                {p.statusMessage || p.status || 'Unknown'}
              </span>
            </li>
          ))
        )}
      </ul>
    </div>
          {user && <LinkTelegramForm />}
  </div>
  );
}
function LinkTelegramForm() {
  const [chatId, setChatId] = useState('');
  const [msg, setMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLink = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await linkTelegram(chatId);
      setMsg('✅ Telegram linked! You will now receive alerts.');
    } catch (err) {
      setMsg('❌ ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Link Telegram</h2>
      <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
        1. Open Telegram and message <strong>@YourBotUsername</strong>, send <code>/start</code><br/>
        2. The bot will reply with your Chat ID — paste it below.
      </p>
      <form onSubmit={handleLink}>
        <div className="form-group">
          <label>Your Telegram Chat ID</label>
          <input
            type="text"
            value={chatId}
            onChange={e => setChatId(e.target.value)}
            placeholder="e.g. 123456789"
            required
          />
        </div>
        {msg && (
          <p className={msg.startsWith('✅') ? 'success-msg' : 'error-msg'}>
            {msg}
          </p>
        )}
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Linking…' : 'Link Telegram'}
        </button>
      </form>
    </div>
  );
}
