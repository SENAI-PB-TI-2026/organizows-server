# SCRUM-86 · Validação manual da hierarquia de localizações

[Cartão no Jira](https://team-senai.atlassian.net/browse/SCRUM-86)

Verificação realizada em 25/11/2026 sobre a `develop` no commit `4267531`, com o perfil `dev`, H2 em memória e as migrações do projeto. A API persistiu e consultou as localizações pelo fluxo REST e JPA existente.

```powershell
.\mvnw.cmd -B spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=18086"
```

Envie as requisições na ordem abaixo para `http://127.0.0.1:18086`. Use `Content-Type: application/json` para POST e PUT. Substitua os IDs pelos valores devolvidos pelo servidor se a base já contiver registros.

| Requisição | Corpo JSON enviado | Resultado esperado e observado |
| --- | --- | --- |
| `POST /api/v1/localizacoes` | `{"codigo":"SCRUM86-PAI","nome":"Almoxarifado"}` | 201. Criou a localização raiz com ID 1 e `localPaiId: null`. |
| `POST /api/v1/localizacoes` | `{"codigo":"SCRUM86-FILHA","nome":"Prateleira","localPaiId":1}` | 201. Criou a localização filha com ID 2, `localPaiId: 1` e endereço `Almoxarifado > Prateleira`. |
| `POST /api/v1/localizacoes` | `{"codigo":"SCRUM86-INVALIDA","nome":"Invalida","localPaiId":999999}` | 404. A mensagem informou que a localização pai com ID 999999 não foi encontrada. |
| `PUT /api/v1/localizacoes/2` | `{"codigo":"SCRUM86-FILHA","nome":"Prateleira","localPaiId":2}` | 400. A resposta informou que uma localização não pode ter a si mesma como pai. |
| `PUT /api/v1/localizacoes/1` | `{"codigo":"SCRUM86-PAI","nome":"Almoxarifado","localPaiId":2}` | 400. A resposta informou que foi detectado um ciclo na hierarquia. |
| `GET /api/v1/localizacoes/1` | — | 200. A localização raiz manteve `localPaiId: null`. |
| `GET /api/v1/localizacoes/2` | — | 200. A filha manteve `localPaiId: 1` e o endereço `Almoxarifado > Prateleira`. |
