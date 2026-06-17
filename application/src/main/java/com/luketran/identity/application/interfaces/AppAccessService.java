package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AppAccessCreateRequest;
import com.luketran.identity.application.dto.request.AppAccessUpdateRequest;
import com.luketran.identity.domain.entities.AppAccess;

import java.util.UUID;

public interface AppAccessService {

    UUID create(AppAccessCreateRequest input);

    void update(UUID id, AppAccessUpdateRequest input);

    AppAccess getByAppCodeAndAccountId(String appCode, UUID accountId);

    void softDelete(UUID id);
}
