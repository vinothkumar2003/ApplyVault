import React, { useState, useEffect } from 'react';

export default function App() {
  // Navigation & Auth States
  const [view, setView] = useState('login'); // 'login', 'register', 'dashboard'
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState(null);

  // Form Inputs: Login
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');

  // Form Inputs: Register
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');

  // Vault Management States
  const [vaults, setVaults] = useState([]);
  const [editingVaultId, setEditingVaultId] = useState(null);
  const [companyName, setCompanyName] = useState('');
  const [vaultTitle, setVaultTitle] = useState('');
  const [vaultUrl, setVaultUrl] = useState('');
  const [description, setDescription] = useState('');
  const [vaultStatus, setVaultStatus] = useState('DRAFT');

  // General App feedback
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const USER_API = 'http://localhost:8080/api/users';
  const VAULT_API = 'http://localhost:8080/api/vaults';

  // --- AUTH ACTIONS ---

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const response = await fetch(`${USER_API}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: identifier.includes('@') ? null : identifier,
          email: identifier.includes('@') ? identifier : null,
          password
        }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(typeof data === 'string' ? data : 'Login failed');
      
      setCurrentUser(data.user);
      setToken(data.jwt);
      setView('dashboard');
      fetchVaults(data.user.id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const response = await fetch(USER_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: regUsername, email: regEmail, password: regPassword }),
      });
      const data = await response.text();
      if (!response.ok) throw new Error(data || 'Registration failed');
      
      setSuccess('Registration successful! Please log in.');
      setView('login');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setToken(null);
    setVaults([]);
    setView('login');
    setIdentifier('');
    setPassword('');
  };

  // --- VAULT ACTIONS ---

  const fetchVaults = async (userId) => {
    try {
      const response = await fetch(`${VAULT_API}/user/${userId}`);
      if (response.ok) {
        const data = await response.json();
        setVaults(data);
      }
    } catch (err) {
      console.error('Failed to load vaults', err);
    }
  };

  const handleSaveVault = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const vaultPayload = { companyName, vaultTitle, vaultUrl, description, status: vaultStatus };

    try {
      let response;
      if (editingVaultId) {
        // PATCH update matching VaultController: @PatchMapping("/{id}/user/{userId}")
        response = await fetch(`${VAULT_API}/${editingVaultId}/user/${currentUser.id}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(vaultPayload),
        });
      } else {
        // POST create matching VaultController: @PostMapping("/user/{userId}")
        response = await fetch(`${VAULT_API}/user/${currentUser.id}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(vaultPayload),
        });
      }

      if (!response.ok) throw new Error('Failed to save vault record.');

      setSuccess(editingVaultId ? 'Vault updated successfully!' : 'Vault created successfully!');
      resetVaultForm();
      fetchVaults(currentUser.id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleEditClick = (vault) => {
    setEditingVaultId(vault.id);
    setCompanyName(vault.companyName);
    setVaultTitle(vault.vaultTitle);
    setVaultUrl(vault.vaultUrl || '');
    setDescription(vault.description || '');
    setVaultStatus(vault.status || 'DRAFT');
  };

  const handleDeleteVault = async (vaultId) => {
    if (!window.confirm('Are you sure you want to delete this vault?')) return;
    try {
      const response = await fetch(`${VAULT_API}/${vaultId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: token || ''
      });
      if (!response.ok) throw new Error('Failed to delete vault');
      
      setSuccess('Vault deleted successfully.');
      fetchVaults(currentUser.id);
    } catch (err) {
      setError(err.message);
    }
  };

  const resetVaultForm = () => {
    setEditingVaultId(null);
    setCompanyName('');
    setVaultTitle('');
    setVaultUrl('');
    setDescription('');
    setVaultStatus('DRAFT');
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h2>Vault Security Portal</h2>
        {currentUser && (
          <button onClick={handleLogout} style={styles.logoutBtn}>Logout</button>
        )}
      </header>

      {error && <div style={styles.errorBanner}>{error}</div>}
      {success && <div style={styles.successBanner}>{success}</div>}

      {/* LOGIN VIEW */}
      {view === 'login' && (
        <div style={styles.card}>
          <h3>User Login</h3>
          <form onSubmit={handleLogin} style={styles.form}>
            <input
              type="text"
              placeholder="Username or Email"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              style={styles.input}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={styles.input}
              required
            />
            <button type="submit" style={styles.button}>Login</button>
          </form>
          <p style={styles.switchText}>
            Need an account? <span style={styles.link} onClick={() => setView('register')}>Register</span>
          </p>
        </div>
      )}

      {/* REGISTER VIEW */}
      {view === 'register' && (
        <div style={styles.card}>
          <h3>Register Account</h3>
          <form onSubmit={handleRegister} style={styles.form}>
            <input
              type="text"
              placeholder="Username"
              value={regUsername}
              onChange={(e) => setRegUsername(e.target.value)}
              style={styles.input}
              required
            />
            <input
              type="email"
              placeholder="Email"
              value={regEmail}
              onChange={(e) => setRegEmail(e.target.value)}
              style={styles.input}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={regPassword}
              onChange={(e) => setRegPassword(e.target.value)}
              style={styles.input}
              required
            />
            <button type="submit" style={styles.button}>Sign Up</button>
          </form>
          <p style={styles.switchText}>
            Already registered? <span style={styles.link} onClick={() => setView('login')}>Login</span>
          </p>
        </div>
      )}

      {/* DASHBOARD VIEW (User Details & Vault CRUD) */}
      {view === 'dashboard' && currentUser && (
        <div>
          <div style={styles.card}>
            <h3>Welcome, {currentUser.username}</h3>
            <p><strong>Email:</strong> {currentUser.email} | <strong>Role:</strong> {currentUser.role}</p>
          </div>

          {/* Vault Form Section (Create / Edit) */}
          <div style={styles.card}>
            <h3>{editingVaultId ? 'Edit Vault Entry' : 'Create New Vault Entry'}</h3>
            <form onSubmit={handleSaveVault} style={styles.form}>
              <input
                type="text"
                placeholder="Company Name"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                style={styles.input}
                required
              />
              <input
                type="text"
                placeholder="Vault Title"
                value={vaultTitle}
                onChange={(e) => setVaultTitle(e.target.value)}
                style={styles.input}
                required
              />
              <input
                type="text"
                placeholder="Vault URL (Optional)"
                value={vaultUrl}
                onChange={(e) => setVaultUrl(e.target.value)}
                style={styles.input}
              />
              <textarea
                placeholder="Description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{ ...styles.input, height: '80px' }}
              />
              <select
                value={vaultStatus}
                onChange={(e) => setVaultStatus(e.target.value)}
                style={styles.input}
              >
                <option value="DRAFT">Draft</option>
                <option value="SUBMITTED">Submitted</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
              </select>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="submit" style={styles.button}>
                  {editingVaultId ? 'Update Vault' : 'Create Vault'}
                </button>
                {editingVaultId && (
                  type="button",
                  <button type="button" onClick={resetVaultForm} style={styles.cancelBtn}>
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>

          {/* Vaults Listing Table */}
          <div style={styles.card}>
            <h3>Your Vault Cards</h3>
            {vaults.length === 0 ? (
              <p>No vaults found. Create your first one above!</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={styles.th}>Company</th>
                      <th style={styles.th}>Title</th>
                      <th style={styles.th}>Status</th>
                      <th style={styles.th}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vaults.map((vault) => (
                      <tr key={vault.id}>
                        <td style={styles.td}>{vault.companyName}</td>
                        <td style={styles.td}>{vault.vaultTitle}</td>
                        <td style={styles.td}><span style={styles.badge}>{vault.status}</span></td>
                        <td style={styles.td}>
                          <button onClick={() => handleEditClick(vault)} style={styles.editBtn}>Edit</button>
                          <button onClick={() => handleDeleteVault(vault.id)} style={styles.deleteBtn}>Delete</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// Styles
const styles = {
  container: { fontFamily: 'Inter, sans-serif', maxWidth: '700px', margin: '30px auto', padding: '20px', backgroundColor: '#f8fafc', borderRadius: '12px', boxShadow: '0 4px 15px rgba(0,0,0,0.08)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  card: { backgroundColor: '#ffffff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.04)', marginBottom: '20px' },
  form: { display: 'flex', flexDirection: 'column', gap: '12px' },
  input: { padding: '10px 12px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '0.95rem' },
  button: { padding: '10px 16px', backgroundColor: '#0284c7', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' },
  cancelBtn: { padding: '10px 16px', backgroundColor: '#64748b', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' },
  logoutBtn: { padding: '8px 14px', backgroundColor: '#ef4444', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' },
  editBtn: { padding: '6px 10px', backgroundColor: '#eab308', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '6px' },
  deleteBtn: { padding: '6px 10px', backgroundColor: '#ef4444', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  errorBanner: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px', borderRadius: '6px', marginBottom: '15px' },
  successBanner: { backgroundColor: '#dcfce7', color: '#166534', padding: '10px', borderRadius: '6px', marginBottom: '15px' },
  switchText: { marginTop: '12px', textAlign: 'center', fontSize: '0.9rem' },
  link: { color: '#0284c7', cursor: 'pointer', fontWeight: '600' },
  table: { width: '100%', borderCollapse: 'collapse', marginTop: '10px' },
  th: { backgroundColor: '#f1f5f9', padding: '10px', textAlign: 'left', borderBottom: '2px solid #e2e8f0', fontSize: '0.9rem' },
  td: { padding: '10px', borderBottom: '1px solid #e2e8f0', fontSize: '0.9rem' },
  badge: { backgroundColor: '#e0f2fe', color: '#0369a1', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: '600' }
};