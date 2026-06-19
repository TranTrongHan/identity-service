package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AppRoleCreateRequest;
import com.luketran.identity.application.dto.request.AppRolePermissionSetRequest;
import com.luketran.identity.application.dto.request.AppRoleUpdateRequest;
import com.luketran.identity.application.dto.response.AppRoleDataResponse;

import java.util.List;
import java.util.UUID;

public interface AppRoleService {

    UUID create(AppRoleCreateRequest input);

    void update(UUID id, AppRoleUpdateRequest input);

    List<AppRoleDataResponse> findAll();

    void softDelete(UUID id);

    void setPermissions(UUID roleId, AppRolePermissionSetRequest input);
}
