package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AccountAuthInputRequest;
import com.luketran.identity.application.dto.request.AccountCreateInputRequest;
import com.luketran.identity.domain.entities.Account;

import java.util.UUID;

public interface AccountService {
    UUID create(AccountCreateInputRequest input);

    void setAuthField(Account account, AccountAuthInputRequest input);
}
