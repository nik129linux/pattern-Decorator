package com.barnizexpress.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * The shipment without any decoration. It is the innermost object of the chain: every decorator
 * wraps it and adds exactly one {@link Layer}.
 */
public class BaseShipment implements Shipment {

    private static final BigDecimal FIXED_HANDLING_COP = new BigDecimal("12000");
    private static final BigDecimal COST_PER_KILOGRAM_COP = new BigDecimal("6000");
    private static final long INTERNATIONAL_SURCHARGE_COP = 45000L;
    private static final String HOME_COUNTRY = "colombia";

    private final Product product;
    private final String city;
    private final String country;
    private final long baseCostCop;

    public BaseShipment(Product product, String city, String country) {
        this.product = product;
        this.city = city;
        this.country = country;
        this.baseCostCop =
                Money.round(
                        FIXED_HANDLING_COP.add(
                                COST_PER_KILOGRAM_COP.multiply(BigDecimal.valueOf(product.weightKg()))))
                        + internationalSurcharge();
    }

    private long internationalSurcharge() {
        return isDomestic() ? 0L : INTERNATIONAL_SURCHARGE_COP;
    }

    public boolean isDomestic() {
        return country != null
                && country.trim().toLowerCase(Locale.ROOT).equals(HOME_COUNTRY);
    }

    @Override
    public long baseCostCop() {
        return baseCostCop;
    }

    @Override
    public long totalCostCop() {
        return baseCostCop;
    }

    @Override
    public List<Layer> layers() {
        return List.of();
    }

    @Override
    public String description() {
        return "Standard shipment of %s to %s, %s".formatted(product.name(), city, country);
    }

    @Override
    public String toString() {
        return "BaseShipment[" + description() + ", baseCostCop=" + baseCostCop + "]";
    }
}