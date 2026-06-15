package com.luketran.identity.application.services;

import com.luketran.identity.application.interfaces.AppService;
import com.luketran.identity.domain.entities.AccountSession;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AccountSessionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountSessionService implements com.luketran.identity.application.interfaces.AccountSessionService {
    private final AccountSessionRepository accountSessionRepository;
    private final AppService appService;

    /**
     * @param accountId
     * @param appId
     * @return
     */
    @Override
    public AccountSession createOrExtend(UUID accountId, UUID appId) {
        App app = appService.findById(appId);
        Optional<AccountSession> existing = accountSessionRepository.findByAccountIdAndAppId(accountId, appId);

        if (existing.isEmpty()) {
            AccountSession session = new AccountSession();
            session.setId(UUID.randomUUID());
            session.setAccountId(accountId);
            session.setAppId(appId);
            session.setExpiredAt(LocalDateTime.now().plusMinutes(app.getSessionLifetimeMinutes()));
            return accountSessionRepository.save(session);
        } else {
            AccountSession session = existing.get();
            session.setExpiredAt(LocalDateTime.now().plusMinutes(app.getSessionLifetimeMinutes()));
            return accountSessionRepository.save(session);
        }
    }

    /**
     * @param sessionId
     * @return
     */
    @Override
    public AccountSession getById(UUID sessionId) {
        return accountSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Account Session not found"));
    }

    /**
     * @param sessionId
     * @return
     */
    @Override
    public AccountSession extend(UUID sessionId) {
        AccountSession accountSession = this.getById(sessionId);
        App app = appService.findById(accountSession.getAppId());
        accountSession.setExpiredAt(LocalDateTime.now().plusMinutes(app.getSessionLifetimeMinutes()));
        return accountSessionRepository.save(accountSession);

    }


}
