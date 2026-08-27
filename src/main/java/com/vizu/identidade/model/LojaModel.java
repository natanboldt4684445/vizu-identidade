package com.vizu.identidade.model;

import java.util.UUID;

public record LojaModel(UUID id, UUID contratanteId, String nome, String telefone, String cidade, String estado,
                        boolean ativo) {
}
