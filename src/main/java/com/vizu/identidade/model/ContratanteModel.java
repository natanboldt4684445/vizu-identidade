package com.vizu.identidade.model;

import java.util.UUID;

public record ContratanteModel(UUID id, String nomeFantasia, String razaoSocial, String cnpj, String telefone,
                               String timezone, boolean ativo) {
}
