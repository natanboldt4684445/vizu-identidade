package com.vizu.identidade.shared.security;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TenantContext {
    public UUID tenantId(Jwt jwt) {
        String value = jwt.getClaimAsString("tenant_id");
        if (value == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token sem tenant");
        return UUID.fromString(value);
    }
    public UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
