package com.barnizexpress.domain;

import java.math.BigDecimal;
import java.util.List;

/** Covers the declared value of the piece, with a minimum premium. */
public class InsuranceDecorator extends ShipmentDecorator {

    private static final BigDecimal RATE = new BigDecimal("0.02");
    private static final long MINIMUM_COP = 5000L;
    private static final String LABEL = "Insurance";

    private final long declaredValueCop;

    public InsuranceDecorator(Shipment inner, long declaredValueCop) {
        super(inner);
        this.declaredValueCop = declaredValueCop;
    }

    public long declaredValueCop() {
        return declaredValueCop;
    }

    @Override
    protected long surchargeCop() {
        return Math.max(MINIMUM_COP, Money.percentage(declaredValueCop, RATE));
    }

    @Override
    protected Layer layer() {
        return new Layer(
                OptionCode.INSURANCE,
                LABEL,
                surchargeCop(),
                List.of("Covers the declared value of %d COP".formatted(declaredValueCop)));
    }
}