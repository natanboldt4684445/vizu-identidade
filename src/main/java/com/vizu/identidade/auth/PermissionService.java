package com.vizu.identidade.auth;

import com.vizu.identidade.auth.repository.AuthRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private final AuthRepository repository;

    public PermissionService(AuthRepository repository) {
        this.repository = repository;
    }

    public List<String> effectivePermissions(UUID userId) {
        return repository.permissions(userId);
    }
}
