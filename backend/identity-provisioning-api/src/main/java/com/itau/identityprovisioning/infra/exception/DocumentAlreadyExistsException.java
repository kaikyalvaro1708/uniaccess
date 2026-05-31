package com.itau.identityprovisioning.infra.exception;

public class DocumentAlreadyExistsException extends RuntimeException {
    public DocumentAlreadyExistsException() {
        super("Document already registered");
    }
}
