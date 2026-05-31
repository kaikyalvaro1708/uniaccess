package com.itau.identityprovisioning.domain.person.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CpfValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {
    String message() default "must be a valid CPF";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
