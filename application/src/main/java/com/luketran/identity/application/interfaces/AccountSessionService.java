package com.luketran.identity.application.interfaces;

import com.luketran.identity.domain.entities.AccountSession;

import java.util.UUID;

public interface AccountSessionService {
    public AccountSession createOrExtend(UUID accountId, UUID appId );
    public AccountSession getById(UUID sessionId);
    public AccountSession extend(UUID sessionId);
}
