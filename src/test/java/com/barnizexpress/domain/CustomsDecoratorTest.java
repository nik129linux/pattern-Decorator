package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomsDecoratorTest {

    private static final Shipment BASE =
            new BaseShipment(
                    new Product("tray", "Tray", "", 185000L, 2.5, "/images/tray.jpg"),
                    "Quito",
                    "Ecuador");

    @Test
    @DisplayName("adds a flat 60000 surcharge")
    void addsFlatSurcharge() {
        Layer layer = new CustomsDecorator(BASE).layers().get(0);

        assertEquals(60000L, layer.costCop());
        assertEquals(132000L, new CustomsDecorator(BASE).totalCostCop());
    }

    @Test
    @DisplayName("publishes one layer with the CUSTOMS code and its label")
    void publishesLayer() {
        Layer layer = new CustomsDecorator(BASE).layers().get(0);

        assertEquals(OptionCode.CUSTOMS, layer.code());
        assertEquals("Customs clearance", layer.label());
    }

    @Test
    @DisplayName("lists the export documents in its notes")
    void listsDocumentsInNotes() {
        Layer layer = new CustomsDecorator(BASE).layers().get(0);

        assertEquals(List.of("DIAN export declaration", "Commercial invoice"), layer.notes());
    }

    @Test
    @DisplayName("keeps the base cost of the wrapped shipment")
    void keepsBaseCost() {
        assertEquals(72000L, new CustomsDecorator(BASE).baseCostCop());
    }
}