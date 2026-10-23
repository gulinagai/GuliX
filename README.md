GuliX — API de E-commerce de Hardware

<p align="center">
  <strong>API REST para gerenciamento de um e-commerce de hardware</strong><br>
  Java • Spring Boot • Spring Security • JWT • MySQL • Docker
</p>

<p align="center">
  <a href="https://github.com/gulinagai/projeto-GuliX/actions"><img alt="CI" src="https://github.com/gulinagai/projeto-GuliX/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Java 25" src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot 4" src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white">
  <img alt="MySQL 8.4" src="https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white">
  <img alt="Docker Compose" src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white">
</p>

O GuliX é uma API REST para um e-commerce de hardware, desenvolvida com Java e Spring Boot. A aplicação contempla os principais fluxos de uma loja virtual, incluindo autenticação e autorização, catálogo de produtos, carrinho, pedidos, pagamentos, controle de estoque, endereços e cálculo de frete.

A solução foi estruturada com separação de responsabilidades, regras de negócio organizadas em serviços, persistência relacional, documentação interativa, testes automatizados e integração contínua. O objetivo é demonstrar a construção de um backend organizado, com atenção a segurança, manutenibilidade e integração com serviços externos.

Índice

Funcionalidades

Tecnologias

Arquitetura e organização

Segurança

Integrações externas

Banco de dados

Pré-requisitos

Como executar com Docker

Como executar localmente

Documentação da API

Testes e qualidade

CI — Integração contínua

Configuração e segurança de ambiente

Regras de negócio

Escopo e evolução

Autor

Funcionalidades

Autenticação e usuários

Cadastro e login de usuários.

Autenticação baseada em JWT.

Fluxo de renovação de tokens (refresh token).

Logout e gerenciamento de sessão por tokens persistidos.

Controle de acesso por permissões/roles.

Senhas armazenadas com hash, sem persistir senhas em texto puro.

Endpoints de consulta, atualização e exclusão de usuários.

Catálogo de produtos

Operações de consulta e manutenção de produtos.

Categorias e marcas.

Filtros de consulta e paginação com Spring Data JPA e Specification.

Suporte a informações comerciais, como preço e descontos, conforme as regras de domínio.

Carrinho e pedidos

Consulta do carrinho.

Inclusão, alteração de quantidade e remoção de itens.

Criação e consulta de pedidos.

Histórico de pedidos do usuário.

Cancelamento de pedidos, sujeito às regras de negócio.

Consulta administrativa e atualização de status do pedido.

Persistência dos dados necessários ao pedido e ao endereço de entrega.

Estoque

Consulta de estoque por produto.

Registro de entrada e saída.

Atualização de inventário.

Separação entre quantidade total e quantidade reservada, apoiando o controle de disponibilidade.

Endereços e estrutura geográfica

Cadastro e manutenção de endereços.

Definição de endereço principal.

Estados e cidades como cadastros próprios.

Armazenamento de coordenadas geográficas para apoiar o cálculo de frete.

Cadastro de empresas e filiais.

Pagamentos e frete

Integração de checkout com Stripe.

Consulta de informações de pagamento.

Regras para cálculo de descontos e parcelamento implementadas no domínio de pagamento.

Cálculo de frete com base em política por estado, distância estimada e valor por quilômetro.

Integração com OpenRouteService para obtenção de informações de rota/distância.

Cadastro e consulta de políticas de frete.

Tratamento de erros e validação

Validação de dados de entrada com Jakarta Bean Validation.

Tratamento centralizado de exceções.

Respostas de erro padronizadas por meio de ErrorResponseDTO.

Configuração de respostas de autenticação e autorização para requisições não autorizadas ou proibidas.

Tecnologias

Área

Tecnologias

Linguagem

Java 25

Framework

Spring Boot 4.0.5, Spring MVC

Persistência

Spring Data JPA, Hibernate

Banco de dados

MySQL 8.4

Segurança

Spring Security, JWT (JJWT)

Validação

Spring Boot Validation / Jakarta Validation

Mapeamento

MapStruct, Lombok

Documentação

Springdoc OpenAPI, Swagger UI

Pagamentos

Stripe Java SDK

Rotas e distância

OpenRouteService

Build

Maven Wrapper

Contêineres

Docker, Docker Compose

Testes

JUnit, Spring Boot Test, Spring Security Test, H2, Testcontainers (dependências disponíveis)

CI

GitHub Actions

Arquitetura e organização

O backend segue uma organização em camadas para manter separadas as responsabilidades de entrada HTTP, regras de negócio e acesso a dados:

