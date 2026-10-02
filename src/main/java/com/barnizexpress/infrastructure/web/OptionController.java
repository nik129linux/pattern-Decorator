package com.barnizexpress.infrastructure.web;

import com.barnizexpress.domain.OptionCode;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/options")
public class OptionController {

    public record OptionView(
            String code,
            String name,
            String description,
            String pricingRule,
            List<String> incompatibleWith) {
    }

    @GetMapping
    public List<OptionView> list() {
        return Arrays.stream(OptionCode.values())
                .map(
                        option ->
                                new OptionView(
                                        option.name(),
                                        option.displayName(),
                                        option.description(),
                                        option.pricingRule(),
                                        option.incompatibleWithCodes()))
                .toList();
    }
}
