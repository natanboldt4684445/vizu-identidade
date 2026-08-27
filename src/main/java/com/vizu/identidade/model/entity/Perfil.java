package com.vizu.identidade.model.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(schema="identidade",name="perfil") public class Perfil { @Id @Column(name="perfil_id") private UUID id; @Column(name="contratante_id") private UUID contratanteId; private String nome; private String descricao; private boolean sistema; private boolean ativo; protected Perfil(){} public UUID getId(){return id;} public UUID getContratanteId(){return contratanteId;} }
