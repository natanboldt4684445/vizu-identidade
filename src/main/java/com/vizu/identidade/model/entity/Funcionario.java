package com.vizu.identidade.model.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.util.UUID;
@Entity @Table(schema="identidade",name="funcionario") public class Funcionario { @Id @Column(name="funcionario_id") private UUID id; @Column(name="contratante_id") private UUID contratanteId; private String nome; @Column(name="comissao_percentual") private BigDecimal comissaoPercentual; private boolean ativo; protected Funcionario(){} public UUID getId(){return id;} public UUID getContratanteId(){return contratanteId;} }
