package com.barnizexpress.application;

import java.util.List;

/** What the customer asked for: a piece, a destination and a set of option codes. */
public record QuoteCommand(
        String productId,
        String city,
        String country,
        Long declaredValueCop,
        List<String> options,
        String giftMessage) {

    public QuoteCommand {
        options = options == null ? List.of() : List.copyOf(options);
    }
}