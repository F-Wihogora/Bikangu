export function SecurityEventsPage() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Security Events</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Monitor security events</p>
        </div>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Search security events..."
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-sm w-64"
          />
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Timestamp</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Event Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Severity</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">User</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">IP Address</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {[
                { timestamp: '2024-01-15 10:30:00', type: 'BRUTE_FORCE_ATTEMPT', severity: 'HIGH', user: 'unknown', ip: '192.168.1.100', status: 'Active' },
                { timestamp: '2024-01-15 10:25:00', type: 'SUSPICIOUS_LOGIN', severity: 'HIGH', user: 'john@example.com', ip: '192.168.1.50', status: 'Resolved' },
                { timestamp: '2024-01-15 10:20:00', type: 'MULTIPLE_FAILED_LOGINS', severity: 'MEDIUM', user: 'jane@example.com', ip: '192.168.1.2', status: 'Active' },
                { timestamp: '2024-01-15 10:15:00', type: 'NEW_DEVICE_LOGIN', severity: 'LOW', user: 'bob@example.com', ip: '192.168.1.3', status: 'Resolved' },
              ].map((event, index) => (
                <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{event.timestamp}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300">
                      {event.type}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      event.severity === 'HIGH' 
                        ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300'
                        : event.severity === 'MEDIUM' 
                        ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300'
                        : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
                    }`}>
                      {event.severity}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{event.user}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{event.ip}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      event.status === 'Active' 
                        ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300'
                        : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
                    }`}>
                      {event.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}