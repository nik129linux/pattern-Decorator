package com.barnizexpress.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Template of the Decorator pattern: it holds a {@link Shipment} by composition and adds exactly
 * one {@link Layer} to it. Concrete decorators only describe their own layer, the plumbing
 * (cost roll up, layer order, description) lives here.
 */
public abstract class ShipmentDecorator implements Shipment {

    private final Shipment inner;

    protected ShipmentDecorator(Shipment inner) {
        this.inner = Objects.requireNonNull(inner, "a decorator needs a shipment to wrap");
    }

    /** The layer this decorator contributes. */
    protected abstract Layer layer();

    protected Shipment inner() {
        return inner;
    }

    /** The cost of this decorator alone, excluding the wrapped shipment. */
    protected long surchargeCop() {
        return layer().costCop();
    }

    @Override
    public long baseCostCop() {
        return inner.baseCostCop();
    }

    @Override
    public long totalCostCop() {
        return inner.totalCostCop() + surchargeCop();
    }

    @Override
    public List<Layer> layers() {
        List<Layer> layers = new ArrayList<>(inner.layers());
        layers.add(layer());
        return List.copyOf(layers);
    }

    @Override
    public String description() {
        return inner.description() + " + " + layer().label();
    }

    @Override
    public String toString() {
        return "%s[%s, totalCostCop=%d]".formatted(getClass().getSimpleName(), description(), totalCostCop());
    }
}