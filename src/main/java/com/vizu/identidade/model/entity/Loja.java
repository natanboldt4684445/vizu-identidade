package com.vizu.identidade.model.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(schema="identidade",name="loja") public class Loja { @Id @Column(name="loja_id") private UUID id; @Column(name="contratante_id") private UUID contratanteId; private String nome; private String telefone; private String cidade; private boolean ativo; protected Loja(){} public UUID getId(){return id;} public UUID getContratanteId(){return contratanteId;} }
