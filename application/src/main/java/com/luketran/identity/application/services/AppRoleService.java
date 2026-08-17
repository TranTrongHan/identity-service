package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AppRoleCreateRequest;
import com.luketran.identity.application.dto.request.AppRolePermissionSetRequest;
import com.luketran.identity.application.dto.request.AppRoleUpdateRequest;
import com.luketran.identity.application.dto.response.AppRoleDataResponse;
import com.luketran.identity.domain.entities.AppRole;
import com.luketran.identity.domain.entities.AppRolePermission;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AppRolePermissionRepository;
import com.luketran.identity.domain.repositories.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppRoleService implements com.luketran.identity.application.interfaces.AppRoleService {
    private final AppRoleRepository appRoleRepository;
    private final AppRolePermissionRepository appRolePermissionRepository;

    @Override
    @Transactional
    public UUID create(AppRoleCreateRequest input) {
        AppRole role = new AppRole();
        role.setId(UUID.randomUUID());
        role.setAppId(input.getAppId());
        role.setCode(input.getCode());
        role.setName(input.getName());
        AppRole saved = appRoleRepository.save(role);
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(UUID id, AppRoleUpdateRequest input) {
        AppRole role = appRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppRole not found"));
        if (input.getName() != null) role.setName(input.getName());
        appRoleRepository.save(role);
    }

    @Override
    public List<AppRoleDataResponse> findAll() {
        return appRoleRepository.findAllActive().stream().map(role -> {
            AppRoleDataResponse dto = new AppRoleDataResponse();
            dto.setId(role.getId());
            dto.setAppId(role.getAppId());
            dto.setCode(role.getCode());
            dto.setName(role.getName());
            dto.setCreatedAt(role.getCreatedAt());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        appRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppRole not found"));
        appRoleRepository.softDelete(id);
    }

    @Override
    @Transactional
    public void setPermissions(UUID roleId, AppRolePermissionSetRequest input) {
        appRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("AppRole not found"));

        appRolePermissionRepository.deleteAllByRoleId(roleId);

        for (UUID permissionId : input.getPermissionIds()) {
            AppRolePermission rp = new AppRolePermission();
            rp.setId(UUID.randomUUID());
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            appRolePermissionRepository.save(rp);
        }
    }
}
