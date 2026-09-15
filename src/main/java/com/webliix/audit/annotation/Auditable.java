package com.webliix.audit.annotation;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    AuditAction action();

    AuditModule module();

    String entityType() default "";

    String entityId() default "";

    String status() default "";
}
