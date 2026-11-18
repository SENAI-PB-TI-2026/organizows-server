package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.exception.CodigoMaterialDuplicadoException;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        if (materialRepository.existsByCodigo(dto.getCodigo())) {
            throw new CodigoMaterialDuplicadoException("Já existe um material cadastrado com o código: " + dto.getCodigo());
        }

        validarQuantidades(dto.getQuantidadeDisponivel(), dto.getQuantidadeMinima());

        Material material = new Material();
        preencherDados(material, dto);

        Material salvo = materialRepository.save(material);
        return new MaterialResponseDTO(salvo);
    }

    public List<MaterialResponseDTO> listar(String nome, CategoriaMaterial categoria, StatusMaterial status, String codigo, Long localId) {
        if (codigo != null && !codigo.isBlank()) {
            return materialRepository.findByCodigo(codigo)
                    .map(m -> List.of(new MaterialResponseDTO(m)))
                    .orElse(List.of());
        }
        if (localId != null) {
            return materialRepository.findByLocalizacaoId(localId)
                    .stream()
                    .map(MaterialResponseDTO::new)
                    .collect(Collectors.toList());
        }
        if (categoria != null) {
            return materialRepository.findByCategoria(categoria)
                    .stream()
                    .map(MaterialResponseDTO::new)
                    .collect(Collectors.toList());
        }
        if (status != null) {
            return materialRepository.findByStatus(status)
                    .stream()
                    .map(MaterialResponseDTO::new)
                    .collect(Collectors.toList());
        }
        if (nome != null && !nome.isBlank()) {
            return materialRepository.findByNomeContainingIgnoreCase(nome)
                    .stream()
                    .map(MaterialResponseDTO::new)
                    .collect(Collectors.toList());
        }
        return listarTodos();
    }

    public List<MaterialResponseDTO> listarTodos() {
        return materialRepository.findAll()
                .stream()
                .map(MaterialResponseDTO::new)
                .collect(Collectors.toList());
    }

    public MaterialResponseDTO buscarPorId(Long id) {
        Material material = buscarEntidadePorId(id);
        return new MaterialResponseDTO(material);
    }

    public MaterialResponseDTO buscarPorCodigo(String codigo) {
        Material material = materialRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com código: " + codigo));
        return new MaterialResponseDTO(material);
    }

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dto) {
        Material material = buscarEntidadePorId(id);

        if (!material.getCodigo().equals(dto.getCodigo()) && materialRepository.existsByCodigo(dto.getCodigo())) {
            throw new CodigoMaterialDuplicadoException("Já existe outro material cadastrado com o código: " + dto.getCodigo());
        }

        if (dto.getQuantidadeMinima() != null && dto.getQuantidadeMinima() < 0) {
            throw new IllegalArgumentException("A quantidade mínima não pode ser negativa.");
        }

        material.setCodigo(dto.getCodigo());
        material.setNome(dto.getNome());
        material.setDescricao(dto.getDescricao());
        material.setCategoria(dto.getCategoria());
        material.setSubgrupo(dto.getSubgrupo());
        material.setModelo(dto.getModelo());
        material.setEspecificacoesTecnicas(dto.getEspecificacoesTecnicas());
        material.setStatus(dto.getStatus());
        material.setQuantidadeMinima(dto.getQuantidadeMinima());
        // Regra de integridade do estoque: a quantidade disponível é preservada durante o PUT de cadastro,
        // garantindo que alterações cadastrais não burlem o fluxo e auditoria de movimentações/saldo.

        if (dto.getLocalId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(dto.getLocalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada com ID: " + dto.getLocalId()));
            material.setLocalizacao(localizacao);
        } else {
            material.setLocalizacao(null);
        }

        Material atualizado = materialRepository.save(material);
        return new MaterialResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Material material = buscarEntidadePorId(id);
        materialRepository.delete(material);
    }

    public List<MaterialResponseDTO> listarAlertaReposicao() {
        return materialRepository.findMateriaisParaReposicao()
                .stream()
                .map(MaterialResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaterialResponseDTO ajustarSaldo(Long materialId, Integer deltaQuantidade) {
        if (deltaQuantidade == null || deltaQuantidade == 0) {
            throw new IllegalArgumentException("A quantidade de ajuste não pode ser nula ou zero.");
        }

        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + materialId));

        int novoSaldo;
        try {
            novoSaldo = Math.addExact(material.getQuantidadeDisponivel(), deltaQuantidade);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Operação resulta em estouro numérico (overflow) de saldo.");
        }

        if (novoSaldo < 0) {
            throw new IllegalArgumentException(
                    "Operação negada: o saldo disponível (" + material.getQuantidadeDisponivel() +
                            ") não pode ficar negativo ao ajustar " + deltaQuantidade + " unidades."
            );
        }

        material.setQuantidadeDisponivel(novoSaldo);
        Material atualizado = materialRepository.save(material);
        return new MaterialResponseDTO(atualizado);
    }

    private void preencherDados(Material material, MaterialRequestDTO dto) {
        material.setCodigo(dto.getCodigo());
        material.setNome(dto.getNome());
        material.setDescricao(dto.getDescricao());
        material.setCategoria(dto.getCategoria());
        material.setSubgrupo(dto.getSubgrupo());
        material.setModelo(dto.getModelo());
        material.setEspecificacoesTecnicas(dto.getEspecificacoesTecnicas());
        material.setStatus(dto.getStatus());
        material.setQuantidadeDisponivel(dto.getQuantidadeDisponivel() != null ? dto.getQuantidadeDisponivel() : 0);
        material.setQuantidadeMinima(dto.getQuantidadeMinima() != null ? dto.getQuantidadeMinima() : 0);

        if (dto.getLocalId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(dto.getLocalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada com ID: " + dto.getLocalId()));
            material.setLocalizacao(localizacao);
        } else {
            material.setLocalizacao(null);
        }
    }

    private void validarQuantidades(Integer quantidadeDisponivel, Integer quantidadeMinima) {
        if (quantidadeDisponivel != null && quantidadeDisponivel < 0) {
            throw new IllegalArgumentException("A quantidade disponível não pode ser negativa.");
        }
        if (quantidadeMinima != null && quantidadeMinima < 0) {
            throw new IllegalArgumentException("A quantidade mínima não pode ser negativa.");
        }
    }

    private Material buscarEntidadePorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
    }
}