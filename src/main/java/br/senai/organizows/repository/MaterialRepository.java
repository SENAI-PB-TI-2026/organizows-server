package br.senai.organizows.repository;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Bloqueio pessimista para evitar concorrência no ajuste de saldo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdForUpdate(@Param("id") Long id);

    // Consulta para o alerta de reposição: retorna apenas itens com saldo menor ou igual ao mínimo
    @Query("SELECT m FROM Material m WHERE m.quantidadeDisponivel <= m.quantidadeMinima")
    List<Material> findMateriaisParaReposicao();

    List<Material> findByCategoria(CategoriaMaterial categoria);

    List<Material> findByStatus(StatusMaterial status);
}