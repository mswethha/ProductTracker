import { useState, useEffect } from 'react';
import { getProductStatus } from '../api';

export default function ProductStatus() {
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
    <div style={{ maxWidth: '700px', margin: '0 auto' }}>
      <div className="card">
        <h2>Product Status</h2>
        <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
          Live availability of tracked products.
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
    </div>
  );
}