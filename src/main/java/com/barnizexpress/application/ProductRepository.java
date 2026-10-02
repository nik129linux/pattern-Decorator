package com.barnizexpress.application;

import com.barnizexpress.domain.Product;
import java.util.List;
import java.util.Optional;

/** Port to the product catalog. The demo keeps it in memory. */
public interface ProductRepository {

    Optional<Product> findById(String id);

    List<Product> findAll();
}