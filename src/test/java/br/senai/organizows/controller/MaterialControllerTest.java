package br.senai.organizows.controller;

import br.senai.organizows.dto.AjusteSaldoRequestDTO;
import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialUpdateRequestDTO;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Localizacao localizacaoSalva;
    private Material materialSalvo;

    @BeforeEach
    void setUp() {
        Localizacao loc = Localizacao.builder()
                .codigo("LOC-CTRL-1")
                .nome("Prateleira Controller")
                .tipo("ALMOXARIFADO")
                .build();
        localizacaoSalva = localizacaoRepository.save(loc);

        Material mat = Material.builder()
                .codigo("MAT-CTRL-1")
                .nome("Cabo UTP Cat6")
                .descricao("Cabo Azul 305m")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Rede")
                .modelo("Furukawa Cat6")
                .especificacoesTecnicas("4 pares trançados")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(20)
                .quantidadeMinima(10)
                .localizacao(localizacaoSalva)
                .build();
        materialSalvo = materialRepository.save(mat);
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 201 Created quando payload for válido")
    void deveRetornar201AoCriarMaterialValido() throws Exception {
        MaterialRequestDTO novo = MaterialRequestDTO.builder()
                .codigo("MAT-NOVO-1")
                .nome("Patch Cord Cat6")
                .descricao("Cabo 1.5m")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Conectividade")
                .modelo("Furukawa")
                .especificacoesTecnicas("RJ45")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(50)
                .quantidadeMinima(15)
                .localId(localizacaoSalva.getId())
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.codigo").value("MAT-NOVO-1"))
                .andExpect(jsonPath("$.data.nome").value("Patch Cord Cat6"))
                .andExpect(jsonPath("$.data.modelo").value("Furukawa"))
                .andExpect(jsonPath("$.data.especificacoesTecnicas").value("RJ45"))
                .andExpect(jsonPath("$.data.localId").value(localizacaoSalva.getId()))
                .andExpect(jsonPath("$.data.localizacaoNome").value("Prateleira Controller"));
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 400 Bad Request quando código ou nome estiverem em branco")
    void deveRetornar400QuandoDadosObrigatoriosFaltando() throws Exception {
        MaterialRequestDTO invalido = MaterialRequestDTO.builder()
                .codigo("")
                .nome("   ")
                .categoria(CategoriaMaterial.CABO)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(10)
                .quantidadeMinima(5)
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 400 Bad Request quando quantidade for negativa")
    void deveRetornar400QuandoQuantidadeNegativa() throws Exception {
        MaterialRequestDTO negativo = MaterialRequestDTO.builder()
                .codigo("MAT-NEG-1")
                .nome("Material Negativo")
                .descricao("Material de teste")
                .grupo("Testes")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(-5)
                .quantidadeMinima(2)
                .localId(localizacaoSalva.getId())
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'quantidadeDisponivel')]").exists());
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 400 quando descriÃ§Ã£o exceder 150 caracteres")
    void deveRetornar400QuandoDescricaoExcederLimite() throws Exception {
        MaterialRequestDTO invalido = MaterialRequestDTO.builder()
                .codigo("MAT-DESC-LONGA")
                .nome("Material com descriÃ§Ã£o longa")
                .descricao("d".repeat(151))
                .grupo("Testes")
                .categoria(CategoriaMaterial.COMPONENTE_ELETRONICO)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(0)
                .quantidadeMinima(0)
                .localId(localizacaoSalva.getId())
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'descricao')]").exists());
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 404 Not Found quando localização não existir")
    void deveRetornar404QuandoLocalIdInexistente() throws Exception {
        MaterialRequestDTO semLocal = MaterialRequestDTO.builder()
                .codigo("MAT-SEM-LOCAL")
                .nome("Material Sem Local")
                .descricao("Material de teste")
                .grupo("Testes")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(10)
                .quantidadeMinima(2)
                .localId(99999L)
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semLocal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/materiais - Deve retornar 409 Conflict quando código for duplicado")
    void deveRetornar409QuandoCodigoDuplicado() throws Exception {
        MaterialRequestDTO duplicado = MaterialRequestDTO.builder()
                .codigo("MAT-CTRL-1")
                .nome("Outro Cabo")
                .descricao("Material duplicado")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(5)
                .quantidadeMinima(1)
                .localId(localizacaoSalva.getId())
                .build();

        mockMvc.perform(post("/api/v1/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/materiais - Deve retornar 200 OK com lista e suportar filtros")
    void deveRetornar200AoListar() throws Exception {
        mockMvc.perform(get("/api/v1/materiais")
                        .param("categoria", "CABO")
                        .param("nome", "Cabo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].codigo").value("MAT-CTRL-1"));
    }

    @Test
    @DisplayName("GET /api/v1/materiais/{id} - Deve retornar 200 OK quando encontrado")
    void deveRetornar200AoBuscarPorId() throws Exception {
        mockMvc.perform(get("/api/v1/materiais/{id}", materialSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(materialSalvo.getId()))
                .andExpect(jsonPath("$.data.codigo").value("MAT-CTRL-1"))
                .andExpect(jsonPath("$.data.modelo").value("Furukawa Cat6"))
                .andExpect(jsonPath("$.data.especificacoesTecnicas").value("4 pares trançados"));
    }

    @Test
    @DisplayName("GET /api/v1/materiais/{id} - Deve retornar 404 Not Found quando não encontrado")
    void deveRetornar404AoBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/materiais/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/materiais/codigo/{codigo} - Deve retornar 200 OK quando encontrado")
    void deveRetornar200AoBuscarPorCodigo() throws Exception {
        mockMvc.perform(get("/api/v1/materiais/codigo/{codigo}", "MAT-CTRL-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("MAT-CTRL-1"))
                .andExpect(jsonPath("$.data.modelo").value("Furukawa Cat6"))
                .andExpect(jsonPath("$.data.especificacoesTecnicas").value("4 pares trançados"));
    }

    @Test
    @DisplayName("GET /api/v1/materiais/alerta-reposicao - Deve retornar 200 OK com lista de reposição")
    void deveRetornar200AoBuscarAlertaReposicao() throws Exception {
        // Criar um item com estoque baixo (5 <= 10)
        Material baixo = Material.builder()
                .codigo("MAT-BAIXO-1")
                .nome("Item Reposição")
                .descricao("Material para reposição")
                .grupo("Consumíveis")
                .categoria(CategoriaMaterial.CONSUMIVEL)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(5)
                .quantidadeMinima(10)
                .localizacao(localizacaoSalva)
                .build();
        materialRepository.save(baixo);

        mockMvc.perform(get("/api/v1/materiais/alerta-reposicao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].alertaReposicao").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/materiais/{id} - Deve retornar 200 OK ao atualizar e preservar saldo atual")
    void deveRetornar200AoAtualizarEPreservarSaldo() throws Exception {
        MaterialUpdateRequestDTO atualizacao = MaterialUpdateRequestDTO.builder()
                .codigo("MAT-CTRL-1")
                .nome("Cabo UTP Cat6 Alterado")
                .descricao("Nova Descrição")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeMinima(15)
                .localId(localizacaoSalva.getId())
                .build();

        mockMvc.perform(put("/api/v1/materiais/{id}", materialSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nome").value("Cabo UTP Cat6 Alterado"))
                .andExpect(jsonPath("$.data.quantidadeDisponivel").value(20)) // Saldo original 20 foi preservado!
                .andExpect(jsonPath("$.data.quantidadeMinima").value(15));
    }

    @Test
    @DisplayName("DELETE /api/v1/materiais/{id} - Deve retornar 204 No Content")
    void deveRetornar204AoDeletar() throws Exception {
        mockMvc.perform(delete("/api/v1/materiais/{id}", materialSalvo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/materiais/{id}", materialSalvo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/materiais/{id}/saldo - Deve retornar 200 OK ao ajustar com query param")
    void deveAjustarSaldoViaParam() throws Exception {
        mockMvc.perform(patch("/api/v1/materiais/{id}/saldo", materialSalvo.getId())
                        .param("delta", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantidadeDisponivel").value(30));
    }

    @Test
    @DisplayName("PATCH /api/v1/materiais/{id}/saldo - Deve retornar 200 OK ao ajustar com request body")
    void deveAjustarSaldoViaBody() throws Exception {
        AjusteSaldoRequestDTO body = new AjusteSaldoRequestDTO(-5);

        mockMvc.perform(patch("/api/v1/materiais/{id}/saldo", materialSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantidadeDisponivel").value(15));
    }

    @Test
    @DisplayName("PATCH /api/v1/materiais/{id}/saldo - Deve retornar 400 Bad Request quando delta torna saldo negativo")
    void deveRetornar400QuandoSaldoInsuficiente() throws Exception {
        mockMvc.perform(patch("/api/v1/materiais/{id}/saldo", materialSalvo.getId())
                        .param("delta", "-100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PATCH /api/v1/materiais/{id}/saldo - Deve retornar 400 Bad Request quando delta for zero")
    void deveRetornar400QuandoDeltaZero() throws Exception {
        mockMvc.perform(patch("/api/v1/materiais/{id}/saldo", materialSalvo.getId())
                        .param("delta", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PATCH /api/v1/materiais/{id}/saldo - Deve retornar 400 Bad Request em caso de overflow numérico")
    void deveRetornar400QuandoOverflow() throws Exception {
        mockMvc.perform(patch("/api/v1/materiais/{id}/saldo", materialSalvo.getId())
                        .param("delta", String.valueOf(Integer.MAX_VALUE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
