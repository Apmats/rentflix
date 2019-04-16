package com.apmats.rentflix.util;

import com.google.common.base.Preconditions;

public enum RecencyType {

    RECENT(1, Price.PREMIUM.getPrice(), Price.PREMIUM.getPrice(), 2),
    REGULAR(3, Price.BASIC.getPrice(), Price.BASIC.getPrice(), 1),
    OLD(5, Price.BASIC.getPrice(), Price.BASIC.getPrice(), 1);

    private final int initialPeriodInDays;
    private final Double initialCharge;
    private final Double additionaDailyCharge;
    private final int bonus;

    RecencyType(int initialPeriodInDays, Double initialCharge, Double additionalDailyCharge, int bonus) {
        this.initialPeriodInDays = initialPeriodInDays;
        this.initialCharge = initialCharge;
        this.additionaDailyCharge = additionalDailyCharge;
        this.bonus = bonus;
    }

    // Calculates surcharge, not including initial cost, based on initial rent
    // period and total days rented
    public Double calculateSurcharge(long totalDaysRented) {
        Preconditions.checkArgument(totalDaysRented >= 0, "Total days rented should be 0 or more");
        long daysLate = Math.max(0L, totalDaysRented - initialPeriodInDays);
        return daysLate * additionaDailyCharge;
    }

    public int getBonus() {
        return bonus;
    }

    public Double getInitialCharge() {
        return initialCharge;
    }

    public int getInitialPeriodInDays() {
        return initialPeriodInDays;
    }
}