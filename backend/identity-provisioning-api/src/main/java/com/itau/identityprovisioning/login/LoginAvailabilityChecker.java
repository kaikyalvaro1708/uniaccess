package com.itau.identityprovisioning.login;

@FunctionalInterface
public interface LoginAvailabilityChecker {
    boolean isAvailable(String login);
}
