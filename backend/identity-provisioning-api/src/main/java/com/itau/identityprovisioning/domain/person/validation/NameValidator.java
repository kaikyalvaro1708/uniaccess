package com.itau.identityprovisioning.domain.person.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    private static final Pattern VALID = Pattern.compile("^\\p{L}+(\\s+\\p{L}+)+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return VALID.matcher(value.trim()).matches();
    }
}
