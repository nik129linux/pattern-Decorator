package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpressDecoratorTest {

    private static final Shipment BASE =
            new BaseShipment(
                    new Product("tray", "Tray", "", 185000L, 2.5, "/images/tray.jpg"),
                    "Pasto",
                    "Colombia");

    @Test
    @DisplayName("charges 35% of what the wrapped shipment already costs")
    void chargesPercentageOfWrappedShipment() {
        Layer layer = new ExpressDecorator(BASE).layers().get(0);

        assertEquals(9450L, layer.costCop());
        assertEquals(36450L, new ExpressDecorator(BASE).totalCostCop());
    }

    @Test
    @DisplayName("publishes one layer with the EXPRESS code and its label")
    void publishesLayer() {
        Layer layer = new ExpressDecorator(BASE).layers().get(0);

        assertEquals(OptionCode.EXPRESS, layer.code());
        assertEquals("Express delivery", layer.label());
    }

    @Test
    @DisplayName("states the delivery window in its notes")
    void statesDeliveryWindowInNotes() {
        Layer layer = new ExpressDecorator(BASE).layers().get(0);

        assertEquals(List.of("1-2 business days"), layer.notes());
    }

    @Test
    @DisplayName("rounds the percentage half up")
    void roundsHalfUp() {
        // 27000 + 18000 = 45000, 35% of it is 15750
        Shipment shipment = new ExpressDecorator(new FragilePackagingDecorator(BASE));

        assertEquals(15750L, shipment.layers().get(1).costCop());
        assertEquals(60750L, shipment.totalCostCop());
    }

    @Test
    @DisplayName("keeps the base cost of the wrapped shipment")
    void keepsBaseCost() {
        assertEquals(27000L, new ExpressDecorator(BASE).baseCostCop());
    }
}