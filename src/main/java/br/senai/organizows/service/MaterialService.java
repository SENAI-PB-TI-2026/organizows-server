package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Local;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.MaterialRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public MaterialResponseDTO criar(MaterialRequestDTO dto) {
        Material material = new Material();
        preencherDados(material, dto);

        Material salvo = materialRepository.save(material);
        return new MaterialResponseDTO(salvo);
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

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dto) {
        Material material = buscarEntidadePorId(id);
        preencherDados(material, dto);

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
        if (deltaQuantidade == null) {
            throw new IllegalArgumentException("A quantidade de ajuste não pode ser nula.");
        }

        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + materialId));

        int novoSaldo = material.getQuantidadeDisponivel() + deltaQuantidade;

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
        material.setDescricao(dto.getDescricao());
        material.setCategoria(dto.getCategoria());
        material.setStatus(dto.getStatus());
        material.setQuantidadeDisponivel(dto.getQuantidadeDisponivel());
        material.setQuantidadeMinima(dto.getQuantidadeMinima());

        if (dto.getLocalId() != null) {
            Local localRef = entityManager.getReference(Local.class, dto.getLocalId());
            material.setLocal(localRef);
        } else {
            material.setLocal(null);
        }
    }

    private Material buscarEntidadePorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
    }
}