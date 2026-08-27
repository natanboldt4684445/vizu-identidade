package com.vizu.identidade.usuario;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final JdbcTemplate j;

    public UsuarioController(JdbcTemplate j) {
        this.j = j;
    }

    @GetMapping("/me")
    public UsuarioMe me(@AuthenticationPrincipal Jwt t) {
        UUID id = UUID.fromString(t.getSubject());
        return j.queryForObject("SELECT usuario_id,nome,email,telefone,contratante_id FROM identidade.usuario WHERE usuario_id=? AND ativo=true", (s, n) -> new UsuarioMe(UUID.fromString(s.getString(1)), s.getString(2), s.getString(3), s.getString(4), UUID.fromString(s.getString(5))), id);
    }

    public record UsuarioMe(UUID id, String nome, String email, String telefone, UUID contratanteId) {
    }
}
