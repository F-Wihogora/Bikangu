package com.bikangu.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySavings {
    private String month;
    private BigDecimal income;
    private BigDecimal savings;
    private BigDecimal withdrawals;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;
}