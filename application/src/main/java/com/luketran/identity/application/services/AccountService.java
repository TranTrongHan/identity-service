package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AccountAuthInputRequest;
import com.luketran.identity.application.dto.request.AccountCreateInputRequest;
import com.luketran.identity.application.helpers.PasswordHelper;
import com.luketran.identity.application.helpers.RandomHelper;
import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.entities.AccountAuth;
import com.luketran.identity.domain.enums.AuthFieldType;
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
}
