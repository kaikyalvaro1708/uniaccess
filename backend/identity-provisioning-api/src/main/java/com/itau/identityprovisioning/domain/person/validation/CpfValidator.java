package com.itau.identityprovisioning.domain.person.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;

        var digits = value.replaceAll("[^0-9]", "");

        if (digits.length() != 11) return false;
        if (digits.chars().distinct().count() == 1) return false; // e.g. 111.111.111-11

        return checkDigit(digits, 9) && checkDigit(digits, 10);
    }

    private boolean checkDigit(String digits, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += (digits.charAt(i) - '0') * (position + 1 - i);
        }
        int remainder = 11 - (sum % 11);
        int expected = remainder >= 10 ? 0 : remainder;
        return expected == (digits.charAt(position) - '0');
    }
}
