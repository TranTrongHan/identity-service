package com.luketran.identity.domain.exceptions;

import java.time.LocalDateTime;

public class BruteForceException extends RuntimeException {

    private final LocalDateTime lockedUntil;

    public BruteForceException(LocalDateTime lockedUntil) {
        super("Account locked due to too many failed login attempts. Try again after: " + lockedUntil);
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
