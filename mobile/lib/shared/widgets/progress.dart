import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';

class CircularProgressIndicatorWidget extends StatelessWidget {
  final double progress;
  final double strokeWidth;
  final double size;
  final Color? progressColor;
  final Color? backgroundColor;
  final Widget? child;
  final TextStyle? progressTextStyle;

  const CircularProgressIndicatorWidget({
    super.key,
    required this.progress,
    this.strokeWidth = 8,
    this.size = 80,
    this.progressColor,
    this.backgroundColor,
    this.child,
    this.progressTextStyle,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    final clampedProgress = progress.clamp(0.0, 1.0);

    return SizedBox(
      width: size,
      height: size,
      child: Stack(
        alignment: Alignment.center,
        children: [
          SizedBox(
            width: size,
            height: size,
            child: CircularProgressIndicator(
              value: clampedProgress,
              strokeWidth: strokeWidth,
              backgroundColor: backgroundColor ?? colorScheme.surfaceContainerHighest,
              valueColor: AlwaysStoppedAnimation<Color>(
                progressColor ?? colorScheme.primary,
              ),
            ),
          ),
          if (child != null)
            child!
          else
            Text(
              '${(clampedProgress * 100).toInt()}%',
              style: progressTextStyle ?? theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: colorScheme.onSurface,
                  ),
            ),
        ],
      ),
    );
  }
}

class LinearProgressIndicatorWidget extends StatelessWidget {
  final double progress;
  final double height;
  final Color? progressColor;
  final Color? backgroundColor;
  final BorderRadius? borderRadius;
  final String? label;
  final String? value;

  const LinearProgressIndicatorWidget({
    super.key,
    required this.progress,
    this.height = 8,
    this.progressColor,
    this.backgroundColor,
    this.borderRadius,
    this.label,
    this.value,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    final clampedProgress = progress.clamp(0.0, 1.0);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (label != null || value != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                if (label != null)
                  Text(
                    label!,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                if (value != null)
                  Text(
                    value!,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurface,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
              ],
            ),
          ),
        Container(
          height: height,
          decoration: BoxDecoration(
            color: backgroundColor ?? colorScheme.surfaceContainerHighest,
            borderRadius: borderRadius ?? BorderRadius.circular(height / 2),
          ),
          child: FractionallySizedBox(
            alignment: Alignment.centerLeft,
            widthFactor: clampedProgress,
            child: Container(
              decoration: BoxDecoration(
                color: progressColor ?? colorScheme.primary,
                borderRadius: borderRadius ?? BorderRadius.circular(height / 2),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class GoalProgressCard extends StatelessWidget {
  final String title;
  final String currentAmount;
  final String targetAmount;
  final String currency;
  final DateTime? deadline;
  final String? icon;
  final Color? color;
  final VoidCallback? onTap;

  const GoalProgressCard({
    super.key,
    required this.title,
    required this.currentAmount,
    required this.targetAmount,
    this.currency = 'RWF',
    this.deadline,
    this.icon,
    this.color,
    this.onTap,
  });

  double get progress {
    final current = double.tryParse(currentAmount) ?? 0;
    final target = double.tryParse(targetAmount) ?? 0;
    if (target == 0) return 0;
    return (current / target).clamp(0.0, 1.0);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final cardColor = color != null ? Color(int.parse(color!.replaceFirst('#', '0xFF'))) : colorScheme.primary;

    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              if (icon != null) ...[
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: cardColor.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Center(
                    child: Text(icon!, style: const TextStyle(fontSize: 20)),
                  ),
                ),
                const SizedBox(width: 12),
              ],
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    if (deadline != null)
                      Text(
                        'Due ${Formatters.formatDate(deadline!)}',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: colorScheme.onSurfaceVariant,
                        ),
                      ),
                  ],
                ),
              ),
              CircularProgressIndicatorWidget(
                progress: progress,
                size: 48,
                strokeWidth: 6,
                progressColor: cardColor,
                child: Text(
                  '${(progress * 100).toInt()}%',
                  style: theme.textTheme.bodySmall?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: cardColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          LinearProgressIndicatorWidget(
            progress: progress,
            height: 10,
            progressColor: cardColor,
            borderRadius: BorderRadius.circular(5),
            value: '${Formatters.formatCurrency(double.tryParse(currentAmount) ?? 0, currency: currency)} / ${Formatters.formatCurrency(double.tryParse(targetAmount) ?? 0, currency: currency)}',
          ),
        ],
      ),
    );
  }
}

class SavingsProgressChart extends StatelessWidget {
  final List<FlSpot> spots;
  final Color? lineColor;
  final Color? gradientStart;
  final Color? gradientEnd;
  final double minX;
  final double maxX;
  final double minY;
  final double maxY;

