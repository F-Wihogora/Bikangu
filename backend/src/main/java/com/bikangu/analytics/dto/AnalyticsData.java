package com.bikangu.analytics.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsData {
    private List<MonthlySavings> monthlySavings;
    private List<SavingsByCategory> savingsByCategory;
}