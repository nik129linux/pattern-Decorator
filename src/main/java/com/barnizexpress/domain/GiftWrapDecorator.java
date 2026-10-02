package com.barnizexpress.domain;

import java.util.List;
import java.util.Objects;

/** Wraps the piece and attaches the gift message the customer wrote. */
public class GiftWrapDecorator extends ShipmentDecorator {

    private static final long SURCHARGE_COP = 9000L;
    private static final String LABEL = "Gift wrap";

    private final String giftMessage;

    public GiftWrapDecorator(Shipment inner, String giftMessage) {
        super(inner);
        this.giftMessage = Objects.requireNonNull(giftMessage, "gift wrap needs a message");
    }

    public String giftMessage() {
        return giftMessage;
    }

    @Override
    protected Layer layer() {
        return new Layer(
                OptionCode.GIFT,
                LABEL,
                SURCHARGE_COP,
                List.of("Wrapped with a handwritten card", "Message: " + giftMessage));
    }
}