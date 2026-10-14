import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:bikangu/config/app_config.dart';
import 'package:bikangu/routing/app_router.dart';
import 'package:bikangu/theme/app_theme.dart';
import 'package:bikangu/core/constants/app_constants.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Firebase.initializeApp(
    options: AppConfig.firebaseOptions,
  );

  runApp(
    const ProviderScope(
      child: BikanguApp(),
    ),
  );
}

class BikanguApp extends ConsumerWidget {
  const BikanguApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);
    final themeMode = ref.watch(themeModeProvider);

    return MaterialApp.router(
      title: AppConstants.appName,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: themeMode,
      routerConfig: router,
      builder: (context, child) {
        return MediaQuery(
          data: MediaQuery.of(context).copyWith(
            textScaler: TextScaler.linear(
              MediaQuery.of(context).textScaler.clamp(minScaleFactor: 0.8, maxScaleFactor: 1.2),
            ),
          ),
          child: child!,
        );
      },
    );
  }
}