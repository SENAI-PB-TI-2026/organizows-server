# Quality Gate docente — organizows-server

Este fluxo de CI é mantido pelo professor, não é uma atividade avaliativa e não exige testes unitários, de integração, cobertura ou deploy. Não altera as versões de dependências do projeto.

- **Dependency Review**: novas dependências vulneráveis de severidade alta/crítica falham; depende do dependency graph habilitado.
- **Secret Scan (Gitleaks)**: analisa novos commits com redação integral; execução manual examina o histórico. Credenciais reais devem ser revogadas.
- **CodeQL**: analisa workflows do GitHub Actions e Java 25 com compilação manual. Configure proteção de merge de CodeQL (alta ou superior) depois de validar a execução. Não use simultaneamente default setup e advanced setup.
- **Build e inicialização**: gera JAR sem executar ou compilar testes e inicia em H2 temporário, verificando HTTP. Não é homologação de regras de negócio nem de PostgreSQL.
- **PMD/CPD e Checkstyle**: análises de qualidade informativas; relatórios disponíveis como artefatos. Não exigir estes checks como condição de merge nesta fase.

- **Dependabot**: configuração de atualizações de Actions para develop; precisa chegar à main para ser reconhecida. Habilitar alertas e atualizações de segurança nativas nas configurações do GitHub; atualizações de versão Maven permanecem desativadas para preservar as versões didáticas.
- **CODEOWNERS**: arquivos de CI e instruções sob revisão docente. Exigir code owner review no ruleset existente de main/develop; confirmar acesso de escrita do proprietário e manter revisão cruzada.

## Ativação administrativa (após a homologação)

1. Habilitar Actions, dependency graph, Dependabot alerts/security updates e secret scanning/push protection.
2. Validar cada workflow em PR; investigar falhas de execução, mapear alertas preexistentes e corrigir separadamente.
3. Preservar proteções vigentes. Exigir os checks de Dependency Review, Secret Scan e Build e inicializacao; exigir análise CodeQL com alertas de segurança altos ou críticos, revisar a baseline e manter PMD/Checkstyle informativos.
4. Exigir aprovação do proprietário para mudanças do CI, aprovação da última alteração por outro revisor e restringir bypass ao professor/admin autorizado. Não exigir checks que ainda não estejam disponíveis na branch.
5. Não usar segredos de produção, `pull_request_target` para executar código de PR, permissões de escrita desnecessárias ou auto-merge. A aplicação não deve ser exposta externamente nesta etapa.

## SonarQube Cloud (fase diagnóstica)

Análise estática docente e não avaliativa: manutenibilidade, confiabilidade, segurança, hotspots, complexidade, duplicação e dívida técnica. Mantém todos os checks anteriores. A cobertura está temporariamente excluída de toda a base (`sonar.coverage.exclusions=**/*`); não adiciona JaCoCo, não executa testes e não modifica o POM. A análise não espera nem torna obrigatório o Quality Gate.

### Ativação (somente pelo docente)

1. No SonarQube Cloud, vincule a organização GitHub `SENAI-PB-TI-2026` ao plano OSS se elegível e importe este repositório. Selecione análise por CI, desativando Automatic Analysis. Configure `develop` como branch de longa duração **antes da primeira análise**; mantenha `main` como referência e estabeleça as baselines de main e develop antes de ativar PRs (a primeira análise pode exigir a inicialização da main pelo tutorial do Sonar).
2. Crie um Quality Gate pedagógico sem condição de cobertura; mantenha-o informativo, inclusive quando vermelho. Não o configure como check obrigatório nem altere as proteções existentes.
3. No GitHub, Settings > Environments, crie `sonar-docente`. Configure Required reviewers (somente docentes autorizados), restrinja as branches de deployment a `develop` e `main`; não permita bypass indiscriminado. Proteja essas branches contra pushes não revisados.
4. **Somente neste environment**, crie as variáveis `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` com as chaves reais do SonarQube Cloud e o secret `SONAR_TOKEN` com uma credencial de análise de privilégio mínimo (conta dedicada, sem permissões administrativas, revogável/rotacionável). Nunca coloque token em secrets de repositório/organização acessíveis a workflows dos alunos ou em arquivos, argumentos da CLI ou logs.
5. Após configurar as proteções, crie a variável de repositório `SONAR_ENABLED=true` (Settings > Secrets and variables > Actions > Variables). Em push aprovado para develop/main, o workflow compila com Java 25 e publica a análise usando SonarScanner Maven fixado. A aprovação do environment é obrigatória antes de disponibilizar o secret. Sem a variável, o job fica ignorado; isso não significa que a análise tenha sido homologada.
6. Opcionalmente, após estabelecer a baseline e conferir as permissões, permita refs de PR (`refs/pull/*/merge`) nas regras do environment e crie a variável de repositório `SONAR_PR_ENABLED=true`. Antes de aprovar **cada** execução de PR, o docente deve revisar as mudanças de workflow, Maven Wrapper, POM, extensões/plugins e todo código que possa executar no build; não aprove conteúdo não confiável. PRs de forks são ignorados; nunca use `pull_request_target` para analisar código não confiável. Se não for possível manter essa aprovação, deixe a variável desativada e analise apenas as branches revisadas.
7. Confirme no SonarQube Cloud a publicação da análise, identificação da branch, indicadores e decoração dos PRs. Após autorização pedagógica futura, retire a exclusão temporária de cobertura, integre JaCoCo e revise metas progressivas de Quality Gate para código novo.

Referências: https://docs.sonarsource.com/sonarqube-cloud/ e https://docs.github.com/en/actions/deployment/targeting-different-environments/managing-environments-for-deployment.
