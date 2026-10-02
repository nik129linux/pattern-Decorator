package com.barnizexpress.domain;

import java.util.List;

/**
 * What one decorator adds to a shipment: the option code, its label, what it costs and the notes
 * the customer should read about it.
 */
public record Layer(OptionCode code, String label, long costCop, List<String> notes) {

    public Layer {
        notes = List.copyOf(notes);
    }
}