package com.barnizexpress.infrastructure.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A gift message has at most 140 characters and no trailing spaces. */
@Documented
@Constraint(validatedBy = ValidGiftMessageValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGiftMessage {

    String message() default "must have at most 140 characters and no trailing spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
