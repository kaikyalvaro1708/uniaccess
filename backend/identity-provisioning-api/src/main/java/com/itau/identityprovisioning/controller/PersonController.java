package com.itau.identityprovisioning.controller;

import com.itau.identityprovisioning.domain.person.PersonDetailsData;
import com.itau.identityprovisioning.domain.person.PersonService;
import com.itau.identityprovisioning.domain.person.PersonSummaryData;
import com.itau.identityprovisioning.domain.person.RegisterPersonData;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    @Autowired
    private PersonService service;

    // cadastra uma nova pessoa e retorna seus dados com o login gerado
    @PostMapping
    public ResponseEntity<PersonDetailsData> register(
            @RequestBody @Valid RegisterPersonData data,
            UriComponentsBuilder uriBuilder) {

        var person = service.register(data);
        var uri = uriBuilder.path("/api/persons/{id}").buildAndExpand(person.id()).toUri();

        return ResponseEntity.created(uri).body(person);
    }

    // lista todas as pessoas cadastradas (resumo — sem endereço completo)
    // suporta paginação: ?page=0&size=10&sort=fullName,asc
    @GetMapping
    public ResponseEntity<Page<PersonSummaryData>> list(
            @ParameterObject @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {

        return ResponseEntity.ok(service.findAll(pageable));
    }

    // retorna os detalhes completos de uma pessoa pelo id
    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsData> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // busca os detalhes de uma pessoa pelo login gerado
    @GetMapping("/login/{login}")
    public ResponseEntity<PersonDetailsData> findByLogin(@PathVariable String login) {
        return ResponseEntity.ok(service.findByLogin(login));
    }

    // remove uma pessoa pelo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
