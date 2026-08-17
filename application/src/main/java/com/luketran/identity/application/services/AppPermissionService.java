package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AppPermissionSetupItemRequest;
import com.luketran.identity.application.dto.request.AppPermissionSetupRequest;
import com.luketran.identity.application.dto.response.AppPermissionDataResponse;
import com.luketran.identity.application.helpers.ScopeHelper;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.domain.entities.AppPermission;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AppAccessRepository;
import com.luketran.identity.domain.repositories.AppPermissionRepository;
import com.luketran.identity.domain.repositories.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppPermissionService implements com.luketran.identity.application.interfaces.AppPermissionService {

    private final AppRepository appRepository;
    private final AppPermissionRepository appPermissionRepository;
    private final AppAccessRepository appAccessRepository;

    @Override
    @Transactional
    public void setup(AppPermissionSetupRequest input) {
        // 1. Tìm App bằng appCode
        App app = appRepository.findByCode(input.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found with code: " + input.getAppCode()));

        // 2. Lấy permissions hiện tại của app
        List<AppPermission> existing = appPermissionRepository.findAllActiveByAppId(app.getId());

        // 3. Xóa permissions không còn trong input
        Set<UUID> inputIds = input.getPermissions().stream()
                .map(AppPermissionSetupItemRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (AppPermission perm : existing) {
            if (!inputIds.contains(perm.getId())) {
                appPermissionRepository.deleteById(perm.getId());
            }
        }

        // 4. Upsert từng permission
        Map<UUID, AppPermission> existingMap = existing.stream()
                .collect(Collectors.toMap(AppPermission::getId, p -> p));

        for (AppPermissionSetupItemRequest item : input.getPermissions()) {
            if (item.getId() != null && existingMap.containsKey(item.getId())) {
                // UPDATE
                AppPermission perm = existingMap.get(item.getId());
                perm.setCode(item.getCode());
                perm.setName(item.getName());
                perm.setGroupName(item.getGroupName());
                perm.setDescription(item.getDescription());
                perm.setIncludePermissionCodes(item.getIncludePermissionCodes());
                appPermissionRepository.save(perm);
            } else {
                // CREATE
                AppPermission perm = new AppPermission();
                perm.setId(item.getId() != null ? item.getId() : UUID.randomUUID());
                perm.setAppId(app.getId());
                perm.setCode(item.getCode());
                perm.setName(item.getName());
                perm.setGroupName(item.getGroupName());
                perm.setDescription(item.getDescription());
                perm.setIncludePermissionCodes(item.getIncludePermissionCodes());
                appPermissionRepository.save(perm);
            }
        }

        // 5. Sync scope — loại bỏ permission codes không còn tồn tại khỏi AppAccess.scope
        Set<String> currentCodes = input.getPermissions().stream()
                .map(AppPermissionSetupItemRequest::getCode)
                .collect(Collectors.toSet());

        List<AppAccess> appAccesses = appAccessRepository.findAllActiveByAppId(app.getId());
        for (AppAccess access : appAccesses) {
            if (access.getScope() != null && !access.getScope().isBlank()) {
                String filtered = ScopeHelper.filterToKnownPermissions(access.getScope(), currentCodes);
                if (!access.getScope().equals(filtered)) {
                    access.setScope(filtered);
                    appAccessRepository.save(access);
                }
            }
        }
    }

    @Override
    public List<AppPermissionDataResponse> findAllActiveByAppCode(String appCode) {
        App app = appRepository.findByCode(appCode)
                .orElseThrow(() -> new ResourceNotFoundException("App not found with code: " + appCode));
        return appPermissionRepository.findAllActiveByAppId(app.getId()).stream().map(perm -> {
            AppPermissionDataResponse dto = new AppPermissionDataResponse();
            dto.setId(perm.getId());
            dto.setAppId(perm.getAppId());
            dto.setCode(perm.getCode());
            dto.setName(perm.getName());
            dto.setGroupName(perm.getGroupName());
            dto.setDescription(perm.getDescription());
            dto.setIncludePermissionCodes(perm.getIncludePermissionCodes());
            dto.setCreatedAt(perm.getCreatedAt());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        appPermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppPermission not found"));
        appPermissionRepository.softDelete(id);
    }
}
