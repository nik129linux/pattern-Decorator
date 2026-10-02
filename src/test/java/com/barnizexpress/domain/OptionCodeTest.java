package com.barnizexpress.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionCodeTest {

    @Test
    @DisplayName("exposes the five shipping options")
    void exposesFiveOptions() {
        assertEquals(
                Set.of(
                        OptionCode.FRAGILE,
                        OptionCode.INSURANCE,
                        OptionCode.CUSTOMS,
                        OptionCode.GIFT,
                        OptionCode.EXPRESS),
                Set.of(OptionCode.values()));
    }

    @Test
    @DisplayName("every option carries name, description, pricing rule and incompatibility")
    void everyOptionIsDescribed() {
        for (OptionCode option : OptionCode.values()) {
            assertTrue(!option.name().isBlank(), option + " needs a name");
            assertTrue(!option.description().isBlank(), option + " needs a description");
            assertTrue(!option.pricingRule().isBlank(), option + " needs a pricing rule");
            assertTrue(option.incompatibleWith() != null, option + " needs incompatibleWith");
        }
    }

    @Test
    @DisplayName("only customs and express are incompatible")
    void onlyCustomsAndExpressConflict() {
        assertEquals(Set.of(OptionCode.EXPRESS), Set.copyOf(OptionCode.CUSTOMS.incompatibleWith()));
        assertEquals(Set.of(OptionCode.CUSTOMS), Set.copyOf(OptionCode.EXPRESS.incompatibleWith()));
        assertEquals(Set.of(), Set.copyOf(OptionCode.FRAGILE.incompatibleWith()));
        assertEquals(Set.of(), Set.copyOf(OptionCode.INSURANCE.incompatibleWith()));
        assertEquals(Set.of(), Set.copyOf(OptionCode.GIFT.incompatibleWith()));
    }

    @Test
    @DisplayName("is case insensitive when parsing an incoming code")
    void parsesCaseInsensitively() {
        assertEquals(Optional.of(OptionCode.FRAGILE), OptionCode.parse("fragile"));
        assertEquals(Optional.of(OptionCode.EXPRESS), OptionCode.parse("EXPRESS"));
        assertEquals(Optional.empty(), OptionCode.parse("TELEPATHY"));
    }
}