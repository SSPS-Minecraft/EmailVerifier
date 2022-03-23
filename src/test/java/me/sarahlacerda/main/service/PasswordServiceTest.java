package me.sarahlacerda.main.service;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    @Test
    void shouldBeAbleToValidateGeneratedHash() throws NoSuchAlgorithmException {
        PasswordService passwordService = new PasswordService();

        String password = "ChangeMe1234~\"\"@Hello_World";

        String passwordHash = passwordService.generateHashFor(password);

        assertTrue(passwordService.validate(password, passwordHash));
    }
}