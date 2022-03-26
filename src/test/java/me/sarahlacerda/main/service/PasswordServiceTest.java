package me.sarahlacerda.main.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    PasswordService passwordService;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException {
        passwordService = new PasswordService("SHA3-256");
    }

    @Test
    void shouldBeAbleToValidateGeneratedHash() {
        String password = "ChangeMe1234~\"\"@Hello_World";

        String passwordHash = passwordService.generateHashFor(password);

        assertTrue(passwordService.validate(password, passwordHash));
    }

    @Test
    void shouldMatchPasswordRequirements() {
        List<String> validPasswords = List.of("ChangeMe1", "123456Ab", "12345678910@Ba", "@!1234567890ABcdEFgh");
        List<String> invalidPasswords = List.of("ChangeMe", "12345678", "changeme", "CHANGEME", "12345678!@#$", "1234567890ABcdefghijk", "!@#$%^&*");

        validPasswords
                .stream()
                .map(validPassword -> passwordService.validateRequirements(validPassword))
                .forEach(Assertions::assertTrue);

        invalidPasswords
                .stream()
                .map(invalidPassword -> passwordService.validateRequirements(invalidPassword))
                .forEach(Assertions::assertFalse);
    }
}