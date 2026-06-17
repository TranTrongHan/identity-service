package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AppAccessCreateRequest;
import com.luketran.identity.application.dto.request.AppAccessUpdateRequest;
import com.luketran.identity.application.helpers.ScopeHelper;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.domain.entities.AppPermission;
import com.luketran.identity.domain.entities.AppRole;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AppAccessRepository;
import com.luketran.identity.domain.repositories.AppPermissionRepository;
import com.luketran.identity.domain.repositories.AppRepository;
import com.luketran.identity.domain.repositories.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppAccessService implements com.luketran.identity.application.interfaces.AppAccessService {

    private final AppRepository appRepository;
    private final AppAccessRepository appAccessRepository;
    private final AppRoleRepository appRoleRepository;
    private final AppPermissionRepository appPermissionRepository;

    @Override
    @Transactional
    public UUID create(AppAccessCreateRequest input) {
        // 1. Resolve App
        App app = appRepository.findByCode(input.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found with code: " + input.getAppCode()));

        // 2. Resolve Role (optional)
        UUID roleId = null;
        if (input.getRoleCode() != null && !input.getRoleCode().isBlank()) {
            AppRole role = appRoleRepository.findByCodeAndAppId(input.getRoleCode(), app.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "AppRole not found with code: " + input.getRoleCode() + " in app: " + input.getAppCode()));
            roleId = role.getId();
        }

        // 3. Validate scope
        String scope = validateScope(input.getScope(), app.getId());

        // 4. Create AppAccess
        AppAccess access = new AppAccess();
        access.setId(UUID.randomUUID());
        access.setAccountId(input.getAccountId());
        access.setAppId(app.getId());
        access.setRoleId(roleId);
        access.setScope(scope);

        AppAccess saved = appAccessRepository.save(access);
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(UUID id, AppAccessUpdateRequest input) {
        // 1. Find existing
        AppAccess access = appAccessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppAccess not found"));

        // 2. Resolve role if changed
        if (input.getRoleCode() != null) {
            if (input.getRoleCode().isBlank()) {
                // Empty string = remove role
                access.setRoleId(null);
            } else {
                AppRole role = appRoleRepository.findByCodeAndAppId(input.getRoleCode(), access.getAppId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "AppRole not found with code: " + input.getRoleCode()));
                access.setRoleId(role.getId());
            }
        }

        // 3. Validate scope if changed
        if (input.getScope() != null) {
            access.setScope(validateScope(input.getScope(), access.getAppId()));
        }

        // 4. Save
        appAccessRepository.save(access);
    }

    @Override
    public AppAccess getByAppCodeAndAccountId(String appCode, UUID accountId) {
        App app = appRepository.findByCode(appCode)
                .orElseThrow(() -> new ResourceNotFoundException("App not found with code: " + appCode));

        return appAccessRepository.findByAccountIdAndAppId(accountId, app.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AppAccess not found for account " + accountId + " in app " + appCode));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        appAccessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppAccess not found"));
        appAccessRepository.softDelete(id);
    }

    private String validateScope(String scope, UUID appId) {
        if (scope == null || scope.isBlank()) {
            return "";
        }

        Set<String> knownCodes = appPermissionRepository.findAllActiveByAppId(appId).stream()
                .map(AppPermission::getCode)
                .collect(Collectors.toSet());

        return ScopeHelper.filterToKnownPermissions(scope, knownCodes);
    }
}
