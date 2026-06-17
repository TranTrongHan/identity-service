package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.AppCreateRequest;
import com.luketran.identity.application.dto.request.AppUpdateRequest;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService implements com.luketran.identity.application.interfaces.AppService {
    private final AppRepository appRepository;

    @Override
    public App findById(UUID appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
    }

    @Override
    @Transactional
    public UUID create(AppCreateRequest input) {
        App app = new App();
        app.setId(UUID.randomUUID());
        app.setCode(input.getCode());
        app.setName(input.getName());
        app.setDescription(input.getDescription());
        app.setSigningKey(input.getSigningKey());
        app.setTokenLifetimeMinutes(input.getTokenLifetimeMinutes());
        app.setSessionLifetimeMinutes(input.getSessionLifetimeMinutes());
        App saved = appRepository.save(app);
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(UUID id, AppUpdateRequest input) {
        App app = appRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
        if (input.getName() != null) app.setName(input.getName());
        if (input.getDescription() != null) app.setDescription(input.getDescription());
        if (input.getTokenLifetimeMinutes() != null) app.setTokenLifetimeMinutes(input.getTokenLifetimeMinutes());
        if (input.getSessionLifetimeMinutes() != null) app.setSessionLifetimeMinutes(input.getSessionLifetimeMinutes());
        appRepository.save(app);
    }

    @Override
    public List<App> findAll() {
        return appRepository.findAllActive();
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        appRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
        appRepository.softDelete(id);
    }
}
