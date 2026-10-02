package com.barnizexpress.domain;

import java.util.List;

/** Handles the export paperwork needed to cross a border. */
public class CustomsDecorator extends ShipmentDecorator {

    private static final long SURCHARGE_COP = 60000L;
    private static final String LABEL = "Customs clearance";
    private static final List<String> NOTES = List.of("DIAN export declaration", "Commercial invoice");

    public CustomsDecorator(Shipment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        return new Layer(OptionCode.CUSTOMS, LABEL, SURCHARGE_COP, NOTES);
    }
}