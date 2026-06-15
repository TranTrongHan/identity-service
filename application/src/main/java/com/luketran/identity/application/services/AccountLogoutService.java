package com.luketran.identity.application.services;

import com.luketran.identity.domain.entities.AccountLogout;
import com.luketran.identity.domain.repositories.AccountLogoutRepository;
import com.luketran.identity.domain.repositories.AccountSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountLogoutService implements com.luketran.identity.application.interfaces.AccountLogoutService {
    private final AccountLogoutRepository accountLogoutRepository;
    private final AccountSessionRepository accountSessionRepository;

    @Override
    @Transactional
    public boolean checkForceLogout(UUID accountId) {
        boolean isExisted = accountLogoutRepository.findByAccountId(accountId).isPresent();
        if (isExisted) {
            accountLogoutRepository.deleteByAccountId(accountId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void createForceLogout(UUID accountId) {
        // 1. Xóa toàn bộ sessions (invalidate refresh tokens)
        accountSessionRepository.deleteAllByAccountId(accountId);

        // 2. Tạo flag nếu chưa có
        if (accountLogoutRepository.findByAccountId(accountId).isEmpty()) {
            AccountLogout logout = new AccountLogout();
            logout.setId(UUID.randomUUID());
            logout.setAccountId(accountId);
            accountLogoutRepository.save(logout);
        }
    }

    @Override
    public boolean isForceLoggedOut(UUID accountId) {
        return accountLogoutRepository.findByAccountId(accountId).isPresent();
    }
}
