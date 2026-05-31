-- Legacy records loaded from massa_dados.txt.
-- CPFs in this dataset do not pass check-digit validation and are inserted
-- directly here, bypassing application-level validation intentionally.
-- Address fields are NULL because this data predates the address requirement.

INSERT INTO persons (full_name, document, email, date_of_birth, login, created_at) VALUES
    ('Maria Silva Santos',   '12345678901', 'maria.silva.santos1@exemplo.com',  '1998-03-14', 'mariasi',  NOW()),
    ('Maria Simoes Andrade', '12345678902', 'maria.simoes.andrade@exemplo.com',  '1999-07-22', 'mariasa',  NOW()),
    ('Maria Silva Soares',   '12345678903', 'maria.silva.soares@exemplo.com',   '2000-01-10', 'mariass',  NOW()),
    ('Maria Siqueira Lima',  '12345678904', 'maria.siqueira.lima@exemplo.com',  '1997-11-05', 'mariasl',  NOW()),
    ('Joao Pedro Alves',     '12345678905', 'joao.pedro.alves@exemplo.com',     '1998-05-09', 'joaoped',  NOW()),
    ('Joao Pedrosa Lima',    '12345678906', 'joao.pedrosa.lima@exemplo.com',    '2001-09-18', 'joaopel',  NOW()),
    ('Joao Pedro Antunes',   '12345678907', 'joao.pedro.antunes@exemplo.com',   '1996-12-01', 'joaoant',  NOW()),
    ('Joao Pedreira Costa',  '12345678908', 'joao.pedreira.costa@exemplo.com',  '1999-04-27', 'joaocos',  NOW()),
    ('Ana Clara Souza',      '12345678909', 'ana.clara.souza@exemplo.com',      '2000-08-13', 'anaclar',  NOW()),
    ('Ana Claudia Rocha',    '12345678910', 'ana.claudia.rocha@exemplo.com',    '1998-02-16', 'anaclau',  NOW()),
    ('Ana Clara Lima',       '12345678911', 'ana.clara.lima@exemplo.com',       '1997-06-30', 'anaclal',  NOW()),
    ('Ana Clarice Melo',     '12345678912', 'ana.clarice.melo@exemplo.com',     '2002-10-02', 'anaclam',  NOW()),
    ('Carlos Eduardo Lima',  '12345678913', 'carlos.eduardo.lima@exemplo.com',  '1995-03-11', 'carlose',  NOW()),
    ('Carlos Emanuel Rosa',  '12345678914', 'carlos.emanuel.rosa@exemplo.com',  '1996-07-20', 'carlosr',  NOW()),
    ('Carlos Ernesto Dias',  '12345678915', 'carlos.ernesto.dias@exemplo.com',  '1994-12-29', 'carlosd',  NOW()),
    ('Carlos Esteves Prado', '12345678916', 'carlos.esteves.prado@exemplo.com', '2001-01-06', 'carlosp',  NOW()),
    ('Paula Fernanda Reis',  '12345678917', 'paula.fernanda.reis@exemplo.com',  '1998-09-09', 'paulafe',  NOW()),
    ('Paula Ferreira Nunes', '12345678918', 'paula.ferreira.nunes@exemplo.com', '1999-11-23', 'paulafn',  NOW()),
    ('Paula Fernanda Lima',  '12345678919', 'paula.fernanda.lima@exemplo.com',  '1997-04-04', 'paulafl',  NOW()),
    ('Lucas Henrique Prado', '12345678920', 'lucas.henrique.prado@exemplo.com', '2000-05-17', 'lucashe',  NOW());
