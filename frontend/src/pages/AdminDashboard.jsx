import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function AdminDashboard() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => { fetchOrders(); }, [page]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/orders', { params: { page, size: 15 } });
      setOrders(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (orderId, status) => {
    try {
      await api.put(`/admin/orders/${orderId}/status`, { status });
      fetchOrders();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  const getStatusBadge = (status) => <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>;

  return (
    <div className="container">
      <h1 className="page-title">Admin Dashboard</h1>

      <div className="admin-grid">
        <Link to="/admin/products" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="card stat-card">
            <div className="stat-value">📦</div>
            <div className="stat-label">Manage Products</div>
          </div>
        </Link>
        <div className="card stat-card">
          <div className="stat-value">{orders.length}</div>
          <div className="stat-label">Orders (This Page)</div>
        </div>
      </div>

      <h2 style={{ fontSize: '1.3rem', marginBottom: '1rem' }}>All Orders</h2>

      {loading ? (
        <div className="loading">Loading orders...</div>
      ) : (
        <div className="card">
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Date</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Update Status</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>{order.items?.length || 0}</td>
                    <td style={{ fontWeight: 600 }}>${order.totalAmount?.toFixed(2)}</td>
                    <td>{getStatusBadge(order.status)}</td>
                    <td>
                      <select
                        value={order.status}
                        onChange={(e) => updateStatus(order.id, e.target.value)}
                        style={{ padding: '0.3rem 0.5rem', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                      >
                        {['CREATED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map(s => (
                          <option key={s} value={s}>{s}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '1.5rem' }}>
          <button className="btn btn-outline btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
          <span style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }}>Page {page + 1} of {totalPages}</span>
          <button className="btn btn-outline btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
        </div>
      )}
    </div>
  );
}
