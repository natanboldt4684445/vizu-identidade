package com.vizu.identidade.cadastro.repository;
import java.util.*; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class CadastroRepository {
 private final JdbcTemplate j; public CadastroRepository(JdbcTemplate j){this.j=j;}
 public boolean exists(String table,String column,UUID id,UUID tenant){return j.queryForObject("SELECT count(*) FROM identidade."+table+" WHERE "+column+"=? AND contratante_id=?",Integer.class,id,tenant)>0;}
 public List<Map<String,Object>> listStores(UUID t){return j.queryForList("SELECT loja_id,nome,telefone,cidade,estado,ativo FROM identidade.loja WHERE contratante_id=? ORDER BY nome",t);}
 public Map<String,Object> store(UUID id){return j.queryForMap("SELECT * FROM identidade.loja WHERE loja_id=?",id);}
 public void createStore(UUID id,UUID t,Object... v){j.update("INSERT INTO identidade.loja(loja_id,contratante_id,nome,telefone,logradouro,numero,complemento,bairro,cidade,estado,cep,capacidade_simultanea) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",prepend(id,t,v));}
 public void updateStore(UUID id,Object... v){j.update("UPDATE identidade.loja SET nome=?,telefone=?,logradouro=?,numero=?,complemento=?,bairro=?,cidade=?,estado=?,cep=?,capacidade_simultanea=?,atualizado_em=now() WHERE loja_id=?",append(v,id));}
 public void activeStore(UUID id,boolean active){j.update("UPDATE identidade.loja SET ativo=?,atualizado_em=now() WHERE loja_id=?",active,id);}
 public List<Map<String,Object>> listUsers(UUID t){return j.queryForList("SELECT usuario_id,nome,email,telefone,ativo FROM identidade.usuario WHERE contratante_id=? ORDER BY nome",t);}
 public Map<String,Object> user(UUID id){return j.queryForMap("SELECT usuario_id,nome,email,telefone,ativo,recebe_notificacao FROM identidade.usuario WHERE usuario_id=?",id);}
 public void createUser(UUID id,UUID t,String n,String e,String p,String hash){j.update("INSERT INTO identidade.usuario(usuario_id,contratante_id,nome,email,telefone) VALUES(?,?,?,?,?)",id,t,n,e,p);j.update("INSERT INTO identidade.credencial_local(usuario_id,senha_hash,senha_temporaria) VALUES(?,?,true)",id,hash);}
 public void updateUser(UUID id,String n,String e,String p,boolean notify){j.update("UPDATE identidade.usuario SET nome=?,email=?,telefone=?,recebe_notificacao=?,atualizado_em=now() WHERE usuario_id=?",n,e,p,notify,id);}
 public void activeUser(UUID id,boolean active){j.update("UPDATE identidade.usuario SET ativo=?,authz_version=authz_version+1,atualizado_em=now() WHERE usuario_id=?",active,id);}
 public List<Map<String,Object>> accesses(){return j.queryForList("SELECT acesso_id,codigo,descricao,ativo FROM identidade.acesso ORDER BY codigo");}
 public List<Map<String,Object>> profiles(UUID t){return j.queryForList("SELECT perfil_id,nome,descricao,sistema,ativo FROM identidade.perfil WHERE sistema OR contratante_id=? ORDER BY sistema DESC,nome",t);}
 public void createProfile(UUID id,UUID t,String n,String d){j.update("INSERT INTO identidade.perfil(perfil_id,contratante_id,nome,descricao,sistema) VALUES(?,?,?,?,false)",id,t,n,d);}
 public void updateProfile(UUID id,String n,String d){j.update("UPDATE identidade.perfil SET nome=?,descricao=?,atualizado_em=now() WHERE perfil_id=? AND sistema=false",n,d,id);}
 public void activeProfile(UUID id,boolean active){j.update("UPDATE identidade.perfil SET ativo=?,atualizado_em=now() WHERE perfil_id=? AND sistema=false",active,id);}
 public Map<String,Object> tenant(UUID t){return j.queryForMap("SELECT contratante_id,nome_fantasia,razao_social,cnpj,telefone_responsavel,timezone,dia_vencimento,ativo FROM identidade.contratante WHERE contratante_id=?",t);}
 public void updateTenant(UUID t,String f,String r,String p,String z){j.update("UPDATE identidade.contratante SET nome_fantasia=?,razao_social=?,telefone_responsavel=?,timezone=?,atualizado_em=now() WHERE contratante_id=?",f,r,p,z,t);}
 private static Object[] prepend(Object first,Object second,Object[] values){Object[] result=new Object[values.length+2];result[0]=first;result[1]=second;System.arraycopy(values,0,result,2,values.length);return result;}
 private static Object[] append(Object[] values,Object last){Object[] result=java.util.Arrays.copyOf(values,values.length+1);result[values.length]=last;return result;}
}
