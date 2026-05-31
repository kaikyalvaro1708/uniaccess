package com.itau.identityprovisioning.domain.person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "persons")
@Entity(name = "Person")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String document;
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "zip_code")
    private String zipCode;

    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String complement;

    @Column(unique = true)
    private String login;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Person(RegisterPersonData data, String login) {
        this.fullName     = data.fullName().trim();
        this.document     = data.document().replaceAll("[^0-9]", "");
        this.email        = data.email();
        this.dateOfBirth  = data.dateOfBirth();
        this.zipCode      = data.zipCode().replaceAll("[^0-9]", "");
        this.street       = data.street();
        this.neighborhood = data.neighborhood();
        this.city         = data.city();
        this.state        = data.state();
        this.complement   = data.complement();
        this.login        = login;
        this.createdAt    = LocalDateTime.now();
    }
}
