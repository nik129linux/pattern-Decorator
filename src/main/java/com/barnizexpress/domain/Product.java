package com.barnizexpress.domain;

/** A handcrafted Barniz de Pasto piece available for shipping. */
public record Product(
        String id,
        String name,
        String description,
        long basePriceCop,
        double weightKg,
        String imageUrl) {
}