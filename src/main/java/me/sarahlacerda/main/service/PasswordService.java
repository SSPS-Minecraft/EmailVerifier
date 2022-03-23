package me.sarahlacerda.main.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordService {

    private final MessageDigest messageDigest;

    public PasswordService() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance("SHA3-256");
    }

    public String generateHashFor(String plainTextPassword) {
        final byte[] hashBytes = messageDigest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hashBytes);
    }

    public boolean validate(String password, String hexHash) {
        return generateHashFor(password).equals(hexHash);
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