Controller: recebe requisições REST e define os contratos HTTP.

Service: concentra operações e regras de negócio.

Repository: abstrai o acesso aos dados por meio do Spring Data JPA.

Entity: representa o modelo persistido.

DTO: define os dados de entrada e saída da API, evitando expor diretamente as entidades nos contratos públicos.

Config / Security: reúne configurações da aplicação, integrações, autenticação e autorização.

Tratamento de erros: centraliza a conversão de falhas em respostas consistentes.

Estrutura simplificada do repositório:

projeto-GuliX/
├── .github/
│   └── workflows/
│       └── ci.yml
├── GuliX/
│   ├── backend/
│   │   ├── src/
│   │   │   ├── main/java/guli/gulix/backend/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   └── service/
│   │   │   └── test/java/
│   │   ├── Dockerfile
│   │   ├── docker-compose.yml
│   │   ├── .env.example
│   │   ├── pom.xml
│   │   └── mvnw
│   └── docs/
│       ├── business-rules.md
│       └── der-gulix.png
└── README.md

A árvore acima é uma visão resumida das áreas principais; alguns nomes de pacotes e arquivos podem variar conforme a evolução do código.

Segurança

A API utiliza Spring Security e tokens JWT para proteger recursos e aplicar autorização. O projeto também define handlers específicos para falhas de autenticação e autorização e configura CORS.

A implementação contempla a separação entre endpoints públicos e protegidos, validação de dados, armazenamento de senhas com hash, configuração de credenciais por variáveis de ambiente e respostas padronizadas para erros de autenticação e autorização.

A execução em produção exige configuração própria do ambiente, incluindo HTTPS, segredos fortes, CORS restrito aos domínios autorizados, gestão de credenciais e políticas de operação adequadas. As credenciais de exemplo são destinadas exclusivamente ao desenvolvimento local.

Integrações externas

Stripe

Responsável pelo fluxo de checkout/pagamento. Para executar os fluxos integrados, é necessário configurar a chave secreta e, quando aplicável, o segredo de webhook da sua conta Stripe. Use credenciais de teste durante o desenvolvimento.

OpenRouteService

Utilizado para obter dados de rota/distância que alimentam o cálculo de frete. É necessário configurar uma chave de API válida e considerar os limites de uso do serviço.

Essas integrações dependem de credenciais externas e de conectividade; a execução local da aplicação não garante que os fluxos de pagamento ou roteamento funcionem sem essa configuração.

Banco de dados

O projeto utiliza MySQL e persiste os dados por meio de Spring Data JPA/Hibernate. O modelo contempla, entre outros, usuários, endereços, catálogo, categorias, marcas, carrinho, pedidos, pagamentos, estoque, empresas, filiais, estados, cidades e políticas de frete.

Diagrama Entidade-Relacionamento



As regras de negócio documentadas estão em GuliX/docs/business-rules.md.

Pré-requisitos

Para executar localmente:

Git.

JDK 25.

Docker e Docker Compose (para a opção conteinerizada).

Credenciais de teste do Stripe e chave do OpenRouteService para testar os fluxos que dependem desses serviços.

O Maven Wrapper (mvnw / mvnw.cmd) está incluído no repositório; não é necessário instalar o Maven separadamente.

Como executar com Docker

1. Clone o repositório

git clone https://github.com/gulinagai/projeto-GuliX.git
cd projeto-GuliX/GuliX/backend

2. Configure as variáveis de ambiente

Copie o arquivo de exemplo e edite os valores:

Linux/macOS/Git Bash:

cp .env.example .env

PowerShell:

Copy-Item .env.example .env

Preencha no .env as variáveis exigidas pelo docker-compose.yml, incluindo as credenciais de integração e a expiração do refresh token. Exemplo de variáveis esperadas:

MYSQL_ROOT_PASSWORD=defina_uma_senha_local
MYSQL_DATABASE=gulix
MYSQL_USER=gulix
MYSQL_PASSWORD=defina_uma_senha_local

DB_URL=jdbc:mysql://mysql:3306/gulix
DB_USERNAME=gulix
DB_PASSWORD=defina_uma_senha_local

JWT_SECRET=use_um_segredo_forte_e_adequado_ao_algoritmo
JWT_EXPIRATION=3600000
REFRESH_TOKEN_EXPIRATION=604800000

STRIPE_SECRET_KEY=sk_test_sua_chave
STRIPE_WEBHOOK_SECRET=whsec_seu_segredo_de_teste
OPEN_ROUTE_SERVICE_KEY=sua_chave_openrouteservice

