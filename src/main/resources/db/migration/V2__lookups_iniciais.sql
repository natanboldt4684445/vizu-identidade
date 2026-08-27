SET search_path TO identidade, public;

INSERT INTO perfil_plataforma_tipo
    (perfil_plataforma_tipo_id, codigo, nome, descricao, ativo, ordem)
VALUES
    (1, 'ADMINISTRADOR', 'Administrador', 'Acesso integral da equipe interna da Vizu.', TRUE, 10),
    (2, 'SUPORTE', 'Suporte', 'Atendimento e diagnóstico sem poderes administrativos integrais.', TRUE, 20)
ON CONFLICT (codigo) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    ativo = EXCLUDED.ativo,
    ordem = EXCLUDED.ordem,
    atualizado_em = NOW();

INSERT INTO provedor_identidade
    (provedor_identidade_id, codigo, nome, descricao, ativo, ordem)
VALUES
    (1, 'GOOGLE', 'Google', 'Identidade federada pelo Google OIDC.', TRUE, 10)
ON CONFLICT (codigo) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    ativo = EXCLUDED.ativo,
    ordem = EXCLUDED.ordem,
    atualizado_em = NOW();

INSERT INTO status_chamado
    (status_chamado_id, codigo, nome, descricao, terminal, ativo, ordem)
VALUES
    (1, 'ABERTO', 'Aberto', 'Chamado criado e ainda não assumido.', FALSE, TRUE, 10),
    (2, 'EM_ATENDIMENTO', 'Em atendimento', 'Chamado em análise pela equipe.', FALSE, TRUE, 20),
    (3, 'AGUARDANDO_CLIENTE', 'Aguardando cliente', 'Aguardando retorno do solicitante.', FALSE, TRUE, 30),
    (4, 'RESOLVIDO', 'Resolvido', 'Solução apresentada ao solicitante.', FALSE, TRUE, 40),
    (5, 'FECHADO', 'Fechado', 'Chamado encerrado.', TRUE, TRUE, 50)
ON CONFLICT (codigo) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    terminal = EXCLUDED.terminal,
    ativo = EXCLUDED.ativo,
    ordem = EXCLUDED.ordem,
    atualizado_em = NOW();

INSERT INTO categoria_chamado
    (categoria_chamado_id, codigo, nome, descricao, ativo, ordem)
VALUES
    (1, 'DUVIDA', 'Dúvida', 'Dúvida sobre o uso da plataforma.', TRUE, 10),
    (2, 'PROBLEMA_TECNICO', 'Problema técnico', 'Falha ou comportamento inesperado.', TRUE, 20),
    (3, 'FINANCEIRO', 'Financeiro', 'Cobrança, pagamento ou assinatura.', TRUE, 30),
    (4, 'CADASTRO', 'Cadastro', 'Contratante, loja, usuário ou cliente.', TRUE, 40),
    (5, 'OUTRO', 'Outro', 'Assunto não classificado nas categorias anteriores.', TRUE, 90)
ON CONFLICT (codigo) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    ativo = EXCLUDED.ativo,
    ordem = EXCLUDED.ordem,
    atualizado_em = NOW();

SELECT setval(pg_get_serial_sequence('perfil_plataforma_tipo', 'perfil_plataforma_tipo_id'),
              (SELECT MAX(perfil_plataforma_tipo_id) FROM perfil_plataforma_tipo));
SELECT setval(pg_get_serial_sequence('provedor_identidade', 'provedor_identidade_id'),
              (SELECT MAX(provedor_identidade_id) FROM provedor_identidade));
SELECT setval(pg_get_serial_sequence('status_chamado', 'status_chamado_id'),
              (SELECT MAX(status_chamado_id) FROM status_chamado));
SELECT setval(pg_get_serial_sequence('categoria_chamado', 'categoria_chamado_id'),
              (SELECT MAX(categoria_chamado_id) FROM categoria_chamado));
