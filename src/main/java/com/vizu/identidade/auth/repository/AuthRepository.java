package com.vizu.identidade.auth.repository;

import java.time.Duration;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbc;

    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<AuthenticatedUser> findActiveByEmail(String email) {
        return jdbc.query("SELECT u.usuario_id,u.contratante_id,u.nome,c.senha_hash FROM identidade.usuario u JOIN identidade.credencial_local c ON c.usuario_id=u.usuario_id WHERE lower(u.email)=lower(?) AND u.ativo=true", (rs, n) -> new AuthenticatedUser(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)), rs.getString(3), rs.getString(4)), email).stream().findFirst();
    }

    public Optional<AuthenticatedUser> findActiveByRefreshHash(String hash) {
        return jdbc.query("SELECT s.usuario_id,u.contratante_id,u.nome,c.senha_hash FROM identidade.sessao_refresh_token s JOIN identidade.usuario u ON u.usuario_id=s.usuario_id JOIN identidade.credencial_local c ON c.usuario_id=u.usuario_id WHERE s.token_hash=? AND s.revogado_em IS NULL AND s.expira_em>now() AND u.ativo=true", (rs, n) -> new AuthenticatedUser(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)), rs.getString(3), rs.getString(4)), hash).stream().findFirst();
    }

    public void revoke(String hash) {
        jdbc.update("UPDATE identidade.sessao_refresh_token SET revogado_em=now() WHERE token_hash=?", hash);
    }

    public void saveRefresh(UUID userId, UUID familyId, String hash, long days) {
        jdbc.update("INSERT INTO identidade.sessao_refresh_token(usuario_id,familia_id,token_hash,expira_em) VALUES(?,?,?,now()+ (? || ' days')::interval)", userId, familyId, hash, days);
    }

    public List<String> permissions(UUID userId) {
        return jdbc.queryForList("SELECT DISTINCT a.codigo FROM identidade.acesso a LEFT JOIN identidade.acesso_usuario au ON au.acesso_id=a.acesso_id AND au.usuario_id=? LEFT JOIN identidade.usuario_perfil up ON up.usuario_id=? LEFT JOIN identidade.perfil_acesso pa ON pa.perfil_id=up.perfil_id AND pa.acesso_id=a.acesso_id WHERE a.ativo AND (au.usuario_id IS NOT NULL OR pa.perfil_id IS NOT NULL) ORDER BY a.codigo", String.class, userId, userId);
    }

    public record AuthenticatedUser(UUID id, UUID tenantId, String nome, String passwordHash) {
    }
}
