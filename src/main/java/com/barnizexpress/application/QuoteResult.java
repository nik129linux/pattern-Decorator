package com.barnizexpress.application;

import com.barnizexpress.domain.Layer;
import java.util.List;

/** The quote the API returns: money in COP plus one layer per decorator. */
public record QuoteResult(
        String currency, long baseCostCop, long totalCop, List<Layer> layers, String description) {

    public static final String CURRENCY = "COP";

    public QuoteResult {
        layers = List.copyOf(layers);
    }
}