import { useState, useEffect } from 'react';
import { getAdminProducts, getAdminSubscribers } from '../api';

export default function Admin() {
  const [products, setProducts] = useState([]);
  const [subscribers, setSubscribers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([getAdminProducts(), getAdminSubscribers()])
      .then(([prods, subs]) => {
        setProducts(prods || []);
        setSubscribers(subs || []);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="card"><p>Loading admin data…</p></div>;
  if (error) return <div className="card"><p className="error-msg">{error}</p></div>;

  const formatDate = (d) => {
    if (!d) return '—';
    try {
      const date = new Date(d);
      return date.toLocaleString();
    } catch {
      return d;
    }
  };

  return (
    <>
      <div className="card">
        <h2>Admin panel</h2>
        <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
          Monitor product status, when each product was last in stock, and Telegram subscribers.
        </p>
      </div>

      <div className="admin-section card">
        <h3>Product status & last available</h3>
        <table>
          <thead>
            <tr>
              <th>Product</th>
              <th>Status</th>
              <th>Last checked</th>
              <th>Last in stock</th>
              <th>URL</th>
            </tr>
          </thead>
          <tbody>
            {products.length === 0 ? (
              <tr><td colSpan={5}>No products</td></tr>
            ) : (
              products.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>
                    <span className={`status-badge ${(p.status || '').toLowerCase().replace('_', '-')}`}>
                      {p.statusMessage || p.status || '—'}
                    </span>
                  </td>
                  <td>{formatDate(p.lastChecked)}</td>
                  <td>{formatDate(p.lastInStockAt)}</td>
                  <td>{p.url ? <a href={p.url} target="_blank" rel="noopener noreferrer">Link</a> : '—'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="admin-section card">
        <h3>Subscribers (Telegram)</h3>
        <p style={{ color: '#a1a1aa', fontSize: '0.9rem', marginBottom: '0.75rem' }}>
          Users who subscribed via the Telegram bot and receive status change alerts.
        </p>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Chat ID</th>
            </tr>
          </thead>
          <tbody>
            {subscribers.length === 0 ? (
              <tr><td colSpan={2}>No subscribers yet</td></tr>
            ) : (
              subscribers.map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td>{s.chatId}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
