package com.bikangu.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsByCategory {
    private String category;
    private BigDecimal amount;
}