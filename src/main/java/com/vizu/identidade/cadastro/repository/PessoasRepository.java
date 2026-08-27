package com.vizu.identidade.cadastro.repository;
import java.math.BigDecimal; import java.util.*; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class PessoasRepository {
 private final JdbcTemplate j; public PessoasRepository(JdbcTemplate j){this.j=j;}
 public boolean employeeExists(UUID id,UUID tenant){return exists("funcionario","funcionario_id",id,tenant);} public boolean clientExists(UUID id,UUID tenant){return exists("cliente","cliente_id",id,tenant);} public boolean storeExists(UUID id,UUID tenant){return exists("loja","loja_id",id,tenant);}
 private boolean exists(String t,String k,UUID id,UUID tenant){return j.queryForObject("SELECT count(*) FROM identidade."+t+" WHERE "+k+"=? AND contratante_id=?",Integer.class,id,tenant)>0;}
 public List<Map<String,Object>> employees(UUID t){return j.queryForList("SELECT funcionario_id,nome,comissao_percentual,exibir_no_site,ativo FROM identidade.funcionario WHERE contratante_id=? ORDER BY nome",t);}
 public Map<String,Object> employee(UUID id){return j.queryForMap("SELECT * FROM identidade.funcionario WHERE funcionario_id=?",id);}
 public void createEmployee(UUID id,UUID t,String n,BigDecimal c,String b,boolean site){j.update("INSERT INTO identidade.funcionario(funcionario_id,contratante_id,nome,comissao_percentual,bio,exibir_no_site) VALUES(?,?,?,?,?,?)",id,t,n,c,b,site);}
 public void updateEmployee(UUID id,String n,BigDecimal c,String b,boolean site){j.update("UPDATE identidade.funcionario SET nome=?,comissao_percentual=?,bio=?,exibir_no_site=?,atualizado_em=now() WHERE funcionario_id=?",n,c,b,site,id);}
 public void setEmployeeActive(UUID id,boolean active){j.update("UPDATE identidade.funcionario SET ativo=?,atualizado_em=now() WHERE funcionario_id=?",active,id);}
 public List<UUID> employeeStores(UUID id){return j.query("SELECT loja_id FROM identidade.funcionario_loja WHERE funcionario_id=?",(rs,n)->UUID.fromString(rs.getString(1)),id);}
 public void replaceEmployeeStores(UUID id,UUID t,List<UUID> stores){j.update("DELETE FROM identidade.funcionario_loja WHERE funcionario_id=?",id);for(UUID s:stores)j.update("INSERT INTO identidade.funcionario_loja(funcionario_id,loja_id,contratante_id) VALUES(?,?,?)",id,s,t);}
 public List<Map<String,Object>> clients(UUID t){return j.queryForList("SELECT cliente_id,nome,telefone,email,ativo FROM identidade.cliente WHERE contratante_id=? ORDER BY nome",t);}
 public Map<String,Object> client(UUID id){return j.queryForMap("SELECT * FROM identidade.cliente WHERE cliente_id=?",id);}
 public void createClient(UUID id,UUID t,String n,String p,String e){j.update("INSERT INTO identidade.cliente(cliente_id,contratante_id,nome,telefone,email) VALUES(?,?,?,?,?)",id,t,n,p,e);}
 public void updateClient(UUID id,String n,String p,String e){j.update("UPDATE identidade.cliente SET nome=?,telefone=?,email=?,atualizado_em=now() WHERE cliente_id=?",n,p,e,id);}
 public void setClientActive(UUID id,boolean active){j.update("UPDATE identidade.cliente SET ativo=?,atualizado_em=now() WHERE cliente_id=?",active,id);}
}
