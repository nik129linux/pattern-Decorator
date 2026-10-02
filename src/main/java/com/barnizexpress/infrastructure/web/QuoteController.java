package com.barnizexpress.infrastructure.web;

import com.barnizexpress.application.QuoteCommand;
import com.barnizexpress.application.QuoteResult;
import com.barnizexpress.application.QuoteService;
import com.barnizexpress.infrastructure.web.validation.SafeText;
import com.barnizexpress.infrastructure.web.validation.ValidGiftMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    public record DestinationRequest(
            @NotBlank @SafeText String city, @NotBlank @SafeText String country) {
    }

    public record QuoteRequest(
            @NotBlank @SafeText String productId,
            @NotNull @Valid DestinationRequest destination,
            Long declaredValueCop,
            List<@SafeText String> options,
            @ValidGiftMessage @SafeText String giftMessage) {
    }

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public QuoteResult quote(@Valid @RequestBody QuoteRequest request) {
        return quoteService.quote(
                new QuoteCommand(
                        request.productId(),
                        request.destination().city(),
                        request.destination().country(),
                        request.declaredValueCop(),
                        request.options(),
                        request.giftMessage()));
    }
}
