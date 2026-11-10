# Botton — meu ateliê

Controle pessoal de insumos e demandas de clientes em português. Backend Java 21 / Spring Boot 3.5.16, banco MySQL 8.4 e frontend JavaScript, HTML e CSS. O Java serve a interface e a API na mesma origem.

## Executar no computador

Instale e abra o Docker Desktop com suporte a Docker Compose. Na pasta deste projeto:

Atalho: no PowerShell 7, execute `./Iniciar.ps1`. O script cria senhas aleatórias locais na primeira execução e inicia os serviços. Ou siga os passos manuais abaixo:

1. Copie `.env.example` para `.env` e substitua as duas senhas por valores diferentes e longos.
2. Execute `docker compose up --build -d`.
3. Aguarde o banco iniciar e abra http://localhost:8080.

Para acompanhar a inicialização: `docker compose logs -f app`. Para parar: `docker compose down`. Os dados permanecem no volume `mysql-data`. Não use `docker compose down -v` se quiser preservar os dados. Faça backup do banco antes de atualizar ou remover volumes.

A porta da aplicação está limitada ao próprio computador; o MySQL não expõe uma porta externa. Esta versão é pessoal/local, sem login. Antes de disponibilizar pela internet, acrescente autenticação, HTTPS e proteção das rotas. Não há armazenamento de dados do negócio no navegador.

## Usar

1. Cadastre cada insumo com unidade, quantidade atual e limites mínimo/máximo (por exemplo: chapa 58 mm, verso com alfinete e película).
2. Crie o pedido com cliente, modelo, quantidade e prazo. Informe quantas unidades de cada insumo são consumidas por botton; 0 exclui o insumo da composição.
3. As demandas abertas reservam materiais. **Disponível = estoque físico − reservado**.
4. Disponível negativo gera falta para pedidos; abaixo do mínimo gera estoque baixo; acima do máximo gera excesso. Os limites exatos são considerados equilibrados. A reposição sugerida cobre pedidos abertos e recompõe o mínimo.
5. Use Movimentar para entradas positivas e saídas negativas. Editar permite corrigir o saldo físico e alterar limites.
6. Concluir desconta todos os insumos em uma única transação. Se faltar algum, nada é descontado. Cancelar libera a reserva sem alterar o estoque físico. Pedidos encerrados não podem ser concluídos novamente.

Os alertas aparecem dentro do painel; não são notificações por e-mail ou do sistema operacional. Quantidades são inteiras. Para materiais fracionados, cadastre uma unidade menor inteira, como mililitros ou centímetros. A composição é registrada por pedido, sem catálogo de receitas fixas.

## Desenvolvimento

Com Java 21+ e Maven 3.6.3+, configure `DB_URL`, `DB_USER` e `DB_PASSWORD` e execute `mvn spring-boot:run`. O schema inicial é criado automaticamente. Para compilar e testar, execute `mvn package`; o JAR fica em `target/botton-1.0.0.jar`.

Testes automatizados usam H2 em modo MySQL, exclusivamente no escopo de teste. O ambiente real usa o driver MySQL. A suíte verifica reservas, falta, excesso, cancelamento, baixa única e rollback quando falta um dos materiais. H2 não substitui a validação final em MySQL real.

API: `GET /api/state`, `POST /api/supplies`, `PUT /api/supplies/{id}`, `POST /api/supplies/{id}/movement`, `POST /api/orders`, `POST /api/orders/{id}/complete` e `POST /api/orders/{id}/cancel`. Os corpos JSON correspondem aos records em `Api.java`. A interface usa essas mesmas rotas.

## Sites

O runtime de hospedagem do Sites é Cloudflare Workers e não executa este servidor Java ou MySQL. O projeto completo é entregue para execução local. Para hospedar o frontend no Sites mantendo a stack solicitada, é necessário disponibilizar a API Java com MySQL em um servidor separado e configurar a integração/autenticação. Nenhum frontend desconectado foi publicado como se fosse um sistema operacional.

Fontes tipográficas opcionais vêm do Google Fonts; sem conexão, a interface usa fontes locais. O recurso experimental WebMCP registra uma consulta somente de leitura quando o navegador oferece suporte.
