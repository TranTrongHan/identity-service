package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AccountAuthInputRequest;
import com.luketran.identity.application.dto.request.AccountCreateInputRequest;
import com.luketran.identity.application.dto.request.AccountFilterRequest;
import com.luketran.identity.application.dto.request.AdminResetPasswordRequest;
import com.luketran.identity.application.dto.response.PageResponse;
import com.luketran.identity.application.dto.response.account.AccountDetailDataResponse;
import com.luketran.identity.domain.entities.Account;

import java.util.UUID;

public interface AccountService {
    UUID create(AccountCreateInputRequest input);

    void setAuthField(Account account, AccountAuthInputRequest input);

    void resetPassword(UUID accountId, AdminResetPasswordRequest input);

    void unlockAccount(UUID accountId);

    AccountDetailDataResponse retreive(UUID accountId);

    PageResponse<AccountDetailDataResponse.AccountListItem> getPage(AccountFilterRequest filter);

    void softDelete(UUID accountId);
}
