package br.senai.organizows.controller;

import br.senai.organizows.dto.localizacao.LocalizacaoRequestDTO;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.repository.LocalizacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class LocalizacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    private Localizacao predioSalvo;
    private Localizacao salaSalva;

    @BeforeEach
    void setUp() {
        localizacaoRepository.deleteAll();

        Localizacao predio = Localizacao.builder()
                .codigo("PREDIO-1")
                .nome("Prédio Central")
                .tipo("Edifício")
                .descricao("Prédio principal do campus")
                .build();
        predioSalvo = localizacaoRepository.save(predio);

        Localizacao sala = Localizacao.builder()
                .codigo("SALA-101")
                .nome("Sala 101")
                .tipo("Sala de Aula")
                .descricao("Laboratório de desenvolvimento")
                .localPai(predioSalvo)
                .build();
        salaSalva = localizacaoRepository.save(sala);
    }

    @Test
    @DisplayName("Deve cadastrar nova localização raiz com sucesso (HTTP 201)")
    void deveCadastrarLocalizacaoRaizComSucesso() throws Exception {
        LocalizacaoRequestDTO request = LocalizacaoRequestDTO.builder()
                .codigo("GALPAO-A")
                .nome("Galpão de Logística")
                .tipo("Galpão")
                .descricao("Área de estocagem de insumos")
                .build();

        mockMvc.perform(post("/api/v1/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Localização criada com sucesso"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.codigo").value("GALPAO-A"))
                .andExpect(jsonPath("$.data.nome").value("Galpão de Logística"))
                .andExpect(jsonPath("$.data.localPaiId").doesNotExist())
                .andExpect(jsonPath("$.data.enderecoCompleto").value("Galpão de Logística"));
    }

    @Test
    @DisplayName("Deve cadastrar nova localização filha vinculada a pai existente (HTTP 201)")
    void deveCadastrarLocalizacaoFilhaComSucesso() throws Exception {
        LocalizacaoRequestDTO request = LocalizacaoRequestDTO.builder()
                .codigo("ARM-01")
                .nome("Armário 01")
                .tipo("Armário")
                .localPaiId(salaSalva.getId())
                .build();

        mockMvc.perform(post("/api/v1/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("ARM-01"))
                .andExpect(jsonPath("$.data.localPaiId").value(salaSalva.getId()))
                .andExpect(jsonPath("$.data.localPaiNome").value("Sala 101"))
                .andExpect(jsonPath("$.data.enderecoCompleto").value("Prédio Central > Sala 101 > Armário 01"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar cadastrar localização com campos inválidos")
    void deveRetornarBadRequestParaCamposInvalidos() throws Exception {
        LocalizacaoRequestDTO invalido = LocalizacaoRequestDTO.builder()
                .codigo("")
                .nome("")
                .build();

        mockMvc.perform(post("/api/v1/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItems("codigo", "nome")));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar cadastrar código já existente")
    void deveRetornarConflictParaCodigoDuplicado() throws Exception {
        LocalizacaoRequestDTO duplicado = LocalizacaoRequestDTO.builder()
                .codigo("PREDIO-1")
                .nome("Outro Prédio")
                .build();

        mockMvc.perform(post("/api/v1/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("Já existe uma localização cadastrada com o código")));
    }

    @Test
    @DisplayName("Deve listar todas as localizações cadastradas (HTTP 200)")
    void deveListarTodasAsLocalizacoes() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].codigo", hasItems("PREDIO-1", "SALA-101")));
    }

    @Test
    @DisplayName("Deve listar apenas as localizações raiz (HTTP 200)")
    void deveListarApenasRaizes() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/raizes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].codigo").value("PREDIO-1"));
    }

    @Test
    @DisplayName("Deve buscar localização existente por ID (HTTP 200)")
    void deveBuscarPorIdExistente() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/{id}", salaSalva.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(salaSalva.getId()))
                .andExpect(jsonPath("$.data.codigo").value("SALA-101"))
                .andExpect(jsonPath("$.data.localPaiId").value(predioSalvo.getId()));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found para ID inexistente")
    void deveRetornarNotFoundParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/{id}", 99999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Deve buscar localização existente por código (HTTP 200)")
    void deveBuscarPorCodigoExistente() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/codigo/{codigo}", "PREDIO-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("PREDIO-1"));
    }

    @Test
    @DisplayName("Deve listar filhas de um local pai específico (HTTP 200)")
    void deveListarFilhasDeUmPai() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/{id}/filhas", predioSalvo.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].codigo").value("SALA-101"));
    }

    @Test
    @DisplayName("Deve obter caminho hierárquico ordenado (HTTP 200)")
    void deveObterCaminhoHierarquico() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes/{id}/caminho", salaSalva.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].codigo").value("PREDIO-1"))
                .andExpect(jsonPath("$.data[1].codigo").value("SALA-101"));
    }

    @Test
    @DisplayName("Deve atualizar localização com sucesso (HTTP 200)")
    void deveAtualizarLocalizacaoComSucesso() throws Exception {
        LocalizacaoRequestDTO atualizacao = LocalizacaoRequestDTO.builder()
                .codigo("PREDIO-1")
                .nome("Prédio Central Reformado")
                .tipo("Edifício Principal")
                .descricao("Novo hall de entrada")
                .build();

        mockMvc.perform(put("/api/v1/localizacoes/{id}", predioSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Localização atualizada com sucesso"))
                .andExpect(jsonPath("$.data.nome").value("Prédio Central Reformado"))
                .andExpect(jsonPath("$.data.tipo").value("Edifício Principal"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar ciclo hierárquico na atualização")
    void deveRetornarBadRequestAoCriarCicloHierarquico() throws Exception {
        // Tentar colocar a sala como pai do prédio (ciclo PREDIO -> SALA -> PREDIO)
        LocalizacaoRequestDTO comCiclo = LocalizacaoRequestDTO.builder()
                .codigo("PREDIO-1")
                .nome("Prédio Central")
                .localPaiId(salaSalva.getId())
                .build();

        mockMvc.perform(put("/api/v1/localizacoes/{id}", predioSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comCiclo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Ciclo detectado")));
    }

    @Test
    @DisplayName("Deve excluir localização com sucesso (HTTP 204)")
    void deveExcluirLocalizacaoComSucesso() throws Exception {
        mockMvc.perform(delete("/api/v1/localizacoes/{id}", salaSalva.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/localizacoes/{id}", salaSalva.getId()))
                .andExpect(status().isNotFound());
    }
}
