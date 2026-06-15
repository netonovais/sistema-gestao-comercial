-- Usuário ADMIN padrão
-- Senha: admin123
INSERT INTO usuarios (username, senha, perfil)
SELECT 'admin', '$2b$10$fOXe2NIeovlT1zwarJsmheu/yB/W5Whq.zbHFKoUJYIFGDth2reFu', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

-- Usuário FUNCIONARIO padrão
-- Senha: func123
INSERT INTO usuarios (username, senha, perfil)
SELECT 'funcionario', '$2b$10$emKWuT9GHULgk.bim/l3nuj5ES0CD7gFFvL25cy1FTSoiUKr.KV42', 'FUNCIONARIO'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'funcionario');