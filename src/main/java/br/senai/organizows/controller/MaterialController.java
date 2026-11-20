package br.senai.organizows.controller;

import br.senai.organizows.dto.AjusteSaldoRequestDTO;
import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.response.ApiResponse;
import br.senai.organizows.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> criar(@Valid @RequestBody MaterialRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material criado com sucesso", materialService.criar(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listarTodos(
            @RequestParam(required = false) String grupo) {
        List<MaterialResponseDTO> materiais = (grupo == null || grupo.isBlank())
                ? materialService.listarTodos()
                : materialService.listarPorGrupo(grupo);
        return ResponseEntity.ok(ApiResponse.success(materiais));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.buscarPorId(id)));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.success(materialService.buscarPorCodigo(codigo)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> atualizar(
            @PathVariable Long id, @Valid @RequestBody MaterialRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Material atualizado com sucesso",
                materialService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        materialService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerta-reposicao")
    public ResponseEntity<ApiResponse<List<MaterialResponseDTO>>> listarAlertaReposicao() {
        return ResponseEntity.ok(ApiResponse.success(materialService.listarAlertaReposicao()));
    }

    @PostMapping("/{id}/ajuste-saldo")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> ajustarSaldo(
            @PathVariable Long id, @Valid @RequestBody AjusteSaldoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Saldo ajustado com sucesso",
                materialService.ajustarSaldo(id, dto.getDeltaQuantidade())));
    }

    @PatchMapping("/{id}/saldo")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> atualizarSaldo(
            @PathVariable Long id, @RequestParam(required = false) Integer delta,
            @RequestBody(required = false) AjusteSaldoRequestDTO body) {
        Integer variacao = delta != null ? delta : body == null ? null : body.getDeltaQuantidade();
        return ResponseEntity.ok(ApiResponse.success("Saldo ajustado com sucesso",
                materialService.ajustarSaldo(id, variacao)));
    }
}
