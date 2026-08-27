package com.vizu.identidade.model;

import java.util.UUID;

public record ClienteModel(UUID id, UUID contratanteId, String nome, String telefone, String email, boolean ativo) {
}
