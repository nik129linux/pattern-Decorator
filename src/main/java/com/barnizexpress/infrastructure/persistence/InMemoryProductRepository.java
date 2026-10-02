package com.barnizexpress.infrastructure.persistence;

import com.barnizexpress.application.ProductRepository;
import com.barnizexpress.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Four Barniz de Pasto pieces, kept in memory for the demo. */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final List<Product> products =
            List.of(
                    new Product(
                            "tray-giralda",
                            "Giralda Lacquered Tray",
                            "Serving tray decorated with mopa-mopa resin inlay in the Giralda pattern",
                            185000L,
                            2.5,
                            "/images/tray-giralda.jpg"),
                    new Product(
                            "bowl-mopa",
                            "Mopa-Mopa Bowl",
                            "Hand-turned wooden bowl covered in colored mopa-mopa leaf",
                            120000L,
                            1.2,
                            "/images/bowl-mopa.jpg"),
                    new Product(
                            "box-decorative",
                            "Decorative Lacquer Box",
                            "Lidded keepsake box with gold and green barniz motifs",
                            95000L,
                            0.8,
                            "/images/box-decorative.jpg"),
                    new Product(
                            "jewelry-set",
                            "Jewelry Set",
                            "Pair of earrings and a pendant made with fine mopa-mopa lacquer",
                            140000L,
                            0.3,
                            "/images/jewelry-set.jpg"));

    @Override
    public Optional<Product> findById(String id) {
        return products.stream().filter(product -> product.id().equals(id)).findFirst();
    }

    @Override
    public List<Product> findAll() {
        return products;
    }
}
