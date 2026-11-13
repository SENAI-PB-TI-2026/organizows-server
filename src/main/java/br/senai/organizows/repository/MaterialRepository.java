package br.senai.organizows.repository;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Consulta para o alerta de reposição: retorna apenas itens com saldo menor ou igual ao mínimo
    @Query("SELECT m FROM Material m WHERE m.quantidadeDisponivel <= m.quantidadeMinima")
    List<Material> findMateriaisParaReposicao();

    List<Material> findByCategoria(CategoriaMaterial categoria);

    List<Material> findByStatus(StatusMaterial status);
}