package com.luketran.identity.application.helpers;

import java.security.SecureRandom;

public class RandomHelper {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 15; // Length of "khWMO6wl7cv8wln" is 15

    /**
     * Generate a random alphanumeric string with the default length of 15 characters.
     *
     * @return random string
     */
    public static String generateSecretKey() {
        return generateSecretKey(DEFAULT_LENGTH);
    }

    /**
     * Generate a random alphanumeric string with a specified length.
     *
     * @param length length of the string
     * @return random string
     */
    public static String generateSecretKey(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
