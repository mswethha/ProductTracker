const API_BASE = '/api';

function getAuthHeader() {
  const auth = sessionStorage.getItem('auth');
  if (!auth) return {};
  return { Authorization: `Basic ${auth}` };
}

export async function register(username, email, password) {
  const form = new URLSearchParams({ username, email, password });
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: form,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || res.statusText);
  }
  return res.json();
}

export async function getMe() {
  const res = await fetch(`${API_BASE}/auth/me`, { headers: getAuthHeader() });
  if (res.status === 401) return null;
  if (!res.ok) throw new Error('Failed to get user');
  return res.json();
}

export async function getProductStatus() {
  const res = await fetch(`${API_BASE}/products/status`);
  if (!res.ok) throw new Error('Failed to load product status');
  return res.json();
}

export async function getAdminProducts() {
  const res = await fetch(`${API_BASE}/admin/products`, { headers: getAuthHeader() });
  if (res.status === 401 || res.status === 403) return null;
  if (!res.ok) throw new Error('Failed to load admin products');
  return res.json();
}

export async function getAdminSubscribers() {
  const res = await fetch(`${API_BASE}/admin/subscribers`, { headers: getAuthHeader() });
  if (res.status === 401 || res.status === 403) return null;
  if (!res.ok) throw new Error('Failed to load subscribers');
  return res.json();
}

export function setAuth(username, password) {
  if (!username || !password) {
    sessionStorage.removeItem('auth');
    return;
  }
  sessionStorage.setItem('auth', btoa(`${username}:${password}`));
}

export function clearAuth() {
  sessionStorage.removeItem('auth');
}
export async function linkTelegram(chatId) {
  const res = await fetch(`${API_BASE}/auth/link-telegram`, {
    method: 'POST',
    headers: { ...getAuthHeader(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ chatId }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || res.statusText);
  }
  return res.json();
}