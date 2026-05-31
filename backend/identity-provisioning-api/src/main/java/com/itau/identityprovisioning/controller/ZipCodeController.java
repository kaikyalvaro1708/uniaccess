package com.itau.identityprovisioning.controller;

import com.itau.identityprovisioning.domain.zipcode.ZipCodeDetails;
import com.itau.identityprovisioning.domain.zipcode.ZipCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zip-code")
public class ZipCodeController {

    @Autowired
    private ZipCodeService service;

    @GetMapping("/{zipCode}")
    public ResponseEntity<ZipCodeDetails> findByZipCode(@PathVariable String zipCode) {
        var details = service.findByZipCode(zipCode);
        return ResponseEntity.ok(details);
    }
}
