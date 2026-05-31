package com.itau.identityprovisioning.domain.zipcode;

public record ViaCepResponse(
    String cep,
    String logradouro,
    String bairro,
    String localidade,
    String uf,
    Boolean erro
) {}
