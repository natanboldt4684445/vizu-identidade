package com.vizu.identidade.model;

import java.util.UUID;

public record UsuarioModel(UUID id, UUID contratanteId, String nome, String email, String telefone, boolean ativo) {
}
