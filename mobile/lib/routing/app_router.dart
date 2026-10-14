import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:bikangu/features/authentication/presentation/screens/splash_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/onboarding_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/login_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/register_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/verify_email_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/verify_phone_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/create_pin_screen.dart';
import 'package:bikangu/features/authentication/presentation/screens/biometrics_screen.dart';
import 'package:bikangu/features/dashboard/presentation/screens/dashboard_screen.dart';
import 'package:bikangu/features/accounts/presentation/screens/accounts_screen.dart';
import 'package:bikangu/features/accounts/presentation/screens/link_account_screen.dart';
import 'package:bikangu/features/savings/presentation/screens/savings_screen.dart';
import 'package:bikangu/features/savings/presentation/screens/create_vault_screen.dart';
import 'package:bikangu/features/savings/presentation/screens/create_rule_screen.dart';
import 'package:bikangu/features/savings/presentation/screens/vault_detail_screen.dart';
import 'package:bikangu/features/goals/presentation/screens/goals_screen.dart';
import 'package:bikangu/features/goals/presentation/screens/create_goal_screen.dart';
import 'package:bikangu/features/goals/presentation/screens/goal_detail_screen.dart';
import 'package:bikangu/features/transactions/presentation/screens/transactions_screen.dart';
import 'package:bikangu/features/transactions/presentation/screens/transaction_detail_screen.dart';
import 'package:bikangu/features/analytics/presentation/screens/analytics_screen.dart';
import 'package:bikangu/features/notifications/presentation/screens/notifications_screen.dart';
import 'package:bikangu/features/profile/presentation/screens/profile_screen.dart';
import 'package:bikangu/features/profile/presentation/screens/security_screen.dart';
import 'package:bikangu/features/profile/presentation/screens/notification_preferences_screen.dart';
import 'package:bikangu/core/providers/auth_provider.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authProvider);

  return GoRouter(
    initialLocation: '/splash',
    redirect: (context, state) {
      final isAuthenticated = authState.isAuthenticated;
      final isOnboardingComplete = authState.isOnboardingComplete;

      final isAuthRoute = state.matchedLocation.startsWith('/auth') ||
          state.matchedLocation == '/onboarding' ||
          state.matchedLocation == '/splash';

      if (!isAuthenticated && !isAuthRoute) {
        return '/auth/login';
      }

      if (isAuthenticated && !isOnboardingComplete && state.matchedLocation != '/onboarding') {
        return '/onboarding';
      }

      if (isAuthenticated && isOnboardingComplete && (state.matchedLocation == '/auth/login' || state.matchedLocation == '/auth/register')) {
        return '/dashboard';
      }

      return null;
    },
    routes: [
      // Splash & Onboarding
      GoRoute(
        path: '/splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        builder: (context, state) => const OnboardingScreen(),
      ),

      // Auth routes
      GoRoute(
        path: '/auth/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/auth/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/auth/verify-email',
        builder: (context, state) => const VerifyEmailScreen(),
      ),
      GoRoute(
        path: '/auth/verify-phone',
        builder: (context, state) => const VerifyPhoneScreen(),
      ),
      GoRoute(
        path: '/auth/create-pin',
        builder: (context, state) => const CreatePinScreen(),
      ),
      GoRoute(
        path: '/auth/biometrics',
        builder: (context, state) => const BiometricsScreen(),
      ),

      // Main app with bottom navigation
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return ScaffoldWithBottomNav(navigationShell: navigationShell);
        },
        branches: [
          // Dashboard
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/dashboard',
                builder: (context, state) => const DashboardScreen(),
              ),
            ],
          ),

          // Savings
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/savings',
                builder: (context, state) => const SavingsScreen(),
              ),
              GoRoute(
                path: '/savings/vault/create',
                builder: (context, state) => const CreateVaultScreen(),
              ),
              GoRoute(
                path: '/savings/vault/:vaultId',
                builder: (context, state) => VaultDetailScreen(
                  vaultId: state.pathParameters['vaultId']!,
                ),
              ),
              GoRoute(
                path: '/savings/rule/create',
                builder: (context, state) => const CreateRuleScreen(),
              ),
            ],
          ),

          // Transactions
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/transactions',
                builder: (context, state) => const TransactionsScreen(),
              ),
              GoRoute(
                path: '/transactions/:transactionId',
                builder: (context, state) => TransactionDetailScreen(
                  transactionId: state.pathParameters['transactionId']!,
                ),
              ),
            ],
          ),

          // Analytics
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/analytics',
                builder: (context, state) => const AnalyticsScreen(),
              ),
            ],
          ),

          // Profile
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/profile',
                builder: (context, state) => const ProfileScreen(),
              ),
              GoRoute(
                path: '/profile/security',
                builder: (context, state) => const SecurityScreen(),
              ),
              GoRoute(
                path: '/profile/notifications',
                builder: (context, state) => const NotificationPreferencesScreen(),
              ),
            ],
          ),
        ],
      ),

      // Accounts (modal/full screen)
      GoRoute(
        path: '/accounts',
        builder: (context, state) => const AccountsScreen(),
      ),
      GoRoute(
        path: '/accounts/link',
        builder: (context, state) => const LinkAccountScreen(),
      ),

      // Goals (modal/full screen)
      GoRoute(
        path: '/goals',
        builder: (context, state) => const GoalsScreen(),
      ),
      GoRoute(
        path: '/goals/create',
        builder: (context, state) => const CreateGoalScreen(),
      ),
      GoRoute(
        path: '/goals/:goalId',
        builder: (context, state) => GoalDetailScreen(
          goalId: state.pathParameters['goalId']!,
        ),
      ),

      // Notifications
      GoRoute(
        path: '/notifications',
        builder: (context, state) => const NotificationsScreen(),
      ),
    ],
  );
});

class ScaffoldWithBottomNav extends StatelessWidget {
  final StatefulNavigationShell navigationShell;

  const ScaffoldWithBottomNav({
    super.key,
    required this.navigationShell,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: navigationShell.currentIndex,
        onDestinationSelected: (index) {
          navigationShell.goBranch(index);
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.dashboard_outlined),
            selectedIcon: Icon(Icons.dashboard),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.savings_outlined),
            selectedIcon: Icon(Icons.savings),
            label: 'Savings',
          ),
          NavigationDestination(
            icon: Icon(Icons.receipt_long_outlined),
            selectedIcon: Icon(Icons.receipt_long),
            label: 'Transactions',
          ),
          NavigationDestination(
            icon: Icon(Icons.analytics_outlined),
            selectedIcon: Icon(Icons.analytics),
            label: 'Analytics',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}