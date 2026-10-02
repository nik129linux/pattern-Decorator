package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Locks the wrapping order the catalog publishes: BaseShipment -> FRAGILE -> INSURANCE -> CUSTOMS
 * -> GIFT -> EXPRESS. EXPRESS is a percentage of everything inside it, so the order changes the
 * money.
 */
class ShipmentWrappingOrderTest {

    private static final Product TRAY =
            new Product("tray-giralda", "Tray", "", 185000L, 2.5, "/images/tray-giralda.jpg");
    private static final long DECLARED_VALUE_COP = 500000L;

    /** BaseShipment -> FRAGILE -> INSURANCE -> CUSTOMS -> GIFT -> EXPRESS */
    private static Shipment fullChain(String country) {
        Shipment shipment = new BaseShipment(TRAY, "Quito", country);
        shipment = new FragilePackagingDecorator(shipment);
        shipment = new InsuranceDecorator(shipment, DECLARED_VALUE_COP);
        shipment = new CustomsDecorator(shipment);
        shipment = new GiftWrapDecorator(shipment, "Happy birthday");
        shipment = new ExpressDecorator(shipment);
        return shipment;
    }

    @Test
    @DisplayName("layers come out in the fixed order from the innermost to the outermost")
    void layersFollowTheFixedOrder() {
        Shipment shipment = fullChain("Ecuador");

        assertEquals(
                List.of(
                        OptionCode.FRAGILE,
                        OptionCode.INSURANCE,
                        OptionCode.CUSTOMS,
                        OptionCode.GIFT,
                        OptionCode.EXPRESS),
                shipment.layers().stream().map(Layer::code).toList());
    }

    @Test
    @DisplayName("EXPRESS over base plus every inner layer costs 35% of 169000")
    void expressOverBasePlusInnerLayers() {
        // base 72000 (12000 + 6000 * 2.5 + 45000 international)
        // + fragile 18000 + insurance 10000 + customs 60000 + gift 9000 = 169000
        // express 35% of 169000 = 59150, so the total is 228150
        Shipment shipment = fullChain("Ecuador");

        assertEquals(72000L, shipment.baseCostCop());
        assertEquals(169000L, innerTotal());
        assertEquals(59150L, shipment.layers().get(4).costCop());
        assertEquals(228150L, shipment.totalCostCop());
    }

    private static long innerTotal() {
        Shipment inner = new GiftWrapDecorator(new CustomsDecorator(new InsuranceDecorator(
                new FragilePackagingDecorator(new BaseShipment(TRAY, "Quito", "Ecuador")),
                DECLARED_VALUE_COP)), "Happy birthday");
        return inner.totalCostCop();
    }

    @Test
    @DisplayName("a different wrapping order changes the total for the same options")
    void orderChangesTheTotal() {
        // Fragile -> Express -> Insurance charges 35% of 90000 (31500), while
        // Fragile -> Insurance -> Express charges 35% of 100000 (35000).
        Shipment expressOverInsurance =
                new InsuranceDecorator(new ExpressDecorator(new FragilePackagingDecorator(BASE_QUITO)),
                        DECLARED_VALUE_COP);
        Shipment insuranceOverExpress =
                new ExpressDecorator(new InsuranceDecorator(new FragilePackagingDecorator(BASE_QUITO),
                        DECLARED_VALUE_COP));

        assertEquals(131500L, expressOverInsurance.totalCostCop());
        assertEquals(135000L, insuranceOverExpress.totalCostCop());
    }

    private static final Shipment BASE_QUITO = new BaseShipment(TRAY, "Quito", "Ecuador");

    @Test
    @DisplayName("description grows with every wrapping layer")
    void descriptionGrowsWithEveryLayer() {
        assertEquals(
                "Standard shipment of Tray to Quito, Ecuador"
                        + " + Fragile packaging"
                        + " + Insurance"
                        + " + Customs clearance"
                        + " + Gift wrap"
                        + " + Express delivery",
                fullChain("Ecuador").description());
    }
}