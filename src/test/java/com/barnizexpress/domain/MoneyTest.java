package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("rounds halves up")
    void roundsHalvesUp() {
        assertEquals(5L, Money.round(new BigDecimal("4.5")));
        assertEquals(7L, Money.round(new BigDecimal("6.5")));
        assertEquals(10L, Money.round(new BigDecimal("10.4")));
    }

    @Test
    @DisplayName("keeps whole amounts untouched")
    void keepsWholeAmounts() {
        assertEquals(12000L, Money.round(new BigDecimal("12000")));
        assertEquals(0L, Money.round(new BigDecimal("0")));
    }

    @Test
    @DisplayName("works on percentages of a long amount")
    void percentageOfLongAmount() {
        assertEquals(10000L, Money.percentage(500000L, new BigDecimal("0.02")));
        assertEquals(35350L, Money.percentage(101000L, new BigDecimal("0.35")));
    }
}