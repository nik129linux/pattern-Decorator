package com.barnizexpress.domain;

import java.util.List;

/** Wraps the piece in foam and a double-wall box. */
public class FragilePackagingDecorator extends ShipmentDecorator {

    private static final long SURCHARGE_COP = 18000L;
    private static final String LABEL = "Fragile packaging";
    private static final List<String> NOTES = List.of("Foam lining", "Double-wall box");

    public FragilePackagingDecorator(Shipment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        return new Layer(OptionCode.FRAGILE, LABEL, SURCHARGE_COP, NOTES);
    }
}