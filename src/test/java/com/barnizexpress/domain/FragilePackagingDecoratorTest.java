package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FragilePackagingDecoratorTest {

    private static final Shipment BASE =
            new BaseShipment(
                    new Product("tray", "Tray", "", 185000L, 2.5, "/images/tray.jpg"),
                    "Pasto",
                    "Colombia");

    @Test
    @DisplayName("adds a flat 18000 surcharge")
    void addsFlatSurcharge() {
        Shipment shipment = new FragilePackagingDecorator(BASE);

        assertEquals(18000L, shipment.layers().get(0).costCop());
        assertEquals(45000L, shipment.totalCostCop());
    }

    @Test
    @DisplayName("publishes one layer with the FRAGILE code and its label")
    void publishesLayer() {
        Layer layer = new FragilePackagingDecorator(BASE).layers().get(0);

        assertEquals(OptionCode.FRAGILE, layer.code());
        assertEquals("Fragile packaging", layer.label());
    }

    @Test
    @DisplayName("explains the packaging in its notes")
    void explainsPackagingInNotes() {
        Layer layer = new FragilePackagingDecorator(BASE).layers().get(0);

        assertEquals(java.util.List.of("Foam lining", "Double-wall box"), layer.notes());
    }

    @Test
    @DisplayName("keeps the base cost of the wrapped shipment and appends its layer last")
    void appendsLayerAfterInnerOnes() {
        Shipment inner = new FragilePackagingDecorator(BASE);
        Shipment shipment = new FragilePackagingDecorator(inner);

        assertEquals(27000L, shipment.baseCostCop());
        assertEquals(
                java.util.List.of(OptionCode.FRAGILE, OptionCode.FRAGILE),
                shipment.layers().stream().map(Layer::code).toList());
    }

    @Test
    @DisplayName("describes the wrapped shipment plus the packaging")
    void describesWrappedShipment() {
        assertEquals(
                "Standard shipment of Tray to Pasto, Colombia + Fragile packaging",
                new FragilePackagingDecorator(BASE).description());
    }

    @Test
    @DisplayName("cannot wrap a missing shipment")
    void cannotWrapNull() {
        assertThrows(NullPointerException.class, () -> new FragilePackagingDecorator(null));
    }
}