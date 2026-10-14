import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart';
import 'package:shimmer/shimmer.dart';

class LoadingOverlay extends StatelessWidget {
  final bool isLoading;
  final Widget child;
  final String? message;
  final Color? color;

  const LoadingOverlay({
    super.key,
    required this.isLoading,
    required this.child,
    this.message,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Stack(
      children: [
        child,
        if (isLoading)
          Container(
            color: Colors.black.withValues(alpha: 0.3),
            child: Center(
              child: Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: colorScheme.surface,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    SizedBox(
                      width: 48,
                      height: 48,
                      child: CircularProgressIndicator(
                        strokeWidth: 3,
                        color: color ?? colorScheme.primary,
                      ),
                    ),
                    if (message != null) ...[
                      const SizedBox(height: 16),
                      Text(
                        message!,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: colorScheme.onSurface,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class LoadingShimmer extends StatelessWidget {
  final Widget child;
  final bool isLoading;
  final Color? baseColor;
  final Color? highlightColor;

  const LoadingShimmer({
    super.key,
    required this.child,
    required this.isLoading,
    this.baseColor,
    this.highlightColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    if (!isLoading) return child;

    return Shimmer.fromColors(
      baseColor: baseColor ?? colorScheme.surfaceContainerHighest,
      highlightColor: highlightColor ?? colorScheme.surfaceContainerLowest,
      child: child,
    );
  }
}

class SkeletonCard extends StatelessWidget {
  final double? height;
  final double? width;
  final BorderRadius? borderRadius;

  const SkeletonCard({
    super.key,
    this.height,
    this.width,
    this.borderRadius,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      height: height,
      width: width,
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerHighest,
        borderRadius: borderRadius ?? BorderRadius.circular(16),
      ),
    );
  }
}

class SkeletonList extends StatelessWidget {
  final int itemCount;
  final double itemHeight;
  final EdgeInsetsGeometry? padding;

  const SkeletonList({
    super.key,
    this.itemCount = 5,
    this.itemHeight = 80,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      padding: padding,
      itemCount: itemCount,
      itemBuilder: (context, index) {
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
          child: SkeletonCard(
            height: itemHeight,
            borderRadius: BorderRadius.circular(12),
          ),
        );
      },
    );
  }
}

class EmptyState extends StatelessWidget {
  final String title;
  final String? message;
  final IconData? icon;
  final Widget? action;
  final String? assetPath;

  const EmptyState({
    super.key,
    required this.title,
    this.message,
    this.icon,
    this.action,
    this.assetPath,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (assetPath != null) ...[
              Lottie.asset(
                assetPath!,
                width: 200,
                height: 200,
                fit: BoxFit.contain,
              ),
            ] else if (icon != null) ...[
              Container(
                width: 120,
                height: 120,
                decoration: BoxDecoration(
                  color: colorScheme.primaryContainer.withValues(alpha: 0.5),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  icon,
                  size: 60,
                  color: colorScheme.primary,
                ),
              ),
            ] else ...[
              Container(
                width: 120,
                height: 120,
                decoration: BoxDecoration(
                  color: colorScheme.primaryContainer.withValues(alpha: 0.5),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  Icons.inbox_outlined,
                  size: 60,
                  color: colorScheme.primary,
                ),
              ),
            ],
            const SizedBox(height: 24),
            Text(
              title,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.w600,
                color: colorScheme.onSurface,
              ),
              textAlign: TextAlign.center,
            ),
            if (message != null) ...[
              const SizedBox(height: 12),
              Text(
                message!,
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (action != null) ...[
              const SizedBox(height: 24),
              action!,
            ],
          ],
        ),
      ),
    );
  }
}

class ErrorState extends StatelessWidget {
  final String title;
  final String? message;
  final VoidCallback? onRetry;
  final String? retryLabel;
  final IconData? icon;

  const ErrorState({
    super.key,
    required this.title,
    this.message,
    this.onRetry,
    this.retryLabel = 'Try Again',
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                color: colorScheme.errorContainer.withValues(alpha: 0.5),
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon ?? Icons.error_outline,
                size: 60,
                color: colorScheme.error,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              title,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.w600,
                color: colorScheme.onSurface,
              ),
              textAlign: TextAlign.center,
            ),
            if (message != null) ...[
              const SizedBox(height: 12),
              Text(
                message!,
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (onRetry != null) ...[
              const SizedBox(height: 24),
              PrimaryButton(
                label: retryLabel!,
                onPressed: onRetry,
                icon: Icons.refresh,
                isFullWidth: false,
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class SuccessState extends StatelessWidget {
  final String title;
  final String? message;
  final VoidCallback? onContinue;
  final String? continueLabel;
  final IconData? icon;

  const SuccessState({
    super.key,
    required this.title,
    this.message,
    this.onContinue,
    this.continueLabel = 'Continue',
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                color: colorScheme.primaryContainer.withValues(alpha: 0.5),
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon ?? Icons.check_circle_outline,
                size: 60,
                color: colorScheme.primary,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              title,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.w600,
                color: colorScheme.onSurface,
              ),
              textAlign: TextAlign.center,
            ),
            if (message != null) ...[
              const SizedBox(height: 12),
              Text(
                message!,
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (onContinue != null) ...[
              const SizedBox(height: 24),
              PrimaryButton(
                label: continueLabel!,
                onPressed: onContinue,
                isFullWidth: false,
              ),
            ],
          ],
        ),
      ),
    );
  }
}