Use valores compatíveis com sua configuração local. Não envie o arquivo .env ao Git.

3. Suba os serviços

docker compose up --build

O Compose configura o backend na porta 8080 e publica o MySQL na porta 3307 do host. O backend se conecta ao MySQL pelo nome do serviço mysql na rede do Compose.

Quando a aplicação estiver pronta, acesse a documentação em http://localhost:8080/swagger-ui/index.html.

Para encerrar os serviços:

docker compose down

Para remover também o volume persistente do banco (isso apaga os dados locais):

docker compose down -v

Como executar localmente

Se preferir executar o backend fora do Docker, disponibilize uma instância MySQL acessível pela sua máquina e configure as variáveis de ambiente utilizadas em application.properties:

DB_URL

DB_USERNAME

DB_PASSWORD

JWT_SECRET

JWT_EXPIRATION

REFRESH_TOKEN_EXPIRATION

STRIPE_SECRET_KEY

STRIPE_WEBHOOK_SECRET

OPEN_ROUTE_SERVICE_KEY

Exemplo para iniciar o backend no Linux/macOS:

cd GuliX/backend
./mvnw spring-boot:run

No Windows, utilize:

cd GuliX/backend
.\mvnw.cmd spring-boot:run

O Spring configura spring.jpa.hibernate.ddl-auto=update no perfil atual. Para ambientes de produção, recomenda-se substituir a atualização automática do esquema por migrações versionadas, por exemplo com Flyway ou Liquibase.

Documentação da API

Com a aplicação em execução, a interface interativa Swagger UI fica disponível em:

Swagger UI: http://localhost:8080/swagger-ui/index.html

Documento OpenAPI: http://localhost:8080/v3/api-docs

A API agrupa recursos de autenticação, usuários, produtos, categorias, marcas, carrinho, pedidos, pagamentos, estoque, endereços, empresas, filiais, estados, cidades e políticas de frete. Consulte o Swagger para visualizar os caminhos, parâmetros, contratos DTO, permissões e respostas definidos no código atual.

Testes e qualidade

A suíte de testes automatizados está organizada por camada e recurso, incluindo testes de controllers, segurança, serviços e repositórios. Fixtures reutilizáveis apoiam a preparação dos cenários de teste.

Execute a suíte de testes e a verificação do build com:

cd GuliX/backend
./mvnw clean verify

No Windows:

cd GuliX/backend
.\mvnw.cmd clean verify

O comando compila a aplicação, executa os testes configurados e verifica o resultado do build. A dependência H2 está disponível para testes que utilizam banco em memória; as dependências do Testcontainers também estão declaradas para cenários de integração com MySQL em contêiner, quando aplicáveis.

CI — Integração contínua

O workflow em .github/workflows/ci.yml é executado em pushes e em pull requests direcionados à branch main. Ele configura Java 25, utiliza cache do Maven e executa:

./mvnw clean verify

O resultado mais recente do pipeline pode ser consultado na aba Actions do repositório: GitHub Actions.

Configuração e segurança de ambiente

Nunca versione .env, tokens, senhas ou chaves privadas.

Mantenha .env.example com nomes de variáveis e valores fictícios, sem credenciais válidas.

Em produção, utilize um gerenciador de segredos e credenciais diferentes das de desenvolvimento.

Configure CORS para os domínios realmente autorizados.

Ative HTTPS e monitore erros e integrações externas.

Avalie migrações de banco, logs estruturados, métricas, health checks e limites de requisição antes do deploy.

Regras de negócio

As regras de negócio conhecidas e documentadas devem ser consultadas em business-rules.md. Entre os pontos do domínio estão o cálculo de frete, o controle de estoque disponível/reservado, o fluxo de pedidos e as condições de pagamento e parcelamento.

Escopo e evolução

O GuliX reúne os principais fluxos de uma API de e-commerce em um escopo definido: autenticação, catálogo, carrinho, pedidos, estoque, pagamentos e logística. A implementação é acompanhada de documentação técnica, testes automatizados e integração contínua.

Como em qualquer aplicação de software, o projeto pode receber evoluções adicionais conforme novos requisitos sejam definidos. Entre as possibilidades estão o aprofundamento da observabilidade, a adoção de migrações versionadas de banco de dados e a automação de implantação. Esses itens são oportunidades de evolução, não funcionalidades anunciadas como já implementadas.

Autor

Gustavo Nagai Amorim

GitHub: @gulinagai

LinkedIn: gustavo-nagai

<p align="center">Projeto autoral de portfólio, com foco em desenvolvimento backend e boas práticas de engenharia de software.</p>