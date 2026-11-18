package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final LocalizacaoRepository localizacaoRepository;

    public MaterialService(MaterialRepository materialRepository, LocalizacaoRepository localizacaoRepository) {
        this.materialRepository = materialRepository;
        this.localizacaoRepository = localizacaoRepository;
    }

    @Transactional
    public MaterialResponseDTO criar(MaterialRequestDTO dto) {
        Material material = new Material();
        preencherCadastro(material, dto);
        material.setQuantidadeDisponivel(dto.getQuantidadeDisponivel());
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    public List<MaterialResponseDTO> listarTodos() {
        return materialRepository.findAll().stream().map(MaterialResponseDTO::new).toList();
    }

    public List<MaterialResponseDTO> listarPorGrupo(String grupo) {
        return materialRepository.findByGrupoIgnoreCase(grupo).stream()
                .map(MaterialResponseDTO::new).toList();
    }

    public MaterialResponseDTO buscarPorId(Long id) {
        return new MaterialResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dto) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
        // Atualizações cadastrais não podem substituir o saldo e contornar uma movimentação concorrente.
        if (!material.getQuantidadeDisponivel().equals(dto.getQuantidadeDisponivel())) {
            throw new IllegalArgumentException("Para alterar a quantidade disponível, utilize o ajuste de saldo.");
        }
        preencherCadastro(material, dto);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    @Transactional
    public void deletar(Long id) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
        materialRepository.delete(material);
    }

    public List<MaterialResponseDTO> listarAlertaReposicao() {
        return materialRepository.findMateriaisParaReposicao().stream()
                .map(MaterialResponseDTO::new).toList();
    }

    @Transactional
    public MaterialResponseDTO ajustarSaldo(Long materialId, Integer deltaQuantidade) {
        if (deltaQuantidade == null || deltaQuantidade == 0) {
            throw new IllegalArgumentException("A quantidade do ajuste deve ser diferente de zero.");
        }
        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + materialId));

        int novoSaldo;
        try {
            novoSaldo = Math.addExact(material.getQuantidadeDisponivel(), deltaQuantidade);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("O ajuste de saldo excede o limite permitido.", ex);
        }

        if (novoSaldo < 0) {
            throw new IllegalArgumentException("O saldo do material não pode ficar negativo.");
        }
        material.setQuantidadeDisponivel(novoSaldo);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    private void preencherCadastro(Material material, MaterialRequestDTO dto) {
        material.setCodigo(dto.getCodigo().trim());
        material.setNome(dto.getNome().trim());
        material.setDescricao(dto.getDescricao().trim());
        material.setCaracteristicas(dto.getCaracteristicas());
        material.setGrupo(dto.getGrupo().trim());
        material.setSubgrupo(dto.getSubgrupo());
        material.setCategoria(dto.getCategoria());
        material.setStatus(dto.getStatus());
        material.setQuantidadeMinima(dto.getQuantidadeMinima());

        Localizacao localizacao = localizacaoRepository.findById(dto.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Localização não encontrada com ID: " + dto.getLocalId()));
        material.setLocalizacao(localizacao);

        materialRepository.findByCodigo(material.getCodigo())
                .filter(existente -> !existente.getId().equals(material.getId()))
                .ifPresent(existente -> {
                    throw new org.springframework.dao.DataIntegrityViolationException(
                            "Código de material já cadastrado: " + material.getCodigo());
                });
    }

    private Material buscarEntidadePorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
    }
}
