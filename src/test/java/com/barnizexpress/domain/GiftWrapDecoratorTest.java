package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GiftWrapDecoratorTest {

    private static final Shipment BASE =
            new BaseShipment(
                    new Product("tray", "Tray", "", 185000L, 2.5, "/images/tray.jpg"),
                    "Pasto",
                    "Colombia");

    @Test
    @DisplayName("adds a flat 9000 surcharge")
    void addsFlatSurcharge() {
        Shipment shipment = new GiftWrapDecorator(BASE, "Happy birthday");

        assertEquals(9000L, shipment.layers().get(0).costCop());
        assertEquals(36000L, shipment.totalCostCop());
    }

    @Test
    @DisplayName("publishes one layer with the GIFT code and its label")
    void publishesLayer() {
        Layer layer = new GiftWrapDecorator(BASE, "Happy birthday").layers().get(0);

        assertEquals(OptionCode.GIFT, layer.code());
        assertEquals("Gift wrap", layer.label());
    }

    @DisplayName("puts the gift message in the notes")
    @Test
    void putsMessageInNotes() {
        Layer layer = new GiftWrapDecorator(BASE, "Happy birthday").layers().get(0);

        assertEquals(List.of("Wrapped with a handwritten card", "Message: Happy birthday"), layer.notes());
    }

    @Test
    @DisplayName("cannot be built without a gift message")
    void cannotBeBuiltWithoutMessage() {
        assertThrows(NullPointerException.class, () -> new GiftWrapDecorator(BASE, null));
    }

    @Test
    @DisplayName("keeps the base cost of the wrapped shipment")
    void keepsBaseCost() {
        assertEquals(27000L, new GiftWrapDecorator(BASE, "Happy birthday").baseCostCop());
    }
}