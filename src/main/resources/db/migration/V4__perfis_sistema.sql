SET search_path TO identidade, public;

INSERT INTO perfil (perfil_id, contratante_id, nome, descricao, sistema)
VALUES
    ('00000000-0000-0000-0000-000000000003', NULL, 'Dono do Salão',
     'Administrador do contratante e de suas lojas.', TRUE),
    ('00000000-0000-0000-0000-000000000004', NULL, 'Gerente / Recepção',
     'Operação diária de clientes, agenda e pagamentos.', TRUE),
    ('00000000-0000-0000-0000-000000000005', NULL, 'Financeiro do Salão',
     'Pagamentos e relatórios financeiros.', TRUE),
    ('00000000-0000-0000-0000-000000000006', NULL, 'Profissional',
     'Profissional que atende e acompanha sua agenda.', TRUE),
    ('00000000-0000-0000-0000-000000000007', NULL, 'Cliente',
     'Cliente final; operações administrativas não são concedidas.', TRUE)
ON CONFLICT (perfil_id) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    ativo = TRUE,
    atualizado_em = NOW();

/* Administrador da Plataforma: todo o catálogo. */
INSERT INTO perfil_plataforma_acesso (perfil_plataforma_tipo_id, acesso_id)
SELECT 1, acesso_id
FROM acesso
ON CONFLICT DO NOTHING;

/* Suporte da Plataforma. */
INSERT INTO perfil_plataforma_acesso (perfil_plataforma_tipo_id, acesso_id)
SELECT 2, acesso_id
FROM acesso
WHERE codigo = ANY (ARRAY[
    'identidade.contratante.visualizar',
    'identidade.impersonacao.iniciar',
    'identidade.impersonacao.visualizar_log',
    'identidade.historico_senha.visualizar',
    'identidade.chamado.visualizar_todos',
    'identidade.chamado.responder',
    'identidade.chamado.fechar',
    'identidade.categoria_chamado.gerenciar'
])
ON CONFLICT DO NOTHING;

/* Dono do Salão: tudo, exceto administração exclusiva da plataforma. */
INSERT INTO perfil_acesso (perfil_id, acesso_id)
SELECT '00000000-0000-0000-0000-000000000003', acesso_id
FROM acesso
WHERE codigo NOT LIKE 'identidade.contratante.suspender'
  AND codigo NOT LIKE 'identidade.contratante.reativar'
  AND codigo NOT LIKE 'identidade.configuracao_global.%'
  AND codigo NOT LIKE 'identidade.acesso.%'
  AND codigo NOT LIKE 'identidade.impersonacao.%'
  AND codigo <> 'identidade.chamado.visualizar_todos'
  AND codigo <> 'identidade.categoria_chamado.gerenciar'
ON CONFLICT DO NOTHING;

/* Gerente / Recepção. */
INSERT INTO perfil_acesso (perfil_id, acesso_id)
SELECT '00000000-0000-0000-0000-000000000004', acesso_id
FROM acesso
WHERE codigo = ANY (ARRAY[
    'identidade.cliente.criar', 'identidade.cliente.editar', 'identidade.cliente.visualizar',
    'identidade.chamado.abrir', 'identidade.chamado.visualizar', 'identidade.chamado.responder',
    'agendamento.servico.visualizar', 'agendamento.pacote.visualizar',
    'agendamento.cliente_pacote.vender', 'agendamento.cliente_pacote.visualizar',
    'agendamento.cliente_pacote.cancelar',
    'agendamento.agendamento.criar', 'agendamento.agendamento.visualizar',
    'agendamento.agendamento.editar', 'agendamento.agendamento.confirmar',
    'agendamento.agendamento.remarcar', 'agendamento.agendamento.cancelar',
    'agendamento.agendamento.finalizar', 'agendamento.agendamento.aplicar_desconto',
    'agendamento.pagamento.registrar', 'agendamento.pagamento.visualizar',
    'upload.arquivo.enviar', 'upload.arquivo.visualizar'
])
ON CONFLICT DO NOTHING;

/* Financeiro. */
INSERT INTO perfil_acesso (perfil_id, acesso_id)
SELECT '00000000-0000-0000-0000-000000000005', acesso_id
FROM acesso
WHERE codigo = ANY (ARRAY[
    'agendamento.forma_pagamento.gerenciar',
    'agendamento.pagamento.registrar', 'agendamento.pagamento.visualizar',
    'agendamento.historico_pagamento.visualizar',
    'agendamento.relatorio_financeiro.visualizar',
    'agendamento.cliente_pacote.visualizar'
])
ON CONFLICT DO NOTHING;

/* Profissional. Ownership restringe as operações ao próprio profissional. */
INSERT INTO perfil_acesso (perfil_id, acesso_id)
SELECT '00000000-0000-0000-0000-000000000006', acesso_id
FROM acesso
WHERE codigo = ANY (ARRAY[
    'agendamento.servico.visualizar',
    'agendamento.agendamento.visualizar',
    'agendamento.agendamento.confirmar',
    'agendamento.agendamento.finalizar',
    'agendamento.avaliacao.visualizar',
    'identidade.funcionario_portfolio.gerenciar',
    'upload.arquivo.enviar', 'upload.arquivo.visualizar'
])
ON CONFLICT DO NOTHING;

/* Cliente não recebe acessos administrativos; usa apenas ownership. */
