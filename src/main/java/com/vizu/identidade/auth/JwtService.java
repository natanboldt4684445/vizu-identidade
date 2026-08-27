package com.vizu.identidade.auth;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder e;
    private final String i;
    private final long t;

    public JwtService(JwtEncoder e, @Value("${app.jwt.issuer}") String i, @Value("${app.jwt.access-ttl-minutes}") long t) {
        this.e = e;
        this.i = i;
        this.t = t;
    }

    public String accessToken(UUID id, UUID tenant, String name, List<String> a) {
        Instant n = Instant.now();
        var c = JwtClaimsSet.builder().issuer(i).subject(id.toString()).issuedAt(n).expiresAt(n.plus(t, java.time.temporal.ChronoUnit.MINUTES)).claim("tenant_id", tenant.toString()).claim("name", name).claim("scope", String.join(" ", a)).id(UUID.randomUUID().toString()).build();
        return e.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), c)).getTokenValue();
    }
}
