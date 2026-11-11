package br.senai.organizows.service;

import br.senai.organizows.dto.MovimentacaoEstoqueDTO;
import br.senai.organizows.dto.PecaRequestDTO;
import br.senai.organizows.dto.PecaResponseDTO;
import br.senai.organizows.model.Peca;
import br.senai.organizows.repository.PecaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PecaService {

    private final PecaRepository pecaRepository;

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> listarTodas() {
        return pecaRepository.findAll()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PecaResponseDTO> buscarPorId(Long id) {
        return pecaRepository.findById(id).map(PecaResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> buscarPorNome(String nome) {
        return pecaRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> buscarPorCategoria(String categoria) {
        return pecaRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PecaResponseDTO> buscarPorCodigo(String codigo) {
        return pecaRepository.findByCodigoIgnoreCase(codigo).map(PecaResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> listarEstoqueBaixo() {
        return pecaRepository.findPecasComEstoqueBaixo()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public PecaResponseDTO salvar(PecaRequestDTO dto) {
        Peca peca = toEntity(dto);
        Peca salva = pecaRepository.save(peca);
        return PecaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public PecaResponseDTO atualizar(Long id, PecaRequestDTO dto) {
        return pecaRepository.findById(id)
                .map(pecaExistente -> {
                    pecaExistente.setNome(dto.getNome());
                    pecaExistente.setCodigo(dto.getCodigo());
                    pecaExistente.setCategoria(dto.getCategoria());
                    pecaExistente.setDescricao(dto.getDescricao());
                    pecaExistente.setQuantidadeEstoque(dto.getQuantidadeEstoque() != null ? dto.getQuantidadeEstoque() : 0);
                    pecaExistente.setQuantidadeMinima(dto.getQuantidadeMinima() != null ? dto.getQuantidadeMinima() : 5);
                    pecaExistente.setPrecoUnitario(dto.getPrecoUnitario());
                    pecaExistente.setLocalizacao(dto.getLocalizacao());
                    return PecaResponseDTO.fromEntity(pecaRepository.save(pecaExistente));
                })
                .orElseThrow(() -> new RuntimeException("Peça não encontrada com id: " + id));
    }

    @Transactional
    public PecaResponseDTO movimentarEstoque(Long id, MovimentacaoEstoqueDTO dto) {
        return pecaRepository.findById(id)
                .map(peca -> {
                    int novoEstoque = peca.getQuantidadeEstoque() + dto.getQuantidade();
                    if (novoEstoque < 0) {
                        throw new IllegalArgumentException("Saldo de estoque insuficiente para realizar a saída.");
                    }
                    peca.setQuantidadeEstoque(novoEstoque);
                    return PecaResponseDTO.fromEntity(pecaRepository.save(peca));
                })
                .orElseThrow(() -> new RuntimeException("Peça não encontrada com id: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        if (!pecaRepository.existsById(id)) {
            throw new RuntimeException("Peça não encontrada com id: " + id);
        }
        pecaRepository.deleteById(id);
    }

    private Peca toEntity(PecaRequestDTO dto) {
        return Peca.builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .categoria(dto.getCategoria())
                .descricao(dto.getDescricao())
                .quantidadeEstoque(dto.getQuantidadeEstoque() != null ? dto.getQuantidadeEstoque() : 0)
                .quantidadeMinima(dto.getQuantidadeMinima() != null ? dto.getQuantidadeMinima() : 5)
                .precoUnitario(dto.getPrecoUnitario())
                .localizacao(dto.getLocalizacao())
                .build();
    }
}
