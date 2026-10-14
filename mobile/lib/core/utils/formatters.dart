import 'package:intl/intl.dart';

class Formatters {
  static final NumberFormat _currencyFormat = NumberFormat.currency(
    locale: 'rw_RW',
    symbol: 'RWF ',
    decimalDigits: 0,
  );

  static final NumberFormat _numberFormat = NumberFormat('#,##0', 'rw_RW');

  static String formatCurrency(double amount, {String currency = 'RWF'}) {
    if (currency == 'RWF') {
      return _currencyFormat.format(amount);
    }
    return NumberFormat.currency(
      locale: 'rw_RW',
      symbol: '$currency ',
      decimalDigits: 0,
    ).format(amount);
  }

  static String formatNumber(double number) {
    return _numberFormat.format(number);
  }

  static String formatPercentage(double value) {
    return '${value.toStringAsFixed(1)}%';
  }

  static String formatDate(DateTime date) {
    return DateFormat('MMM d, yyyy').format(date);
  }

  static String formatDateTime(DateTime dateTime) {
    return DateFormat('MMM d, yyyy HH:mm').format(dateTime);
  }

  static String formatTime(DateTime dateTime) {
    return DateFormat('HH:mm').format(dateTime);
  }

  static String formatRelativeTime(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 365) {
      return '${(difference.inDays / 365).floor()}y ago';
    } else if (difference.inDays > 30) {
      return '${(difference.inDays / 30).floor()}mo ago';
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
  }

  static String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber.length < 4) return '****';
    return '****${phoneNumber.substring(phoneNumber.length - 4)}';
  }

  static String maskEmail(String email) {
    final parts = email.split('@');
    if (parts.length != 2) return '****';
    final local = parts[0];
    if (local.length <= 2) return '**@${parts[1]}';
    return '${local[0]}***${local[local.length - 1]}@${parts[1]}';
  }

  static String formatAccountNumber(String accountNumber) {
    if (accountNumber.length < 4) return accountNumber;
    return '****${accountNumber.substring(accountNumber.length - 4)}';
  }
}