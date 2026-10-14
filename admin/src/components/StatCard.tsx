import { LucideIcon } from 'lucide-react';
import { clsx } from 'clsx';

interface StatCardProps {
  name: string;
  value: string;
  change: string;
  icon: LucideIcon;
  color: 'primary' | 'secondary' | 'success' | 'warning';
}

const colorClasses = {
  primary: 'bg-primary-100 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400',
  secondary: 'bg-secondary-100 text-secondary-600 dark:bg-secondary-900/30 dark:text-secondary-400',
  success: 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400',
  warning: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400',
};

export function StatCard({ name, value, change, icon: Icon, color }: StatCardProps) {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">{name}</p>
          <p className="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{value}</p>
        </div>
        <div className={clsx('p-3 rounded-lg', colorClasses[color])}>
          <Icon className="h-6 w-6" />
        </div>
      </div>
      <div className="mt-4 flex items-center gap-1">
        <span className="text-xs font-medium text-green-600 dark:text-green-400">{change}</span>
        <span className="text-xs text-gray-500 dark:text-gray-400">vs last period</span>
      </div>
    </div>
  );
}