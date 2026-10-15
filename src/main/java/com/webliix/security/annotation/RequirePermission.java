package com.webliix.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequirePermission {
    /**
     * Primary permission code required, e.g. "EXPENSES_APPROVE".
     */
    String value() default "";

    /**
     * Alternative list of permission codes (caller holds any of these).
     */
    String[] anyOf() default {};
}