  const SavingsProgressChart({
    super.key,
    required this.spots,
    this.lineColor,
    this.gradientStart,
    this.gradientEnd,
    this.minX = 0,
    this.maxX = 11,
    this.minY = 0,
    required this.maxY,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final lineColor = this.lineColor ?? colorScheme.primary;
    final gradientStart = this.gradientStart ?? lineColor.withValues(alpha: 0.3);
    final gradientEnd = this.gradientEnd ?? lineColor.withValues(alpha: 0.0);

    if (spots.isEmpty) {
      return SizedBox(
        height: 200,
        child: Center(
          child: Text(
            'No data available',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: colorScheme.onSurfaceVariant,
            ),
          ),
        ),
      );
    }

    return SizedBox(
      height: 200,
      child: LineChart(
        LineChartData(
          minX: minX,
          maxX: maxX,
          minY: minY,
          maxY: maxY,
          gridData: FlGridData(
            show: true,
            drawVerticalLine: false,
            horizontalInterval: maxY / 5,
            getDrawingHorizontalLine: (value) => FlLine(
              color: colorScheme.outline.withValues(alpha: 0.2),
              strokeWidth: 1,
            ),
          ),
          titlesData: FlTitlesData(
            show: true,
            rightTitles: AxisTitles(
              sideTitles: SideTitles(showTitles: false),
            ),
            topTitles: AxisTitles(
              sideTitles: SideTitles(showTitles: false),
            ),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                reservedSize: 30,
                interval: 1,
                getTitlesWidget: (value, meta) {
                  const months = [
                    'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
                  ];
                  if (value.toInt() >= 0 && value.toInt() < months.length) {
                    return Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        months[value.toInt()],
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: colorScheme.onSurfaceVariant,
                        ),
                      ),
                    );
                  }
                  return const SizedBox.shrink();
                },
              ),
            ),
            leftTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                reservedSize: 50,
                interval: maxY / 5,
                getTitlesWidget: (value, meta) {
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: Text(
                      Formatters.formatNumber(value),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                      textAlign: TextAlign.right,
                    ),
                  );
                },
              ),
            ),
          ),
          borderData: FlBorderData(show: false),
          lineBarsData: [
            LineChartBarData(
              spots: spots,
              isCurved: true,
              gradient: LinearGradient(
                colors: [lineColor, lineColor],
              ),
              barWidth: 3,
              isStrokeCapRound: true,
              dotData: FlDotData(show: false),
              belowBarData: BarAreaData(
                show: true,
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [gradientStart, gradientEnd],
                ),
              ),
            ),
          ],
          lineTouchData: LineTouchData(
            touchTooltipData: LineTouchTooltipData(
              getTooltipColor: (spot) => colorScheme.surfaceContainerHighest,
              getTooltipItems: (spots) {
                return spots.map((spot) {
                  return LineTooltipItem(
                    Formatters.formatCurrency(spot.y, currency: 'RWF'),
                    theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurface,
                      fontWeight: FontWeight.w600,
                    ) ?? const TextStyle(),
                  );
                }).toList();
              },
            ),
          ),
        ),
      ),
    );
  }
}

class PieChartWidget extends StatelessWidget {
  final List<PieChartSectionData> sections;
  final String? centerText;
  final TextStyle? centerTextStyle;
  final double? sectionsSpace;
  final double? centerSpaceRadius;

  const PieChartWidget({
    super.key,
    required this.sections,
    this.centerText,
    this.centerTextStyle,
    this.sectionsSpace,
    this.centerSpaceRadius,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return AspectRatio(
      aspectRatio: 1.3,
      child: PieChart(
        PieChartData(
          sections: sections,
          sectionsSpace: sectionsSpace ?? 2,
          centerSpaceRadius: centerSpaceRadius ?? 60,
          startDegreeOffset: -90,
          borderData: FlBorderData(show: false),
          pieTouchData: PieTouchData(
            touchCallback: (event, response) {},
          ),
        ),
      ),
    );
  }
}