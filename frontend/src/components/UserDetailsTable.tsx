import React, { useEffect, useState } from 'react';

interface User {
  id: number;
  username: string;
  role: string;
  userState: string;
}

export const UserDetailsTable: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchUsers = async () => {
    try {
      const res = await fetch(`http://localhost:8081/api/admin/users?page=${page}&size=10`);
      const data = await res.json();
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page]);

  return (
    <div className="p-3">
      <table className="table table-bordered border-primary text-center">
        <thead className="table-primary">
          <tr>
            <th>USERNAME</th>
            <th>ROLE</th>
            <th>STATUS</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.username}</td>
              <td>{u.role}</td>
              <td>
                <span className={`badge ${u.userState === 'Activated' ? 'bg-success' : 'bg-secondary'}`}>
                  {u.userState || 'Activated'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="d-flex justify-content-center gap-2 mt-3">
        <button
          className="btn btn-sm btn-secondary"
          disabled={page === 0}
          onClick={() => setPage(p => p - 1)}
        >
          Previous
        </button>
        <span className="align-self-center">Page {page + 1} of {totalPages || 1}</span>
        <button
          className="btn btn-sm btn-secondary"
          disabled={page >= totalPages - 1}
          onClick={() => setPage(p => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
};