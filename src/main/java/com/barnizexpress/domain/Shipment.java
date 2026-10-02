package com.barnizexpress.domain;

import java.util.List;

/** A shipment that can be quoted: the base cost plus the decorations applied to it. */
public interface Shipment {

    /** Cost of shipping the piece itself, before any decoration. */
    long baseCostCop();

    /** Base cost plus every decoration cost, in wrapping order. */
    long totalCostCop();

    /** One layer per decorator, from the innermost decoration to the outermost one. */
    List<Layer> layers();

    String description();
}