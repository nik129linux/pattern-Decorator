package com.barnizexpress.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a quoting operation that must be audited. An aspect wraps the method and logs who asked
 * for what, so the audit trail stays out of the business code. It is a decorator at method level.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditedQuote {
}
