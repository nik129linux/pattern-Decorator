package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseShipmentTest {

    private static final Product TRAY = new Product(
            "tray-giralda",
            "Mopa-mopa lacquered tray",
            "Handcrafted tray finished with mopa-mopa lacquer",
            185000L,
            2.5,
            "/images/tray-giralda.jpg");

    @Test
    @DisplayName("charges 12000 plus 6000 per kilogram for a domestic shipment")
    void chargesDomesticBase() {
        Shipment shipment = new BaseShipment(TRAY, "Pasto", "Colombia");

        assertEquals(27000L, shipment.baseCostCop());
    }

    @Test
    @DisplayName("adds 45000 when the destination is not Colombia")
    void addsInternationalSurcharge() {
        Shipment shipment = new BaseShipment(TRAY, "Quito", "Ecuador");

        assertEquals(72000L, shipment.baseCostCop());
    }

    @Test
    @DisplayName("compares the country ignoring case and surrounding spaces")
    void comparesCountryIgnoringCase() {
        Shipment shipment = new BaseShipment(TRAY, "Medellin", " colombia ");

        assertEquals(27000L, shipment.baseCostCop());
    }

    @Test
    @DisplayName("is not a layer, so it exposes no layers and no extra cost")
    void hasNoLayers() {
        Shipment shipment = new BaseShipment(TRAY, "Pasto", "Colombia");

        assertEquals(List.of(), shipment.layers());
        assertEquals(shipment.baseCostCop(), shipment.totalCostCop());
    }

    @Test
    @DisplayName("describes the product and the destination")
    void describesShipment() {
        Shipment shipment = new BaseShipment(TRAY, "Pasto", "Colombia");

        assertEquals(
                "Standard shipment of Mopa-mopa lacquered tray to Pasto, Colombia",
                shipment.description());
    }

    @Test
    @DisplayName("rounds the weight based base cost half up")
    void roundsHalfUp() {
        Product odd = new Product("odd", "Odd piece", "", 10000L, 1.75, "/images/odd.jpg");

        Shipment shipment = new BaseShipment(odd, "Pasto", "Colombia");

        assertEquals(22500L, shipment.baseCostCop());
    }

    @Test
    @DisplayName("treats a null destination city as unknown and keeps the shipment describable")
    void handlesBlankCity() {
        assertTrue(new BaseShipment(TRAY, "  ", "Colombia").description().contains("Colombia"));
    }
}