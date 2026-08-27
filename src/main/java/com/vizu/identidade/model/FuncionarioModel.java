package com.vizu.identidade.model;

import java.math.BigDecimal;
import java.util.UUID;

public record FuncionarioModel(UUID id, UUID contratanteId, String nome, BigDecimal comissaoPercentual, boolean ativo) {
}
