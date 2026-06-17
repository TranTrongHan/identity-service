package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AppPermissionSetupRequest;
import com.luketran.identity.domain.entities.AppPermission;

import java.util.List;
import java.util.UUID;

public interface AppPermissionService {

    void setup(AppPermissionSetupRequest input);

    List<AppPermission> findAllActiveByAppCode(String appCode);

    void softDelete(UUID id);
}
