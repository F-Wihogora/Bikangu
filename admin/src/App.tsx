import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { UsersPage } from './pages/UsersPage';
import { AccountsPage } from './pages/AccountsPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { SavingsPage } from './pages/SavingsPage';
import { GoalsPage } from './pages/GoalsPage';
import { ProvidersPage } from './pages/ProvidersPage';
import { AuditLogsPage } from './pages/AuditLogsPage';
import { SecurityEventsPage } from './pages/SecurityEventsPage';
import { SettingsPage } from './pages/SettingsPage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-500 border-t-transparent"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-500 border-t-transparent"></div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <Routes>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/users" element={<UsersPage />} />
                <Route path="/accounts" element={<AccountsPage />} />
                <Route path="/transactions" element={<TransactionsPage />} />
                <Route path="/savings" element={<SavingsPage />} />
                <Route path="/goals" element={<GoalsPage />} />
                <Route path="/providers" element={<ProvidersPage />} />
                <Route path="/audit-logs" element={<AuditLogsPage />} />
                <Route path="/security-events" element={<SecurityEventsPage />} />
                <Route path="/settings" element={<SettingsPage />} />
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;