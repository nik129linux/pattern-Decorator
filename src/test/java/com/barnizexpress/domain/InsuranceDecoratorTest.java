package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsuranceDecoratorTest {

    private static final Shipment BASE =
            new BaseShipment(
                    new Product("tray", "Tray", "", 185000L, 2.5, "/images/tray.jpg"),
                    "Pasto",
                    "Colombia");

    @Test
    @DisplayName("charges 2% of the declared value")
    void chargesTwoPercent() {
        Layer layer = new InsuranceDecorator(BASE, 500000L).layers().get(0);

        assertEquals(10000L, layer.costCop());
    }

    @Test
    @DisplayName("never charges less than the 5000 minimum")
    void appliesMinimum() {
        Layer layer = new InsuranceDecorator(BASE, 100000L).layers().get(0);

        assertEquals(5000L, layer.costCop());
    }

    @Test
    @DisplayName("rounds the percentage half up")
    void roundsHalfUp() {
        Layer layer = new InsuranceDecorator(BASE, 250075L).layers().get(0);

        assertEquals(5002L, layer.costCop());
    }

    @Test
    @DisplayName("publishes one layer with the INSURANCE code and its label")
    void publishesLayer() {
        Layer layer = new InsuranceDecorator(BASE, 500000L).layers().get(0);

        assertEquals(OptionCode.INSURANCE, layer.code());
        assertEquals("Insurance", layer.label());
    }

    @Test
    @DisplayName("mentions the covered value in its notes")
    void mentionsCoveredValue() {
        Layer layer = new InsuranceDecorator(BASE, 500000L).layers().get(0);

        assertEquals(java.util.List.of("Covers the declared value of 500000 COP"), layer.notes());
    }

    @Test
    @DisplayName("keeps the base cost of the wrapped shipment")
    void keepsBaseCost() {
        Shipment shipment = new InsuranceDecorator(BASE, 500000L);

        assertEquals(27000L, shipment.baseCostCop());
        assertEquals(37000L, shipment.totalCostCop());
    }
}