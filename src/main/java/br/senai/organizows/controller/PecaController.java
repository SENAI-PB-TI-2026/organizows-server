package br.senai.organizows.controller;

import br.senai.organizows.dto.MovimentacaoEstoqueDTO;
import br.senai.organizows.dto.PecaRequestDTO;
import br.senai.organizows.dto.PecaResponseDTO;
import br.senai.organizows.service.PecaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api/pecas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PecaController {

    private final PecaService pecaService;

    @GetMapping
    public ResponseEntity<List<PecaResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String codigo) {

        if (codigo != null && !codigo.isBlank()) {
            return pecaService.buscarPorCodigo(codigo)
                    .map(peca -> ResponseEntity.ok(List.of(peca)))
                    .orElse(ResponseEntity.ok(List.of()));
        }

        if (categoria != null && !categoria.isBlank()) {
            return ResponseEntity.ok(pecaService.buscarPorCategoria(categoria));
        }

        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(pecaService.buscarPorNome(nome));
        }

        return ResponseEntity.ok(pecaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> buscarPorId(@PathVariable Long id) {
        return pecaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/alerta-estoque")
    public ResponseEntity<List<PecaResponseDTO>> listarEstoqueBaixo() {
        return ResponseEntity.ok(pecaService.listarEstoqueBaixo());
    }

    @PostMapping
    public ResponseEntity<PecaResponseDTO> salvar(@Valid @RequestBody PecaRequestDTO dto) {
        PecaResponseDTO novaPeca = pecaService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaPeca);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PecaRequestDTO dto) {
        try {
            PecaResponseDTO pecaAtualizada = pecaService.atualizar(id, dto);
            return ResponseEntity.ok(pecaAtualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/movimentar-estoque")
    public ResponseEntity<?> movimentarEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueDTO dto) {
        try {
            PecaResponseDTO pecaAtualizada = pecaService.movimentarEstoque(id, dto);
            return ResponseEntity.ok(pecaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            pecaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
