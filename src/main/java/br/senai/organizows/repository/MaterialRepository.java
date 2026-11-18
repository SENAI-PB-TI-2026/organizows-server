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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT m FROM Material m WHERE m.quantidadeDisponivel <= m.quantidadeMinima")
    List<Material> findMateriaisParaReposicao();

    Optional<Material> findByCodigo(String codigo);

    List<Material> findByGrupoIgnoreCase(String grupo);

    List<Material> findByCategoria(CategoriaMaterial categoria);

    List<Material> findByStatus(StatusMaterial status);
}
