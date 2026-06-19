package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.AppCreateRequest;
import com.luketran.identity.application.dto.request.AppUpdateRequest;
import com.luketran.identity.application.dto.response.AppDataResponse;
import com.luketran.identity.domain.entities.App;

import java.util.List;
import java.util.UUID;

public interface AppService {

    App findById(UUID appId);

    UUID create(AppCreateRequest input);

    void update(UUID id, AppUpdateRequest input);

    List<AppDataResponse> findAll();

    void softDelete(UUID id);
}
