package me.sarahlacerda.main.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    @Test
    void shouldBeAbleToValidateGeneratedHash() {
        PasswordService passwordService = new PasswordService("SHA3-256");

        String password = "ChangeMe1234~\"\"@Hello_World";

        String passwordHash = passwordService.generateHashFor(password);

        assertTrue(passwordService.validate(password, passwordHash));
    }
}