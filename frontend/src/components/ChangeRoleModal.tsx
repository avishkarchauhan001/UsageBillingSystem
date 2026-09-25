import React, { useEffect, useState } from 'react';

interface User {
  id: number;
  username: string;
  role: string;
}

export const ChangeRoleModal: React.FC<{ onRoleUpdated: () => void }> = ({ onRoleUpdated }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newRole, setNewRole] = useState('');
  const [message, setMessage] = useState('');

  const fetchUsers = async () => {
    try {
      // FIX 1: Updated port from 8080 to 8081
      const res = await fetch('http://localhost:8081/api/admin/users?page=0&size=10');
      if (!res.ok) throw new Error('Failed to fetch users');
      const data = await res.json();
      setUsers(data.content || []);
    } catch (err) {
      console.error(err);
      setMessage('Could not load users. Ensure backend is running on port 8081.');
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleUpdate = async () => {
    if (!selectedUser || !newRole) return;

    try {
      // FIX 1: Updated port from 8080 to 8081
      const res = await fetch('http://localhost:8081/api/admin/users/role', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        // FIX 2: Send 'role' instead of 'newRole' to match UpdateRoleRequest DTO
        body: JSON.stringify({ username: selectedUser.username, role: newRole })
      });

      if (res.ok) {
        setMessage('Role updated successfully');
        setSelectedUser(null);
        fetchUsers();
        setTimeout(() => onRoleUpdated(), 1000);
      } else {
        const data = await res.json();
        setMessage(data.message || 'Failed to update role');
      }
    } catch (err) {
      setMessage('Network error while updating role');
    }
  };

  return (
    <div className="p-3 row">
      {message && <div className="alert alert-info">{message}</div>}
      <div className="col-8">
        <table className="table table-bordered">
          <thead className="table-primary">
            <tr>
              <th>USERNAME</th>
              <th>ROLE</th>
              <th>ACTION</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.role}</td>
                <td>
                  <button
                    className="btn btn-sm btn-outline-primary"
                    onClick={() => {
                      setSelectedUser(u);
                      setNewRole(u.role === 'ADMIN' ? 'OPERATOR' : 'ADMIN');
                    }}
                  >
                    ✏️ Edit
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedUser && (
        <div className="col-4 border p-3 bg-light rounded">
          <h6>Choose new Role for {selectedUser.username}</h6>
          <select className="form-select mb-3" value={newRole} onChange={(e) => setNewRole(e.target.value)}>
            <option value="ADMIN">ADMIN</option>
            <option value="OPERATOR">OPERATOR</option>
            <option value="CUSTOMER">CUSTOMER</option>
          </select>
          <button className="btn btn-success btn-sm w-100" onClick={handleUpdate}>
            CHANGE
          </button>
        </div>
      )}
    </div>
  );
};