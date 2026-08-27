package com.vizu.identidade.cadastro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.vizu.identidade.integracao.service.OutboxService;

@RestController
public class CadastroController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final OutboxService outbox;

    public CadastroController(JdbcTemplate jdbc, PasswordEncoder passwords, OutboxService outbox) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.outbox = outbox;
    }

    private UUID tenant(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("tenant_id"));
    }

    private UUID user(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private void owned(String table, String column, UUID id, UUID tenant) {
        if (jdbc.queryForObject("SELECT count(*) FROM identidade." + table + " WHERE " + column + "=? AND contratante_id=?", Integer.class, id, tenant) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void emailAvailable(UUID tenant, String email, UUID ignoredUserId) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM identidade.usuario WHERE contratante_id=? AND lower(email)=lower(?) AND (? IS NULL OR usuario_id<>?)", Integer.class, tenant, email, ignoredUserId, ignoredUserId);
        if (count != null && count > 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já utilizado neste contratante");
    }

    private static String digits(String s) {
        return s.replaceAll("\\D", "");
    }

    @GetMapping("/contratantes/me") @PreAuthorize("hasAuthority('SCOPE_identidade.contratante.visualizar')")
    public Map<String, Object> getTenant(@AuthenticationPrincipal Jwt jwt) {
        UUID t = tenant(jwt);
        return jdbc.queryForMap("SELECT contratante_id,nome_fantasia,razao_social,cnpj,telefone_responsavel,timezone,dia_vencimento,ativo FROM identidade.contratante WHERE contratante_id=?", t);
    }

    @PatchMapping("/contratantes/me") @PreAuthorize("hasAuthority('SCOPE_identidade.contratante.editar')")
    public void updateTenant(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TenantRequest r) {
        jdbc.update("UPDATE identidade.contratante SET nome_fantasia=?,razao_social=?,telefone_responsavel=?,timezone=?,atualizado_em=now() WHERE contratante_id=?", r.nomeFantasia(), r.razaoSocial(), r.telefone(), r.timezone(), tenant(jwt));
        outbox.contratante(tenant(jwt),true,r.timezone());
    }

    @GetMapping("/lojas") @PreAuthorize("hasAuthority('SCOPE_identidade.loja.visualizar')")
    public List<Map<String, Object>> stores(@AuthenticationPrincipal Jwt jwt) {
        return jdbc.queryForList("SELECT loja_id,nome,telefone,cidade,estado,ativo FROM identidade.loja WHERE contratante_id=? ORDER BY nome", tenant(jwt));
    }

    @PostMapping("/lojas") @PreAuthorize("hasAuthority('SCOPE_identidade.loja.criar')")
    @ResponseStatus(HttpStatus.CREATED)
    public Id createdStore(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StoreRequest r) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO identidade.loja(loja_id,contratante_id,nome,telefone,logradouro,numero,complemento,bairro,cidade,estado,cep,capacidade_simultanea) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)", id, tenant(jwt), r.nome(), r.telefone(), r.logradouro(), r.numero(), r.complemento(), r.bairro(), r.cidade(), r.estado().toUpperCase(), digits(r.cep()), r.capacidadeSimultanea());
        outbox.loja(id,tenant(jwt),r.nome(),r.capacidadeSimultanea(),true);
        return new Id(id);
    }

    @GetMapping("/lojas/{id}") @PreAuthorize("hasAuthority('SCOPE_identidade.loja.visualizar')")
    public Map<String, Object> store(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        owned("loja", "loja_id", id, tenant(jwt));
        return jdbc.queryForMap("SELECT * FROM identidade.loja WHERE loja_id=?", id);
    }

    @PatchMapping("/lojas/{id}") @PreAuthorize("hasAuthority('SCOPE_identidade.loja.editar')")
    public void updateStore(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody StoreRequest r) {
        owned("loja", "loja_id", id, tenant(jwt));
        jdbc.update("UPDATE identidade.loja SET nome=?,telefone=?,logradouro=?,numero=?,complemento=?,bairro=?,cidade=?,estado=?,cep=?,capacidade_simultanea=?,atualizado_em=now() WHERE loja_id=?", r.nome(), r.telefone(), r.logradouro(), r.numero(), r.complemento(), r.bairro(), r.cidade(), r.estado().toUpperCase(), digits(r.cep()), r.capacidadeSimultanea(), id);
        outbox.loja(id,tenant(jwt),r.nome(),r.capacidadeSimultanea(),true);
    }

    @PostMapping("/lojas/{id}/{action:desativar|reativar}") @PreAuthorize("hasAuthority('SCOPE_identidade.loja.desativar')")
    public void toggleStore(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String action) {
        owned("loja", "loja_id", id, tenant(jwt));
        jdbc.update("UPDATE identidade.loja SET ativo=?,atualizado_em=now() WHERE loja_id=?", action.equals("reativar"), id);
        Map<String,Object> loja=jdbc.queryForMap("SELECT nome,capacidade_simultanea FROM identidade.loja WHERE loja_id=?",id);
        outbox.loja(id,tenant(jwt),(String)loja.get("nome"),((Number)loja.get("capacidade_simultanea")).intValue(),action.equals("reativar"));
    }

    @GetMapping("/usuarios") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.visualizar')")
    public List<Map<String, Object>> users(@AuthenticationPrincipal Jwt jwt) {
        return jdbc.queryForList("SELECT usuario_id,nome,email,telefone,ativo FROM identidade.usuario WHERE contratante_id=? ORDER BY nome", tenant(jwt));
    }

    @PostMapping("/usuarios") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.criar')")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Id createUser(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserRequest r) {
        UUID id = UUID.randomUUID(), t = tenant(jwt);
        emailAvailable(t, r.email(), null);
        jdbc.update("INSERT INTO identidade.usuario(usuario_id,contratante_id,nome,email,telefone) VALUES(?,?,?,?,?)", id, t, r.nome(), lower(r.email()), r.telefone());
        jdbc.update("INSERT INTO identidade.credencial_local(usuario_id,senha_hash,senha_temporaria) VALUES(?,?,true)", id, passwords.encode(r.senha()));
        return new Id(id);
    }

    @GetMapping("/usuarios/{id}") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.visualizar')")
    public Map<String, Object> getUser(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        owned("usuario", "usuario_id", id, tenant(jwt));
        return jdbc.queryForMap("SELECT usuario_id,nome,email,telefone,ativo,recebe_notificacao FROM identidade.usuario WHERE usuario_id=?", id);
    }

    @PatchMapping("/usuarios/{id}") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.editar')")
    public void updateUser(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody UserPatch r) {
        owned("usuario", "usuario_id", id, tenant(jwt));
        emailAvailable(tenant(jwt), r.email(), id);
        jdbc.update("UPDATE identidade.usuario SET nome=?,email=?,telefone=?,recebe_notificacao=?,atualizado_em=now() WHERE usuario_id=?", r.nome(), lower(r.email()), r.telefone(), r.recebeNotificacao(), id);
    }

    @PostMapping("/usuarios/{id}/{action:desativar|reativar}") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.desativar')")
    public void toggleUser(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String action) {
        owned("usuario", "usuario_id", id, tenant(jwt));
        if (id.equals(user(jwt)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A própria conta não pode ser desativada");
        jdbc.update("UPDATE identidade.usuario SET ativo=?,authz_version=authz_version+1,atualizado_em=now() WHERE usuario_id=?", action.equals("reativar"), id);
    }

    @PutMapping("/usuarios/{id}/lojas") @PreAuthorize("hasAuthority('SCOPE_identidade.usuario.editar')")
    @Transactional
    public void setUserStores(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody StoreLinks r) {
        UUID t = tenant(jwt);
        owned("usuario", "usuario_id", id, t);
        if (r.lojas().isEmpty() || r.lojaPadrao() == null || !r.lojas().contains(r.lojaPadrao()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe lojas e uma loja padrão vinculada");
        for (UUID store : r.lojas()) {
            owned("loja", "loja_id", store, t);
        }
        jdbc.update("DELETE FROM identidade.usuario_loja WHERE usuario_id=?", id);
        for (UUID store : r.lojas())
            jdbc.update("INSERT INTO identidade.usuario_loja(usuario_id,loja_id,contratante_id,loja_padrao) VALUES(?,?,?,?)", id, store, t, store.equals(r.lojaPadrao()));
    }

    @PutMapping("/usuarios/{id}/perfis") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    @Transactional
    public void setProfiles(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody Ids r) {
        UUID t = tenant(jwt);
        owned("usuario", "usuario_id", id, t);
        for (UUID profile : r.ids())
            if (jdbc.queryForObject("SELECT count(*) FROM identidade.perfil WHERE perfil_id=? AND ativo AND (sistema OR contratante_id=?)", Integer.class, profile, t) == 0)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        jdbc.update("DELETE FROM identidade.usuario_perfil WHERE usuario_id=?", id);
        for (UUID p : r.ids())
            jdbc.update("INSERT INTO identidade.usuario_perfil(usuario_id,perfil_id) VALUES(?,?)", id, p);
        jdbc.update("UPDATE identidade.usuario SET authz_version=authz_version+1 WHERE usuario_id=?", id);
    }

    @PutMapping("/usuarios/{id}/acessos-diretos") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    @Transactional
    public void setDirectAccess(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody Ids r) {
        owned("usuario", "usuario_id", id, tenant(jwt));
        for (UUID a : r.ids())
            if (jdbc.queryForObject("SELECT count(*) FROM identidade.acesso WHERE acesso_id=? AND ativo", Integer.class, a) == 0)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        jdbc.update("DELETE FROM identidade.acesso_usuario WHERE usuario_id=?", id);
        for (UUID a : r.ids())
            jdbc.update("INSERT INTO identidade.acesso_usuario(usuario_id,acesso_id) VALUES(?,?)", id, a);
        jdbc.update("UPDATE identidade.usuario SET authz_version=authz_version+1 WHERE usuario_id=?", id);
    }

    @GetMapping("/usuarios/me/lojas")
    public List<Map<String, Object>> myStores(@AuthenticationPrincipal Jwt jwt) {
        return jdbc.queryForList("SELECT l.loja_id,l.nome,ul.loja_padrao FROM identidade.usuario_loja ul JOIN identidade.loja l ON l.loja_id=ul.loja_id WHERE ul.usuario_id=? AND l.ativo ORDER BY ul.loja_padrao DESC,l.nome", user(jwt));
    }

    @GetMapping("/usuarios/me/acessos")
    public List<Map<String, Object>> myPermissions(@AuthenticationPrincipal Jwt jwt) {
        return jdbc.queryForList("SELECT DISTINCT a.acesso_id,a.codigo,a.descricao FROM identidade.acesso a LEFT JOIN identidade.acesso_usuario au ON au.acesso_id=a.acesso_id AND au.usuario_id=? LEFT JOIN identidade.usuario_perfil up ON up.usuario_id=? LEFT JOIN identidade.perfil_acesso pa ON pa.perfil_id=up.perfil_id AND pa.acesso_id=a.acesso_id WHERE a.ativo AND (au.usuario_id IS NOT NULL OR pa.perfil_id IS NOT NULL) ORDER BY a.codigo", user(jwt), user(jwt));
    }

    @GetMapping("/acessos") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    public List<Map<String, Object>> accesses() {
        return jdbc.queryForList("SELECT acesso_id,codigo,descricao,ativo FROM identidade.acesso ORDER BY codigo");
    }

    @GetMapping("/perfis") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    public List<Map<String, Object>> profiles(@AuthenticationPrincipal Jwt jwt) {
        return jdbc.queryForList("SELECT perfil_id,nome,descricao,sistema,ativo FROM identidade.perfil WHERE sistema OR contratante_id=? ORDER BY sistema DESC,nome", tenant(jwt));
    }

    @PostMapping("/perfis") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    @ResponseStatus(HttpStatus.CREATED)
    public Id createProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest r) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO identidade.perfil(perfil_id,contratante_id,nome,descricao,sistema) VALUES(?,?,?,?,false)", id, tenant(jwt), r.nome(), r.descricao());
        return new Id(id);
    }

    @PatchMapping("/perfis/{id}") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    public void updateProfile(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ProfileRequest r) {
        owned("perfil", "perfil_id", id, tenant(jwt));
        jdbc.update("UPDATE identidade.perfil SET nome=?,descricao=?,atualizado_em=now() WHERE perfil_id=? AND sistema=false", r.nome(), r.descricao(), id);
    }

    @PutMapping("/perfis/{id}/acessos") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    @Transactional
    public void setProfileAccess(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody Ids r) {
        owned("perfil", "perfil_id", id, tenant(jwt));
        jdbc.update("DELETE FROM identidade.perfil_acesso WHERE perfil_id=?", id);
        for (UUID a : r.ids())
            jdbc.update("INSERT INTO identidade.perfil_acesso(perfil_id,acesso_id) SELECT ?,acesso_id FROM identidade.acesso WHERE acesso_id=? AND ativo", id, a);
    }

    @PostMapping("/perfis/{id}/{action:desativar|reativar}") @PreAuthorize("hasAuthority('SCOPE_identidade.perfil.gerenciar')")
    public void toggleProfile(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String action) {
        owned("perfil", "perfil_id", id, tenant(jwt));
        jdbc.update("UPDATE identidade.perfil SET ativo=?,atualizado_em=now() WHERE perfil_id=? AND sistema=false", action.equals("reativar"), id);
    }

    public record Id(UUID id) {
    }

    public record Ids(@NotNull List<UUID> ids) {
    }

    public record TenantRequest(@NotBlank String nomeFantasia, @NotBlank String razaoSocial, @NotBlank String telefone,
                                @NotBlank String timezone) {
    }

    public record StoreRequest(@NotBlank String nome, @NotBlank String telefone, @NotBlank String logradouro,
                               @NotBlank String numero, String complemento, @NotBlank String bairro,
                               @NotBlank String cidade, @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String estado,
                               @NotBlank String cep, @Positive int capacidadeSimultanea) {
    }

    public record UserRequest(@NotBlank String nome, @NotBlank @Email String email, String telefone,
                              @NotBlank @Size(min = 8, max = 100) String senha) {
    }

    public record UserPatch(@NotBlank String nome, @NotBlank @Email String email, String telefone,
                            boolean recebeNotificacao) {
    }

    public record StoreLinks(@NotNull List<UUID> lojas, UUID lojaPadrao) {
    }

    public record ProfileRequest(@NotBlank @Size(max = 100) String nome, @NotBlank String descricao) {
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
