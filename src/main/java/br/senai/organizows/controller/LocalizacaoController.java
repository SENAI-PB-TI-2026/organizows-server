package br.senai.organizows.controller;

import br.senai.organizows.dto.localizacao.LocalizacaoRequestDTO;
import br.senai.organizows.dto.localizacao.LocalizacaoResponseDTO;
import br.senai.organizows.dto.response.ApiErrorResponse;
import br.senai.organizows.dto.response.ApiResponse;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.service.LocalizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/localizacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Localizações", description = "Endpoints para gerenciamento do catálogo e hierarquia de localizações físicas")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @PostMapping
    @Operation(summary = "Criar nova localização", description = "Cadastra uma nova localização física, opcionalmente associada a um local pai.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Localização criada com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou ciclo hierárquico",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código de localização duplicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocalizacaoResponseDTO>> criar(@Valid @RequestBody LocalizacaoRequestDTO dto) {
        LocalizacaoResponseDTO response = localizacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Localização criada com sucesso", response));
    }

    @GetMapping
    @Operation(summary = "Listar localizações", description = "Retorna todas as localizações cadastradas, com suporte a filtro opcional por código.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<ApiResponse<List<LocalizacaoResponseDTO>>> listar(
            @RequestParam(required = false) String codigo) {
        if (codigo != null && !codigo.isBlank()) {
            return localizacaoService.buscarPorCodigo(codigo)
                    .map(loc -> ResponseEntity.ok(ApiResponse.success(List.of(LocalizacaoResponseDTO.fromEntity(loc)))))
                    .orElse(ResponseEntity.ok(ApiResponse.success(List.of())));
        }

        List<LocalizacaoResponseDTO> lista = localizacaoService.listarTodas()
                .stream()
                .map(LocalizacaoResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(lista));
    }

    @GetMapping("/raizes")
    @Operation(summary = "Listar localizações raiz", description = "Retorna apenas as localizações do nível superior (sem pai).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de raízes retornada com sucesso")
    })
    public ResponseEntity<ApiResponse<List<LocalizacaoResponseDTO>>> listarRaizes() {
        List<LocalizacaoResponseDTO> raizes = localizacaoService.listarRaizes()
                .stream()
                .map(LocalizacaoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(raizes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar localização por ID", description = "Retorna os detalhes de uma localização específica pelo seu identificador único.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Localização encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocalizacaoResponseDTO>> buscarPorId(@PathVariable Long id) {
        Localizacao localizacao = localizacaoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada com ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(LocalizacaoResponseDTO.fromEntity(localizacao)));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar localização por código", description = "Retorna os detalhes de uma localização específica pelo seu código único.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Localização encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocalizacaoResponseDTO>> buscarPorCodigo(@PathVariable String codigo) {
        Localizacao localizacao = localizacaoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada com código: " + codigo));
        return ResponseEntity.ok(ApiResponse.success(LocalizacaoResponseDTO.fromEntity(localizacao)));
    }

    @GetMapping("/{id}/filhas")
    @Operation(summary = "Listar localizações filhas", description = "Retorna as localizações diretamente subordinadas a um determinado local pai.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de localizações filhas retornada")
    })
    public ResponseEntity<ApiResponse<List<LocalizacaoResponseDTO>>> listarFilhas(@PathVariable Long id) {
        List<LocalizacaoResponseDTO> filhas = localizacaoService.listarFilhas(id)
                .stream()
                .map(LocalizacaoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(filhas));
    }

    @GetMapping("/{id}/caminho")
    @Operation(summary = "Obter caminho hierárquico", description = "Retorna a sequência ordenada de nós da raiz até a localização informada.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Caminho da hierarquia retornado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<LocalizacaoResponseDTO>>> obterCaminho(@PathVariable Long id) {
        List<LocalizacaoResponseDTO> caminho = localizacaoService.obterCaminhoHierarquia(id)
                .stream()
                .map(LocalizacaoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(caminho));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar localização", description = "Atualiza os dados cadastrais e o local pai de uma localização existente.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Localização atualizada com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos ou hierarquia cíclica",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização ou local pai não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Código já utilizado por outra localização",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocalizacaoResponseDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LocalizacaoRequestDTO dto) {
        LocalizacaoResponseDTO response = localizacaoService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Localização atualizada com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir localização", description = "Exclui uma localização do sistema pelo seu ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Localização removida com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        localizacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
