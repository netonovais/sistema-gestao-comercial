-- Usuário ADMIN padrão
-- Senha: admin123 (BCrypt)
INSERT INTO usuarios (username, senha, perfil)
SELECT 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

-- Usuário FUNCIONARIO padrão
-- Senha: func123 (BCrypt)
INSERT INTO usuarios (username, senha, perfil)
SELECT 'funcionario', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B9tVHTa', 'FUNCIONARIO'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'funcionario');
