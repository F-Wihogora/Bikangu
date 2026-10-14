import { Users, CreditCard, Receipt, PiggyBank, TrendingUp, Target, Server, FileText, Shield } from 'lucide-react';
import { StatCard } from '../components/StatCard';
import { ChartCard } from '../components/ChartCard';
import { clsx } from 'clsx';

const stats = [
  { name: 'Total Users', value: '12,345', change: '+12%', icon: Users, color: 'primary' as const },
  { name: 'Linked Accounts', value: '8,921', change: '+8%', icon: CreditCard, color: 'secondary' as const },
  { name: 'Transactions (30d)', value: '45,678', change: '+15%', icon: Receipt, color: 'success' as const },
  { name: 'Total Savings', value: 'RWF 2.4B', change: '+23%', icon: PiggyBank, color: 'warning' as const },
];

const transactionVolumeData = [32, 45, 38, 52, 61, 55, 68, 72, 58, 65, 78, 70, 82, 88];

const providerDistributionData = [
  { name: 'MTN Mobile Money', value: 45, color: 'bg-yellow-500' },
  { name: 'Airtel Money', value: 25, color: 'bg-red-500' },
  { name: 'Bank of Kigali', value: 15, color: 'bg-blue-500' },
  { name: 'Equity Bank', value: 10, color: 'bg-green-500' },
  { name: 'Other Banks', value: 5, color: 'bg-purple-500' },
];

const recentActivityData = [
  { action: 'New user registered', user: 'John Doe', time: '2 min ago', type: 'user' },
  { action: 'Savings executed', user: 'Jane Smith', amount: 'RWF 50,000', time: '5 min ago', type: 'savings' },
  { action: 'Account linked', user: 'Bob Wilson', provider: 'MTN MoMo', time: '12 min ago', type: 'account' },
  { action: 'Goal achieved', user: 'Alice Brown', goal: 'Emergency Fund', time: '28 min ago', type: 'goal' },
  { action: 'Withdrawal requested', user: 'Charlie Davis', amount: 'RWF 200,000', time: '45 min ago', type: 'withdrawal' },
];

export function DashboardPage() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Overview of your Bikangu platform</p>
        </div>
        <div className="flex gap-2">
          <select className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-sm">
            <option>Last 7 days</option>
            <option>Last 30 days</option>
            <option>Last 90 days</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <StatCard key={stat.name} {...stat} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ChartCard
          title="Savings Growth"
          subtitle="Monthly savings volume over the last 12 months"
        >
          <div className="h-64 flex items-end justify-center gap-2">
            {[45, 52, 38, 65, 78, 82, 95, 88, 105, 118, 132, 145].map((value, index) => (
              <div key={index} className="flex-1 max-w-12">
                <div
                  className="bg-primary-500 rounded-t transition-all hover:bg-primary-600"
                  style={{ height: `${value}%` }}
                />
              </div>
            ))}
          </div>
        </ChartCard>

        <ChartCard
          title="User Growth"
          subtitle="New user registrations per month"
        >
          <div className="h-64 flex items-end justify-center gap-2">
            {[120, 145, 132, 168, 189, 201, 234, 220, 267, 289, 312, 345].map((value, index) => (
              <div key={index} className="flex-1 max-w-12">
                <div
                  className="bg-secondary-500 rounded-t transition-all hover:bg-secondary-600"
                  style={{ height: `${(value / 345) * 100}%` }}
                />
              </div>
            ))}
          </div>
        </ChartCard>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ChartCard
          title="Transaction Volume"
          subtitle="Daily transaction count and volume"
        >
          <div className="h-64 flex items-end justify-center gap-1">
            {transactionVolumeData.map((value, index) => (
              <div key={index} className="flex-1 max-w-8">
                <div
                  className="bg-green-500 rounded-t transition-all hover:bg-green-600"
                  style={{ height: `${(value / 88) * 100}%` }}
                />
              </div>
            ))}
          </div>
        </ChartCard>

        <ChartCard
          title="Provider Distribution"
          subtitle="Financial accounts by provider type"
        >
          <div className="space-y-4">
            {providerDistributionData.map((item) => (
              <div key={item.name} className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-700 dark:text-gray-300">{item.name}</span>
                  <span className="font-medium text-gray-900 dark:text-white">{item.value}%</span>
                </div>
                <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div
                    className={clsx('h-full rounded-full transition-all', item.color)}
                    style={{ width: `${item.value}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </ChartCard>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <ChartCard
          title="Recent Activity"
          subtitle="Latest platform activity"
        >
          <div className="space-y-4">
            {recentActivityData.map((activity, index) => (
              <div key={index} className="flex items-start gap-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <div className={clsx('w-8 h-8 rounded-full flex items-center justify-center', {
                  'bg-primary-100 text-primary-600': activity.type === 'user',
                  'bg-green-100 text-green-600': activity.type === 'savings',
                  'bg-blue-100 text-blue-600': activity.type === 'account',
                  'bg-yellow-100 text-yellow-600': activity.type === 'goal',
                  'bg-red-100 text-red-600': activity.type === 'withdrawal',
                })}>
                  {activity.type === 'user' && <Users className="h-4 w-4" />}
                  {activity.type === 'savings' && <PiggyBank className="h-4 w-4" />}
                  {activity.type === 'account' && <CreditCard className="h-4 w-4" />}
                  {activity.type === 'goal' && <Target className="h-4 w-4" />}
                  {activity.type === 'withdrawal' && <TrendingUp className="h-4 w-4" />}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-white">{activity.action}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    {activity.user} · {activity.time}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </ChartCard>

        <ChartCard
          title="System Health"
          subtitle="Platform status overview"
        >
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-gray-700 dark:text-gray-300">API Uptime</span>
              <span className="font-medium text-green-600">99.99%</span>
            </div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '99.99%' }} />
            </div>

            <div className="flex items-center justify-between">
              <span className="text-gray-700 dark:text-gray-300">Transaction Success Rate</span>
              <span className="font-medium text-green-600">99.2%</span>
            </div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '99.2%' }} />
            </div>

            <div className="flex items-center justify-between">
              <span className="text-gray-700 dark:text-gray-300">Provider Connectivity</span>
              <span className="font-medium text-green-600">All Connected</span>
            </div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div className="h-full bg-green-500 rounded-full" style={{ width: '100%' }} />
            </div>

            <div className="flex items-center justify-between">
              <span className="text-gray-700 dark:text-gray-300">Avg Response Time</span>
              <span className="font-medium text-gray-900 dark:text-white">145ms</span>
            </div>
            <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div className="h-full bg-primary-500 rounded-full" style={{ width: '72%' }} />
            </div>
          </div>
        </ChartCard>

        <ChartCard
          title="Quick Actions"
          subtitle="Common administrative tasks"
        >
          <div className="space-y-2">
            {[
              { label: 'Review Pending KYC', href: '/users?status=kyc-pending', icon: Users },
              { label: 'Failed Transactions', href: '/transactions?status=failed', icon: Receipt },
              { label: 'Suspended Accounts', href: '/users?status=suspended', icon: CreditCard },
              { label: 'Provider Health Check', href: '/providers', icon: Server },
              { label: 'Export Monthly Report', href: '/settings#reports', icon: FileText },
              { label: 'Security Events', href: '/security-events', icon: Shield },
            ].map((action) => (
              <a
                key={action.label}
                href={action.href}
                className="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
              >
                <action.icon className="h-5 w-5 text-gray-500" />
                <span className="text-sm font-medium text-gray-900 dark:text-white">{action.label}</span>
              </a>
            ))}
          </div>
        </ChartCard>
      </div>
    </div>
  );
}