package com.barnizexpress.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** COP amounts are longs, so every intermediate decimal is rounded half up. */
public final class Money {

    private Money() {
    }

    public static long round(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static long percentage(long amount, BigDecimal rate) {
        return round(BigDecimal.valueOf(amount).multiply(rate));
    }
}