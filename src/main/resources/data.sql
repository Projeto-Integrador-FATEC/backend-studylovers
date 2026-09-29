INSERT INTO users (name, email, password, role)
VALUES ('Daniel', 'daniel@gmail.com', 'Daniel@Admin27!', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Ruan', 'ruan@gmail.com', 'Ruan@Admin58!', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Dornelas', 'dornelas@gmail.com', 'Dornelas@Admin41!', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Gustavo', 'gustavo@gmail.com', 'Gustavo@Admin73!', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Alexandre', 'alexandre@gmail.com', 'Alexandre@Admin36!', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Felipe', 'felipe@gmail.com', 'Felipe@Admin92!', 'ADMIN');

INSERT INTO categorias (id, nome) VALUES (1,'Tecnologia & Programação');
INSERT INTO categorias (id, nome) VALUES (2,'Negócios & Empreendedorismo');
INSERT INTO categorias (id, nome) VALUES (3,'Criatividade & Design');
INSERT INTO categorias (id, nome) VALUES (4,'Carreira & Desenvolvimento Pessoal');
INSERT INTO categorias (id, nome) VALUES (5,'Bem-estar & Lifestyle');

INSERT INTO cursos
(nome, descricao, carga_horaria, nivel, provider, url, categoria_id, imagem_path)
VALUES (
           'Curso de Java Básico',
           'Este curso ensina os fundamentos da linguagem Java, cobrindo sintaxe, estruturas de controle e introdução à orientação a objetos.',
           40,
           'Iniciante',
           'Alura',
           'https://www.alura.com.br/curso-java-basico',
           1,
           '1763332369550_Curso-de-programação.webp'
       );


INSERT INTO cursos
(nome, descricao, carga_horaria, nivel, provider, url, categoria_id, imagem_path)
VALUES (
           'Empreendedorismo na Prática',
           'Aprenda conceitos fundamentais para criar, validar e gerenciar seu próprio negócio.',
           30,
           'Intermediário',
           'Sebrae',
           'https://sebrae.com.br/empreendedorismo-na-pratica',
           2,
           'empreendedorismo.jpeg'
       );

INSERT INTO cursos
(nome, descricao, carga_horaria, nivel, provider, url, categoria_id, imagem_path)
VALUES (
           'Design Gráfico para Iniciantes',
           'Introdução a conceitos visuais, tipografia, cores e criação de layouts utilizando ferramentas de design.',
           25,
           'Iniciante',
           'Domestika',
           'https://domestika.org/design-grafico-basico',
           3,
           'design-grafico.jpg'
       );

INSERT INTO cursos
(nome, descricao, carga_horaria, nivel, provider, url, categoria_id, imagem_path)
VALUES (
           'Comunicação Profissional e Oratória',
           'Aprenda a falar com clareza, confiança e assertividade em ambientes profissionais.',
           15,
           'Iniciante',
           'Udemy',
           'https://udemy.com/comunicacao-oratoria',
           4,
           'comunicacao.webp'
       );






