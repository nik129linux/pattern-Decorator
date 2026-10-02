package com.barnizexpress.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The shipping options a customer can ask for, together with the catalog metadata they expose. */
public enum OptionCode {
    FRAGILE(
            "Fragile packaging",
            "Foam lining and double-wall box to protect handcrafted pieces",
            "Fixed surcharge of 18000 COP"),
    INSURANCE(
            "Insurance",
            "Covers the declared value of the piece during transport",
            "2% of the declared value, minimum 5000 COP"),
    CUSTOMS(
            "Customs clearance",
            "DIAN export declaration and commercial invoice",
            "Fixed surcharge of 60000 COP, international destinations only"),
    GIFT(
            "Gift wrap",
            "Wrapped with a handwritten card",
            "Fixed surcharge of 9000 COP, requires a gift message"),
    EXPRESS(
            "Express delivery",
            "Priority handling in transit",
            "35% of the amount accumulated by the inner layers plus the base cost");

    private static final Map<OptionCode, Set<OptionCode>> INCOMPATIBILITIES =
            Map.of(CUSTOMS, Set.of(EXPRESS), EXPRESS, Set.of(CUSTOMS));

    private final String displayName;
    private final String description;
    private final String pricingRule;

    OptionCode(String displayName, String description, String pricingRule) {
        this.displayName = displayName;
        this.description = description;
        this.pricingRule = pricingRule;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String pricingRule() {
        return pricingRule;
    }

    public Set<OptionCode> incompatibleWith() {
        return INCOMPATIBILITIES.getOrDefault(this, Set.of());
    }

    public List<String> incompatibleWithCodes() {
        return incompatibleWith().stream().map(Enum::name).sorted().toList();
    }

    /** Tells whether two options cannot be combined in the same shipment. */
    public static boolean areIncompatible(OptionCode first, OptionCode second) {
        return first.incompatibleWith().contains(second);
    }

    /** Parses an incoming option code, case insensitively. */
    public static Optional<OptionCode> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase();
        for (OptionCode code : values()) {
            if (code.name().equals(normalized)) {
                return Optional.of(code);
            }
        }
        return Optional.empty();
    }
}