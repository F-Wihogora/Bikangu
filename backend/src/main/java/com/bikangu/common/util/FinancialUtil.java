package com.bikangu.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialUtil {

    private FinancialUtil() {}

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static BigDecimal toBigDecimal(Number value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return ((BigDecimal) value).setScale(SCALE, ROUNDING_MODE);
        return new BigDecimal(value.toString()).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal toBigDecimal(String value) {
        if (value == null || value.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(value).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) return BigDecimal.ZERO;
        return amount.multiply(percentage).divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.add(bb).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.subtract(bb).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.multiply(bb).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.divide(bb, SCALE, ROUNDING_MODE);
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.max(bb);
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        BigDecimal aa = toBigDecimal(a);
        BigDecimal bb = toBigDecimal(b);
        return aa.min(bb);
    }

    public static boolean isPositive(BigDecimal value) {
        return toBigDecimal(value).compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isZeroOrNegative(BigDecimal value) {
        return toBigDecimal(value).compareTo(BigDecimal.ZERO) <= 0;
    }

    public static String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) return "0 " + currency;
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + currency;
    }
}