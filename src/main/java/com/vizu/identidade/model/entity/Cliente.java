package com.vizu.identidade.model.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(schema="identidade",name="cliente") public class Cliente { @Id @Column(name="cliente_id") private UUID id; @Column(name="contratante_id") private UUID contratanteId; private String nome; private String telefone; private String email; private boolean ativo; protected Cliente(){} public UUID getId(){return id;} public UUID getContratanteId(){return contratanteId;} }
