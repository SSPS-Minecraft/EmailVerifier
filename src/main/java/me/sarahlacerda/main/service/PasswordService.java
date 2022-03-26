package me.sarahlacerda.main.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.text.MessageFormat.format;
import static me.sarahlacerda.main.util.Logger.getLogger;

public class PasswordService {

    private final MessageDigest messageDigest;

    public PasswordService(String algorithm) {
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            getLogger().info(format("Using {0} encryption for player passwords", algorithm));
        } catch (NoSuchAlgorithmException e) {
            getLogger().warn(format("Chosen Password Algorithm \"{0}\" is not valid!", algorithm));
            throw new RuntimeException(e);
        }
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
