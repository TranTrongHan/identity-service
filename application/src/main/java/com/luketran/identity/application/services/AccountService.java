package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AccountAuthInputRequest;
import com.luketran.identity.application.dto.request.AccountCreateInputRequest;
import com.luketran.identity.application.dto.request.AccountFilterRequest;
import com.luketran.identity.application.dto.request.AdminResetPasswordRequest;
import com.luketran.identity.application.dto.response.PageResponse;
import com.luketran.identity.application.dto.response.account.AccountDetailDataResponse;
import com.luketran.identity.application.helpers.PasswordHelper;
import com.luketran.identity.application.helpers.RandomHelper;
import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.entities.AccountAuth;
import com.luketran.identity.domain.enums.AuthFieldType;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AccountAuthRepository;
import com.luketran.identity.domain.repositories.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService implements com.luketran.identity.application.interfaces.AccountService {
    AccountRepository accountRepository;
    AccountAuthRepository accountAuthRepository;
    @Override
    @Transactional
    public UUID create(AccountCreateInputRequest input) {
        Account account = new Account();
        account.setName(input.getName());

        String secretKey = RandomHelper.generateSecretKey();
        account.setSecretKey(secretKey);
        account.setPassword(PasswordHelper.hashPassword(input.getPassword(), secretKey));

        Account savedAccount = accountRepository.save(account);

        if (input.getUserName() != null && !input.getUserName().isEmpty()) {
            createAuthField(savedAccount, AuthFieldType.USERNAME, input.getUserName());
        }
        if (input.getPhoneNumber() != null && !input.getPhoneNumber().isEmpty()) {
            createAuthField(savedAccount, AuthFieldType.PHONE, input.getPhoneNumber());
        }
        if (input.getEmail() != null && !input.getEmail().isEmpty()) {
            createAuthField(savedAccount, AuthFieldType.EMAIL, input.getEmail());
        }

        return savedAccount.getId();
    }

    private void createAuthField(Account account, AuthFieldType fieldType, String fieldValue) {
        Optional<AccountAuth> existing = accountAuthRepository.findByFieldTypeAndFieldValue(fieldType, fieldValue);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Auth field already exists: " + fieldType + " = " + fieldValue);
        }

        AccountAuth auth = new AccountAuth();
        auth.setAccountId(account.getId());
        auth.setFieldType(fieldType);
        auth.setFieldValue(fieldValue);
        accountAuthRepository.save(auth);
    }

    @Override
    @Transactional
    public void setAuthField(Account account, AccountAuthInputRequest input) {
        AuthFieldType fieldType = AuthFieldType.fromValue(input.getFieldType());
        List<AccountAuth> accountAuths = accountAuthRepository.findAllByAccountId(account.getId());

        for (AccountAuth aa : accountAuths) {
            if (aa.getFieldType() == fieldType) {
                // Check uniqueness before updating
                Optional<AccountAuth> duplicate = accountAuthRepository.findByFieldTypeAndFieldValue(fieldType, input.getFieldValue());
                if (duplicate.isPresent() && !duplicate.get().getId().equals(aa.getId())) {
                    throw new IllegalArgumentException("Auth field already exists: " + fieldType + " = " + input.getFieldValue());
                }
                aa.setFieldValue(input.getFieldValue());
                accountAuthRepository.save(aa);
                return;
            }
        }

        // Not found — create new
        createAuthField(account, fieldType, input.getFieldValue());
    }

    /**
     * @param accountId
     */
    @Override
    public void resetPassword(UUID accountId, AdminResetPasswordRequest input) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setPassword(PasswordHelper.hashPassword(input.getNewPassword(), account.getSecretKey()));
        accountRepository.save(account);
    }

    /**
     * @param accountId
     */
    @Override
    public void unlockAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setAccessDeniedUntil(null);
        account.setWrongLoginCount(0);
        accountRepository.save(account);
    }

    /**
     * @param accountId
     * @return
     */
    @Override
    public AccountDetailDataResponse retreive(UUID accountId) {
        Account account = accountRepository.findWithDetails(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        AccountDetailDataResponse response = new AccountDetailDataResponse();
        response.setId(account.getId());
        response.setCreatedAt(account.getCreatedAt());
        response.setDeletedAt(account.getDeletedAt());
        response.setName(account.getName());
        response.setAvatarUrl(account.getAvatarUrl());
        response.setWrongLoginCount(account.getWrongLoginCount());
        response.setAccessDeniedUntil(account.getAccessDeniedUntil());

        // Map auth methods
        if (account.getAuthMethods() != null) {
            response.setAuthMethods(account.getAuthMethods().stream().map(auth -> {
                var item = new AccountDetailDataResponse.AuthMethodItem();
                item.setId(auth.getId());
                item.setFieldType(auth.getFieldType().name());
                item.setFieldValue(auth.getFieldValue());
                return item;
            }).toList());
        }

        // Map app accesses
        if (account.getAppAccesses() != null) {
            response.setAppAccesses(account.getAppAccesses().stream().map(access -> {
                var item = new AccountDetailDataResponse.AppAccessItem();
                item.setId(access.getId());
                item.setAppCode(access.getApp() != null ? access.getApp().getCode() : null);
                item.setAppName(access.getApp() != null ? access.getApp().getName() : null);
                item.setRoleCode(access.getRole() != null ? access.getRole().getCode() : null);
                item.setRoleName(access.getRole() != null ? access.getRole().getName() : null);
                item.setScope(access.getScope());
                return item;
            }).toList());
        }

        // Map sessions
        if (account.getSessions() != null) {
            response.setSessions(account.getSessions().stream().map(session -> {
                var item = new AccountDetailDataResponse.SessionItem();
                item.setId(session.getId());
                item.setAppCode(session.getApp() != null ? session.getApp().getCode() : null);
                item.setExpiredAt(session.getExpiredAt());
                item.setCreatedAt(session.getCreatedAt());
                return item;
            }).toList());
        }

        return response;
    }

    @Override
    public PageResponse<AccountDetailDataResponse.AccountListItem> getPage(AccountFilterRequest filter) {
        String statusStr = filter.getStatus() != null ? filter.getStatus().name() : null;
        List<Account> accounts = accountRepository.findPage(
                filter.getName(), statusStr, filter.getOffset(), filter.getSize());
        long total = accountRepository.count(filter.getName(), statusStr);

        List<AccountDetailDataResponse.AccountListItem> items = accounts.stream().map(account -> {
            var item = new AccountDetailDataResponse.AccountListItem();
            item.setId(account.getId());
            item.setName(account.getName());
            item.setAvatarUrl(account.getAvatarUrl());
            item.setCreatedAt(account.getCreatedAt());
            item.setDeletedAt(account.getDeletedAt());
            return item;
        }).toList();

        return PageResponse.of(items, total, filter.getPage(), filter.getSize());
    }

    @Override
    public void softDelete(UUID accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountRepository.softDelete(accountId);
    }
}
