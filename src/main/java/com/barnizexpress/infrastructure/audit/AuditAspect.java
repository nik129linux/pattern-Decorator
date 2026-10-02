package com.barnizexpress.infrastructure.audit;

import com.barnizexpress.application.QuoteCommand;
import com.barnizexpress.application.QuoteResult;
import com.barnizexpress.infrastructure.security.JwtAuthFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Logs user, product, options and total for every method annotated with AuditedQuote. */
@Aspect
@Component
public class AuditAspect {

    private static final Logger AUDIT = LoggerFactory.getLogger("audit.quotes");

    @Around("@annotation(com.barnizexpress.application.AuditedQuote)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        QuoteCommand command = (QuoteCommand) joinPoint.getArgs()[0];
        Object result = joinPoint.proceed();
        QuoteResult quote = (QuoteResult) result;
        AUDIT.info(
                "user={} product={} options={} total={} {}",
                currentUser(),
                command.productId(),
                command.options(),
                quote.totalCop(),
                quote.currency());
        return result;
    }

    private String currentUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "anonymous";
        }
        Object user =
                attributes.getAttribute(JwtAuthFilter.USER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return user == null ? "anonymous" : user.toString();
    }
}
