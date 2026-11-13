package br.senai.organizows.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:organizows}")
    private String applicationName;

    @Value("${application.version:1.0.0}")
    private String applicationVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrganizoWS - API Backend")
                        .version(applicationVersion)
                        .description("""
                                ## Documentação do Contrato OpenAPI - OrganizoWS (Sprint 0)
                                
                                Esta API fornece os contratos de integração de backend para o ecossistema OrganizoWS, estruturada para que os times possam integrar seus módulos nas sprints futuras de maneira padronizada e consistente.
                                
                                ### Convenções e Padrões da API:
                                
                                1. **Padrão de Respostas de Sucesso:**
                                   - As respostas utilizam o envelope padrão `ApiResponse<T>`, contendo:
                                     - `success` (boolean): indica o sucesso da operação.
                                     - `message` (string): mensagem descritiva da operação.
                                     - `data` (objeto/lista): corpo com os dados retornados.
                                     - `timestamp` (data/hora): momento da geração da resposta.
                                
                                2. **Padrão de Respostas de Erro:**
                                   - Erros de cliente (4xx) e servidor (5xx) utilizam a estrutura padrão `ApiErrorResponse`:
                                     - `status` (int): código HTTP (ex: 400, 404, 500).
                                     - `error` (string): razão do erro HTTP.
                                     - `message` (string): mensagem explicativa legível.
                                     - `path` (string): endpoint acessado.
                                     - `timestamp` (data/hora): data e hora da ocorrência.
                                     - `fieldErrors` (lista opcional): detalhes de campos com falha de validação.
                                
                                3. **Códigos de Status HTTP Convencionados:**
                                   - `200 OK`: Consulta ou operação realizada com sucesso.
                                   - `201 Created`: Novo recurso criado com sucesso.
                                   - `204 No Content`: Operação executada sem conteúdo no corpo de resposta.
                                   - `400 Bad Request`: Requisição inválida ou dados incorretos.
                                   - `404 Not Found`: Recurso não localizado.
                                   - `500 Internal Server Error`: Erro interno inesperado no servidor.
                                
                                4. **Endpoint Técnico de Verificação:**
                                   - `/api/v1/status`: Permite monitorar a saúde da aplicação, versão e tempo de atividade.
                                """)
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento OrganizoWS")
                                .email("dev@organizows.senai.br"))
                        .license(new License()
                                .name("Uso Interno SENAI")
                                .url("https://www.senai.br")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Ambiente Local de Desenvolvimento")
                ));
    }
}
