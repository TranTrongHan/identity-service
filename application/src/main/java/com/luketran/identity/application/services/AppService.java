package com.luketran.identity.application.services;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
