package com.itau.identityprovisioning.domain.zipcode;

import com.itau.identityprovisioning.infra.exception.ZipCodeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ZipCodeService {

    @Autowired
    private RestClient viaCepClient;

    public ZipCodeDetails findByZipCode(String zipCode) {
        var clean = zipCode.replaceAll("[^0-9]", "");

        var response = viaCepClient.get()
                .uri("/{zipCode}/json/", clean)
                .retrieve()
                .body(ViaCepResponse.class);

        if (response == null || Boolean.TRUE.equals(response.erro())) {
            throw new ZipCodeNotFoundException(zipCode);
        }

        return new ZipCodeDetails(
                response.cep(),
                response.logradouro(),
                response.bairro(),
                response.localidade(),
                response.uf()
        );
    }
}
