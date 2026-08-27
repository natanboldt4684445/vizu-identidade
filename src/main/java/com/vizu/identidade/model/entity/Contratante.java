package com.vizu.identidade.model.entity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(schema="identidade",name="contratante") public class Contratante {
 @Id @Column(name="contratante_id") private UUID id; @Column(name="nome_fantasia") private String nomeFantasia; @Column(name="razao_social") private String razaoSocial; private String cnpj; @Column(name="telefone_responsavel") private String telefoneResponsavel; private String timezone; @Column(name="dia_vencimento") private Short diaVencimento; private boolean ativo; @Column(name="criado_em") private OffsetDateTime criadoEm; @Column(name="atualizado_em") private OffsetDateTime atualizadoEm;
 protected Contratante(){} public UUID getId(){return id;} public boolean isAtivo(){return ativo;}
}
