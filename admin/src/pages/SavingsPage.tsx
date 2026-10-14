export function SavingsPage() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Savings</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Manage savings vaults and rules</p>
        </div>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Search savings..."
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-sm w-64"
          />
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
        <div className="text-center py-12">
          <div className="w-16 h-16 mx-auto mb-4 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8.5c0 1.657-1.343 3-3 3s-3-1.343-3-3 1.343-3 3-3" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 dark:text-white">No savings vaults yet</h3>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Create your first savings vault to start saving automatically</p>
        </div>
      </div>
    </div>
  );
}