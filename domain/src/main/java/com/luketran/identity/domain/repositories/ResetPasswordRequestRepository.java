package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.ResetPasswordRequest;

import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordRequestRepository {

    Optional<ResetPasswordRequest> findById(UUID id);

    ResetPasswordRequest save(ResetPasswordRequest entity);

    void deleteById(UUID id);
}
