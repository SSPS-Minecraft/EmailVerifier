package me.sarahlacerda.main.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordService {

    private final MessageDigest messageDigest;

    /*
     * Password requirements:
     *
     * At least 8 total characters and at most 20 total characters
     * Must have at least 1 digit
     * Must have at least 1 lowercase letter
     * Must have at least 1 uppercase letter
     *
     * */
    private final String PASSWORD_REQUIREMENTS_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,20}$";

    public PasswordService(String algorithm) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(algorithm);
    }

    public String generateHashFor(String plainTextPassword) {
        final byte[] hashBytes = messageDigest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hashBytes);
    }

    public boolean validate(String password, String hexHash) {
        return generateHashFor(password).equals(hexHash);
    }

    public boolean validateRequirements(String password) {
        return password.matches(PASSWORD_REQUIREMENTS_REGEX);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
