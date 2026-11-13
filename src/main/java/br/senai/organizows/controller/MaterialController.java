package br.senai.organizows.controller;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<MaterialResponseDTO> criar(@Valid @RequestBody MaterialRequestDTO dto) {
        MaterialResponseDTO criado = materialService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponseDTO>> listarTodos() {
        return ResponseEntity.ok(materialService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody MaterialRequestDTO dto) {
        return ResponseEntity.ok(materialService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        materialService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint do critério de aceite: Alerta de reposição (itens abaixo ou igual ao mínimo)
    @GetMapping("/alerta-reposicao")
    public ResponseEntity<List<MaterialResponseDTO>> listarAlertaReposicao() {
        return ResponseEntity.ok(materialService.listarAlertaReposicao());
    }
}