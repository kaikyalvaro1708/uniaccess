package com.itau.identityprovisioning.domain.person;

import com.itau.identityprovisioning.domain.person.validation.ValidCpf;
import com.itau.identityprovisioning.domain.person.validation.ValidName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterPersonData(

    @NotBlank
    @ValidName
    String fullName,

    @NotBlank
    @ValidCpf
    String document,

    @NotBlank
    @Email
    String email,

    @NotNull
    @PastOrPresent
    LocalDate dateOfBirth,

    @NotBlank
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "must be a valid Brazilian zip code")
    String zipCode,

    @NotBlank
    String street,

    @NotBlank
    String neighborhood,

    @NotBlank
    String city,

    @NotBlank
    @Size(min = 2, max = 2)
    String state,

    String complement
) {}
