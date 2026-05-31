package com.itau.identityprovisioning.domain.person.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "must contain only letters and spaces, with at least two words";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
