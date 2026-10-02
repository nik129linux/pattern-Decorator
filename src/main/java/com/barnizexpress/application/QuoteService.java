package com.barnizexpress.application;

import com.barnizexpress.domain.BaseShipment;
import com.barnizexpress.domain.CustomsDecorator;
import com.barnizexpress.domain.ExpressDecorator;
import com.barnizexpress.domain.FragilePackagingDecorator;
import com.barnizexpress.domain.GiftWrapDecorator;
import com.barnizexpress.domain.InsuranceDecorator;
import com.barnizexpress.domain.OptionCode;
import com.barnizexpress.domain.Product;
import com.barnizexpress.domain.Shipment;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Builds the decorator chain for a quote. The chain is always wrapped in the same order, from the
 * innermost decoration to the outermost one, so the price of a set of options never depends on the
 * order the client sent them.
 */
@Service
public class QuoteService {

    private static final int MAX_GIFT_MESSAGE_LENGTH = 140;

    /** Innermost to outermost. */
    private static final List<OptionCode> WRAPPING_ORDER =
            List.of(
                    OptionCode.FRAGILE,
                    OptionCode.INSURANCE,
                    OptionCode.CUSTOMS,
                    OptionCode.GIFT,
                    OptionCode.EXPRESS);

    private final ProductRepository productRepository;

    public QuoteService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @AuditedQuote
    public QuoteResult quote(QuoteCommand command) {
        Product product = findProduct(command.productId());
        validateDestination(command);
        validateDeclaredValue(command.declaredValueCop());

        Set<OptionCode> requested = parseOptions(command.options());
        validateOptionCombination(requested, command);

        long declaredValueCop =
                command.declaredValueCop() == null ? product.basePriceCop() : command.declaredValueCop();

        Shipment shipment = wrap(new BaseShipment(product, command.city(), command.country()),
                requested, declaredValueCop, command.giftMessage());

        return new QuoteResult(
                QuoteResult.CURRENCY,
                shipment.baseCostCop(),
                shipment.totalCostCop(),
                shipment.layers(),
                shipment.description());
    }

    private Shipment wrap(
            Shipment base, Set<OptionCode> requested, long declaredValueCop, String giftMessage) {
        Shipment shipment = base;
        for (OptionCode option : WRAPPING_ORDER) {
            if (!requested.contains(option)) {
                continue;
            }
            shipment =
                    switch (option) {
                        case FRAGILE -> new FragilePackagingDecorator(shipment);
                        case INSURANCE -> new InsuranceDecorator(shipment, declaredValueCop);
                        case CUSTOMS -> new CustomsDecorator(shipment);
                        case GIFT -> new GiftWrapDecorator(shipment, giftMessage.trim());
                        case EXPRESS -> new ExpressDecorator(shipment);
                    };
        }
        return shipment;
    }

    private Product findProduct(String productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new InvalidQuoteException("Unknown product id: " + productId));
    }

    private void validateDestination(QuoteCommand command) {
        if (isBlank(command.city())) {
            throw new InvalidQuoteException("Destination city is required");
        }
        if (isBlank(command.country())) {
            throw new InvalidQuoteException("Destination country is required");
        }
    }

    private void validateDeclaredValue(Long declaredValueCop) {
        if (declaredValueCop != null && declaredValueCop < 0) {
            throw new InvalidQuoteException("Declared value cannot be negative");
        }
    }

    private Set<OptionCode> parseOptions(List<String> rawOptions) {
        Set<OptionCode> options = EnumSet.noneOf(OptionCode.class);
        for (String raw : new LinkedHashSet<>(rawOptions)) {
            options.add(
                    OptionCode.parse(raw)
                            .orElseThrow(
                                    () -> new InvalidQuoteException("Unknown option code: " + raw)));
        }
        return options;
    }

    private void validateOptionCombination(Set<OptionCode> requested, QuoteCommand command) {
        for (OptionCode option : requested) {
            if (option.incompatibleWith().stream().anyMatch(requested::contains)) {
                OptionCode other =
                        option.incompatibleWith().stream()
                                .filter(requested::contains)
                                .findFirst()
                                .orElseThrow();
                throw new InvalidQuoteException(option + " and " + other + " cannot be combined");
            }
        }
        validateCustomsDestination(requested, command);
        validateGiftMessage(requested, command.giftMessage());
    }

    private void validateCustomsDestination(Set<OptionCode> requested, QuoteCommand command) {
        if (!requested.contains(OptionCode.CUSTOMS)) {
            return;
        }
        boolean domestic = new BaseShipment(
                        productRepository
                                .findById(command.productId())
                                .orElseThrow(),
                        command.city(),
                        command.country())
                .isDomestic();
        if (domestic) {
            throw new InvalidQuoteException("CUSTOMS requires an international destination");
        }
    }

    private void validateGiftMessage(Set<OptionCode> requested, String giftMessage) {
        if (!requested.contains(OptionCode.GIFT)) {
            return;
        }
        if (isBlank(giftMessage) || giftMessage.length() > MAX_GIFT_MESSAGE_LENGTH) {
            throw new InvalidQuoteException(
                    "GIFT requires a gift message of at most " + MAX_GIFT_MESSAGE_LENGTH + " characters");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}