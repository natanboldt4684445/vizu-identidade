package com.vizu.identidade.onboarding.repository;
import java.util.UUID; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class OnboardingRepository {
 private final JdbcTemplate jdbc; public OnboardingRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public void tenant(UUID id,String fantasy,String legal,String cnpj,String phone,short due){jdbc.update("INSERT INTO identidade.contratante(contratante_id,nome_fantasia,razao_social,cnpj,telefone_responsavel,dia_vencimento,pagamento_ate) VALUES(?,?,?,?,?,?,now()+interval '30 days')",id,fantasy,legal,cnpj,phone,due);}
 public void config(UUID tenant,String name,String subdomain){jdbc.update("INSERT INTO identidade.configuracao_plataforma(contratante_id,nome,subdominio) VALUES(?,?,?)",tenant,name,subdomain);}
 public void store(UUID id,UUID tenant,String name,String phone,String street,String number,String district,String city,String state,String zip,int capacity){jdbc.update("INSERT INTO identidade.loja(loja_id,contratante_id,nome,telefone,logradouro,numero,bairro,cidade,estado,cep,capacidade_simultanea) VALUES(?,?,?,?,?,?,?,?,?,?,?)",id,tenant,name,phone,street,number,district,city,state,zip,capacity);}
 public void user(UUID id,UUID tenant,String name,String email,String phone,String password){jdbc.update("INSERT INTO identidade.usuario(usuario_id,contratante_id,nome,email,telefone) VALUES(?,?,?,?,?)",id,tenant,name,email,phone);jdbc.update("INSERT INTO identidade.credencial_local(usuario_id,senha_hash) VALUES(?,?)",id,password);}
 public void links(UUID user,UUID store,UUID tenant,UUID profile){jdbc.update("INSERT INTO identidade.usuario_loja(usuario_id,loja_id,contratante_id,loja_padrao) VALUES(?,?,?,true)",user,store,tenant);jdbc.update("INSERT INTO identidade.usuario_perfil(usuario_id,perfil_id) VALUES(?,?)",user,profile);}
}
