package com.barnizexpress.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barnizexpress.domain.OptionCode;
import com.barnizexpress.domain.Product;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuoteServiceTest {

    private static final Product TRAY =
            new Product("tray-giralda", "Tray", "", 185000L, 2.5, "/images/tray-giralda.jpg");

    private final QuoteService service = new QuoteService(new FakeProductRepository(TRAY));

    @Test
    @DisplayName("quotes a domestic shipment with no options")
    void quotesBareShipment() {
        QuoteResult result = service.quote(command("Pasto", "Colombia", null, List.of()));

        assertEquals(27000L, result.baseCostCop());
        assertEquals(27000L, result.totalCop());
        assertEquals(List.of(), result.layers());
        assertEquals("COP", result.currency());
    }

    @Test
    @DisplayName("adds the requested options in the fixed wrapping order regardless of request order")
    void addsOptionsInFixedOrder() {
        QuoteResult result =
                service.quote(
                        command(
                                "Pasto",
                                "Colombia",
                                null,
                                List.of("GIFT", "INSURANCE", "FRAGILE"),
                                "Happy birthday"));

        assertEquals(
                List.of(OptionCode.FRAGILE, OptionCode.INSURANCE, OptionCode.GIFT),
                result.layers().stream().map(layer -> layer.code()).toList());
        assertEquals(59000L, result.totalCop());
    }

    @Test
    @DisplayName("charges the international surcharge on the base cost")
    void chargesInternationalSurcharge() {
        QuoteResult result = service.quote(command("Quito", "Ecuador", null, List.of()));

        assertEquals(72000L, result.baseCostCop());
    }

    @Test
    @DisplayName("uses the product price as declared value when none is sent")
    void defaultsDeclaredValueToProductPrice() {
        QuoteResult result =
                service.quote(command("Pasto", "Colombia", null, List.of("INSURANCE")));

        assertEquals(List.of("Covers the declared value of 185000 COP"), result.layers().get(0).notes());
        assertEquals(32000L, result.totalCop());
    }

    @Test
    @DisplayName("insures the declared value that was sent")
    void insuresDeclaredValue() {
        QuoteResult result =
                service.quote(command("Pasto", "Colombia", 500000L, List.of("INSURANCE")));

        assertEquals(10000L, result.layers().get(0).costCop());
        assertEquals(37000L, result.totalCop());
    }

    @Test
    @DisplayName("wraps the full international chain up to gift wrap")
    void wrapsFullChainWithoutExpress() {
        QuoteResult result =
                service.quote(
                        command(
                                "Quito",
                                "Ecuador",
                                500000L,
                                List.of("FRAGILE", "INSURANCE", "CUSTOMS", "GIFT"),
                                "Feliz día"));

        assertEquals(72000L, result.baseCostCop());
        assertEquals(169000L, result.totalCop());
        assertEquals(
                List.of(OptionCode.FRAGILE, OptionCode.INSURANCE, OptionCode.CUSTOMS, OptionCode.GIFT),
                result.layers().stream().map(layer -> layer.code()).toList());
    }

    @Test
    @DisplayName("wraps express on top of every other option")
    void wrapsExpressOnTop() {
        QuoteResult result =
                service.quote(
                        command("Pasto", "Colombia", null, List.of("FRAGILE", "EXPRESS")));

        assertEquals(60750L, result.totalCop());
        assertEquals(OptionCode.EXPRESS, result.layers().get(1).code());
        assertEquals(15750L, result.layers().get(1).costCop());
    }

    @Test
    @DisplayName("rejects a blank destination city")
    void rejectsBlankCity() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("   ", "Colombia", null, List.of())));

        assertEquals("Destination city is required", error.getMessage());
    }

    @Test
    @DisplayName("rejects a blank destination country")
    void rejectsBlankCountry() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("Pasto", " ", null, List.of())));

        assertEquals("Destination country is required", error.getMessage());
    }

    @Test
    @DisplayName("rejects an unknown product id")
    void rejectsUnknownProduct() {
        QuoteCommand command =
                new QuoteCommand("nope", "Pasto", "Colombia", null, List.of(), null);

        InvalidQuoteException error =
                assertThrows(InvalidQuoteException.class, () -> service.quote(command));

        assertEquals("Unknown product id: nope", error.getMessage());
    }

    @Test
    @DisplayName("rejects a negative declared value")
    void rejectsNegativeDeclaredValue() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("Pasto", "Colombia", -1L, List.of())));

        assertEquals("Declared value cannot be negative", error.getMessage());
    }

    @Test
    @DisplayName("rejects an unknown option code")
    void rejectsUnknownOption() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("Pasto", "Colombia", null, List.of("TELEPATHY"))));

        assertEquals("Unknown option code: TELEPATHY", error.getMessage());
    }

    @Test
    @DisplayName("rejects customs clearance for a domestic destination")
    void rejectsCustomsForDomesticDestination() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("Pasto", "Colombia", null, List.of("CUSTOMS"))));

        assertEquals("CUSTOMS requires an international destination", error.getMessage());
    }

    @Test
    @DisplayName("rejects customs combined with express")
    void rejectsCustomsWithExpress() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () ->
                                service.quote(
                                        command("Quito", "Ecuador", null, List.of("CUSTOMS", "EXPRESS"))));

        assertEquals("CUSTOMS and EXPRESS cannot be combined", error.getMessage());
    }

    @Test
    @DisplayName("rejects gift wrap without a message")
    void rejectsGiftWithoutMessage() {
        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () -> service.quote(command("Pasto", "Colombia", null, List.of("GIFT"))));

        assertEquals("GIFT requires a gift message of at most 140 characters", error.getMessage());
    }

    @Test
    @DisplayName("rejects a gift message longer than 140 characters")
    void rejectsLongGiftMessage() {
        String longMessage = "a".repeat(141);

        InvalidQuoteException error =
                assertThrows(
                        InvalidQuoteException.class,
                        () ->
                                service.quote(
                                        command("Pasto", "Colombia", null, List.of("GIFT"), longMessage)));

        assertEquals("GIFT requires a gift message of at most 140 characters", error.getMessage());
    }

    @Test
    @DisplayName("ignores a gift message when gift wrap was not requested")
    void ignoresGiftMessageWhenNotRequested() {
        QuoteResult result =
                service.quote(command("Pasto", "Colombia", null, List.of(), "unused message"));

        assertEquals(27000L, result.totalCop());
    }

    @Test
    @DisplayName("accepts a gift message of exactly 140 characters")
    void acceptsMaximumGiftMessage() {
        String maxMessage = "a".repeat(140);

        QuoteResult result =
                service.quote(command("Pasto", "Colombia", null, List.of("GIFT"), maxMessage));

        assertEquals(36000L, result.totalCop());
    }

    @Test
    @DisplayName("describes the shipment the customer will read")
    void describesShipment() {
        QuoteResult result =
                service.quote(command("Pasto", "Colombia", null, List.of("FRAGILE", "EXPRESS")));

        assertTrue(result.description().endsWith("+ Express delivery"));
    }

    @Test
    @DisplayName("ignores repeated options")
    void ignoresRepeatedOptions() {
        QuoteResult result =
                service.quote(command("Pasto", "Colombia", null, List.of("FRAGILE", "fragile")));

        assertEquals(1, result.layers().size());
        assertEquals(45000L, result.totalCop());
    }

    private static QuoteCommand command(String city, String country, Long declaredValue, List<String> options) {
        return command(city, country, declaredValue, options, null);
    }

    private static QuoteCommand command(
            String city, String country, Long declaredValue, List<String> options, String giftMessage) {
        return new QuoteCommand(TRAY.id(), city, country, declaredValue, options, giftMessage);
    }

    private record FakeProductRepository(Product product) implements ProductRepository {

        @Override
        public Optional<Product> findById(String id) {
            return product.id().equals(id) ? Optional.of(product) : Optional.empty();
        }

        @Override
        public List<Product> findAll() {
            return List.of(product);
        }
    }
}
