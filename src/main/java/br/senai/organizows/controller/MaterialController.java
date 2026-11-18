package br.senai.organizows.controller;

import br.senai.organizows.dto.AjusteSaldoRequestDTO;
import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.response.ApiResponse;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/materiais", "/api/materiais"})
@Tag(name = "Materiais", description = "Endpoints de gerenciamento cadastral e controle de estoque de materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @Operation(summary = "Criar material", description = "Cadastra um novo material com validação de código único e associação opcional a uma localização existente.")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> criar(@Valid @RequestBody MaterialRequestDTO dto) {
        MaterialResponseDTO criado = materialService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material cadastrado com sucesso", criado));
    }

    @GetMapping
    @Operation(summary = "Listar materiais", description = "Retorna lista de materiais cadastrados, suportando filtros opcionais por nome, categoria, status, código ou localId.")
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) CategoriaMaterial categoria,
            @RequestParam(required = false) StatusMaterial status,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Long localId) {
        List<MaterialResponseDTO> lista = materialService.listar(nome, categoria, status, codigo, localId);
        return ResponseEntity.ok(ApiResponse.success(lista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar material por ID", description = "Retorna os detalhes de um material pelo seu identificador primário.")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorId(@PathVariable Long id) {
        MaterialResponseDTO material = materialService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(material));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar material por código", description = "Retorna os detalhes de um material pelo seu código único de negócio.")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorCodigo(@PathVariable String codigo) {
        MaterialResponseDTO material = materialService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.success(material));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar material", description = "Atualiza os dados cadastrais e estoque mínimo de um material, preservando as regras de estoque.")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialRequestDTO dto) {
        MaterialResponseDTO atualizado = materialService.atualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Material atualizado com sucesso", atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir material", description = "Exclui um material existente do catálogo.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        materialService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerta-reposicao")
    @Operation(summary = "Listar materiais com alerta de reposição", description = "Retorna os materiais cujo saldo disponível é menor ou igual à quantidade mínima.")
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listarAlertaReposicao() {
        List<MaterialResponseDTO> lista = materialService.listarAlertaReposicao();
        return ResponseEntity.ok(ApiResponse.success(lista));
    }

    @PatchMapping("/{id}/saldo")
    @Operation(summary = "Ajustar saldo do material", description = "Ajusta o saldo disponível com bloqueio pessimista, tratando concorrência e overflow.")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> ajustarSaldo(
            @PathVariable Long id,
            @RequestParam(required = false) Integer delta,
            @RequestBody(required = false) AjusteSaldoRequestDTO body) {
        Integer deltaFinal = delta;
        if (deltaFinal == null && body != null) {
            deltaFinal = body.getDelta();
        }
        MaterialResponseDTO atualizado = materialService.ajustarSaldo(id, deltaFinal);
        return ResponseEntity.ok(ApiResponse.success("Saldo ajustado com sucesso", atualizado));
    }
}