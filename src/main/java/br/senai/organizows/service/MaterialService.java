package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialCadastroDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.MaterialUpdateRequestDTO;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.exception.CodigoMaterialDuplicadoException;
import br.senai.organizows.exception.HierarquiaInvalidaException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
        validarQuantidades(dto);
        if (materialRepository.existsByCodigo(dto.getCodigo().trim())) {
            throw new CodigoMaterialDuplicadoException(
                    "Já existe um material cadastrado com o código: " + dto.getCodigo().trim());
        }
        Material material = new Material();
        preencherCadastro(material, dto);
        material.setQuantidadeDisponivel(dto.getQuantidadeDisponivel());
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    public List<MaterialResponseDTO> listarTodos() {
        return materialRepository.findAll().stream().map(MaterialResponseDTO::new).toList();
    }

    public List<MaterialResponseDTO> listarPorGrupo(String grupo) {
        return materialRepository.findByGrupoIgnoreCase(grupo.trim()).stream()
                .map(MaterialResponseDTO::new).toList();
    }

    public MaterialResponseDTO buscarPorCodigo(String codigo) {
        Material material = materialRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com código: " + codigo));
        return new MaterialResponseDTO(material);
    }

    public MaterialResponseDTO buscarPorId(Long id) {
        return new MaterialResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialUpdateRequestDTO dto) {
        return atualizarCadastro(id, dto);
    }

    private MaterialResponseDTO atualizarCadastro(Long id, MaterialCadastroDTO dto) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
        validarQuantidadeMinima(dto.getQuantidadeMinima());
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
            throw new IllegalArgumentException("O ajuste de saldo excede o limite permitido (overflow).", ex);
        }

        if (novoSaldo < 0) {
            throw new IllegalArgumentException("O saldo do material não pode ficar negativo.");
        }
        material.setQuantidadeDisponivel(novoSaldo);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    private void preencherCadastro(Material material, MaterialCadastroDTO dto) {
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()
                || dto.getNome() == null || dto.getNome().isBlank()
                || dto.getDescricao() == null || dto.getDescricao().isBlank()
                || dto.getGrupo() == null || dto.getGrupo().isBlank()
                || dto.getCategoria() == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("Código, nome, descrição, grupo, categoria e status são obrigatórios.");
        }
        if (dto.getLocalId() == null) {
            throw new IllegalArgumentException("A localização do material é obrigatória.");
        }
        String grupo = dto.getGrupo().trim();
        String subgrupo = dto.getSubgrupo() == null || dto.getSubgrupo().isBlank()
                ? null : dto.getSubgrupo().trim();
        boolean classificacaoAlterada = dto.getCategoria() != material.getCategoria()
                || !grupo.equals(material.getGrupo())
                || !Objects.equals(subgrupo, material.getSubgrupo());
        if (classificacaoAlterada && subgrupo != null && !dto.getCategoria().aceitaSubgrupo(grupo, subgrupo)) {
            throw new HierarquiaInvalidaException("O subgrupo informado não pertence ao grupo e à categoria do material.");
        }

        material.setCodigo(dto.getCodigo().trim());
        material.setNome(dto.getNome().trim());
        material.setDescricao(dto.getDescricao() == null ? null : dto.getDescricao().trim());
        material.setCaracteristicas(dto.getCaracteristicas());
        material.setModelo(dto.getModelo());
        material.setEspecificacoesTecnicas(dto.getEspecificacoesTecnicas());
        material.setGrupo(grupo);
        material.setSubgrupo(subgrupo);
        material.setCategoria(dto.getCategoria());
        material.setStatus(dto.getStatus());
        material.setQuantidadeMinima(dto.getQuantidadeMinima());

        Localizacao localizacao = localizacaoRepository.findById(dto.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Localização não encontrada com ID: " + dto.getLocalId()));
        material.setLocalizacao(localizacao);

        materialRepository.findByCodigo(material.getCodigo())
                .filter(existente -> !existente.getId().equals(material.getId()))
                .ifPresent(existente -> { throw new CodigoMaterialDuplicadoException(
                        "Já existe um material cadastrado com o código: " + material.getCodigo()); });
    }

    private void validarQuantidades(MaterialRequestDTO dto) {
        if (dto.getQuantidadeDisponivel() == null || dto.getQuantidadeDisponivel() < 0) {
            throw new IllegalArgumentException("A quantidade disponível é obrigatória e não pode ser negativa.");
        }
        validarQuantidadeMinima(dto.getQuantidadeMinima());
    }

    private void validarQuantidadeMinima(Integer quantidadeMinima) {
        if (quantidadeMinima == null || quantidadeMinima < 0) {
            throw new IllegalArgumentException("A quantidade mínima é obrigatória e não pode ser negativa.");
        }
    }

    private Material buscarEntidadePorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
    }
}
