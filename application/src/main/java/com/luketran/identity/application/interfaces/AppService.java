package com.luketran.identity.application.interfaces;

import com.luketran.identity.domain.entities.App;

import java.util.UUID;

public interface AppService {
    public App findById(UUID appId);
}
