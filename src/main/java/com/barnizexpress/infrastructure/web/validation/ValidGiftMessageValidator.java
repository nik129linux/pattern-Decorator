package com.barnizexpress.infrastructure.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidGiftMessageValidator implements ConstraintValidator<ValidGiftMessage, String> {

    private static final int MAX_LENGTH = 140;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || (value.length() <= MAX_LENGTH && value.equals(value.stripTrailing()));
    }
}
