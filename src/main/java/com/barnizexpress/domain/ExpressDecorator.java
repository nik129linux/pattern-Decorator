package com.barnizexpress.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Priority handling. Its cost is a percentage of everything the inner layers plus the base already
 * cost, so this is the decorator that makes the wrapping order observable.
 */
public class ExpressDecorator extends ShipmentDecorator {

    private static final BigDecimal RATE = new BigDecimal("0.35");
    private static final String LABEL = "Express delivery";
    private static final List<String> NOTES = List.of("1-2 business days");

    public ExpressDecorator(Shipment inner) {
        super(inner);
    }

    @Override
    protected long surchargeCop() {
        return Money.percentage(inner().totalCostCop(), RATE);
    }

    @Override
    protected Layer layer() {
        return new Layer(OptionCode.EXPRESS, LABEL, surchargeCop(), NOTES);
    }
}