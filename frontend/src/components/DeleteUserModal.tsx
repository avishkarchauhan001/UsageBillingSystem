import React, { useEffect, useState } from 'react';

interface User {
  id: number;
  username: string;
  role: string;
}

export const DeleteUserModal: React.FC<{ onDeleted: () => void }> = ({ onDeleted }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [msg, setMsg] = useState('');

  const fetchUsers = async () => {
    const res = await fetch('http://localhost:8081/api/admin/users?page=0&size=10');
    const data = await res.json();
    setUsers(data.content || []);
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const confirmDelete = async () => {
    if (!selectedUser) return;

    const res = await fetch(`http://localhost:8081/api/admin/users/${selectedUser.id}`, {
      method: 'DELETE'
    });

    const data = await res.json();
    setMsg(data.message);
    setSelectedUser(null);
    fetchUsers();
    setTimeout(() => onDeleted(), 1000);
  };

  return (
    <div className="p-3">
      {msg && <div className="alert alert-success">{msg}</div>}
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
                <button className="btn btn-sm btn-danger" onClick={() => setSelectedUser(u)}>
                  DELETE
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedUser && (
        <div className="modal d-block bg-dark bg-opacity-50">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-body text-center">
                <h5>ARE YOU SURE TO DELETE {selectedUser.username}?</h5>
                <div className="d-flex justify-content-center gap-3 mt-4">
                  <button className="btn btn-primary px-4" onClick={confirmDelete}>CONFIRM</button>
                  <button className="btn btn-secondary px-4" onClick={() => setSelectedUser(null)}>CANCEL</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};