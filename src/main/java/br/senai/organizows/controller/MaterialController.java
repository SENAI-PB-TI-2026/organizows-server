package br.senai.organizows.controller;

import br.senai.organizows.dto.AjusteSaldoRequestDTO;
import br.senai.organizows.dto.ClassificacaoMaterialResponseDTO;
import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.MaterialUpdateRequestDTO;
import br.senai.organizows.dto.response.ApiErrorResponse;
import br.senai.organizows.dto.response.ApiResponse;
import br.senai.organizows.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materiais")
@Tag(name = "Materiais", description = "Operações disponíveis para consulta e manutenção do catálogo de materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar material",
            description = "Cadastra um material associado a uma localização existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do material a cadastrar; use valores fictícios nos exemplos.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MaterialRequestDTO.class))))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Material criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Tipo de conteúdo não suportado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização informada não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado ou violação de integridade", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> criar(@Valid @RequestBody MaterialRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material criado com sucesso", materialService.criar(dto)));
    }

    @GetMapping
    @Operation(summary = "Listar materiais", description = "Lista os materiais cadastrados, com filtro opcional pelo grupo.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de materiais retornada")
    })
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listarTodos(
            @Parameter(description = "Grupo para filtrar a lista; quando omitido, retorna todos os materiais.", example = "Periféricos")
            @RequestParam(required = false) String grupo) {
        List<MaterialResponseDTO> materiais = (grupo == null || grupo.isBlank())
                ? materialService.listarTodos()
                : materialService.listarPorGrupo(grupo);
        return ResponseEntity.ok(ApiResponse.success(materiais));
    }

    @GetMapping("/classificacoes")
    @Operation(summary = "Listar classificações", description = "Retorna as categorias e grupos de materiais disponíveis.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Classificações retornadas")
    })
    public ResponseEntity<ApiResponse<List<ClassificacaoMaterialResponseDTO>>> listarClassificacoes() {
        return ResponseEntity.ok(ApiResponse.success(materialService.listarClassificacoes()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar material por ID", description = "Retorna os dados do material identificado pelo ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Material encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID inválido", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorId(
            @Parameter(description = "ID numérico do material", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.buscarPorId(id)));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar material por código", description = "Retorna os dados do material identificado pelo código único.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Material encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorCodigo(
            @Parameter(description = "Código único do material", example = "MAT-EXEMPLO-001") @PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.success(materialService.buscarPorCodigo(codigo)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar material",
            description = "Atualiza os dados cadastrais do material. Os campos do DTO são validados como obrigatórios pelo serviço.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do material.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MaterialUpdateRequestDTO.class))))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Material atualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Tipo de conteúdo não suportado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material ou localização não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código duplicado ou violação de integridade", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> atualizar(
            @Parameter(description = "ID numérico do material", example = "1") @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Material atualizado com sucesso",
                materialService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir material", description = "Exclui o material identificado pelo ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Material excluído; sem corpo de resposta"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID inválido", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID numérico do material", example = "1") @PathVariable Long id) {
        materialService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerta-reposicao")
    @Operation(summary = "Listar materiais em alerta de reposição", description = "Retorna materiais cuja quantidade disponível está abaixo da quantidade mínima.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Materiais em alerta retornados")
    })
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listarAlertaReposicao() {
        return ResponseEntity.ok(ApiResponse.success(materialService.listarAlertaReposicao()));
    }

    @PostMapping("/{id}/ajuste-saldo")
    @Operation(
            summary = "Ajustar saldo do material",
            description = "Aplica uma variação diferente de zero ao saldo atual. O saldo final não pode ser negativo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Variação a aplicar ao saldo atual.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AjusteSaldoRequestDTO.class))))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Saldo ajustado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ajuste inválido ou saldo resultante negativo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Tipo de conteúdo não suportado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> ajustarSaldo(
            @Parameter(description = "ID numérico do material", example = "1") @PathVariable Long id,
            @Valid @RequestBody AjusteSaldoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Saldo ajustado com sucesso",
                materialService.ajustarSaldo(id, dto.getDeltaQuantidade())));
    }

    @PatchMapping("/{id}/saldo")
    @Operation(
            summary = "Atualizar saldo do material",
            description = "Aplica o delta da query string, se informado; caso contrário, usa o delta do corpo. O valor deve ser diferente de zero e o saldo final não pode ser negativo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Alternativa para informar o delta quando ele não for enviado na query string.",
                    content = @Content(schema = @Schema(implementation = AjusteSaldoRequestDTO.class))))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Saldo ajustado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delta inválido, ausente, igual a zero ou saldo resultante negativo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Tipo de conteúdo não suportado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Material não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> atualizarSaldo(
            @Parameter(description = "ID numérico do material", example = "1") @PathVariable Long id,
            @Parameter(description = "Delta inteiro a aplicar; tem precedência sobre o valor no corpo.", example = "5")
            @RequestParam(required = false) Integer delta,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false)
            @RequestBody(required = false) AjusteSaldoRequestDTO body) {
        Integer variacao = delta != null ? delta : body == null ? null : body.getDeltaQuantidade();
        return ResponseEntity.ok(ApiResponse.success("Saldo ajustado com sucesso",
                materialService.ajustarSaldo(id, variacao)));
    }
}
