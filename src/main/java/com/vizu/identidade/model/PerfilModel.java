package com.vizu.identidade.model;

import java.util.UUID;

public record PerfilModel(UUID id, UUID contratanteId, String nome, String descricao, boolean sistema, boolean ativo) {
}
