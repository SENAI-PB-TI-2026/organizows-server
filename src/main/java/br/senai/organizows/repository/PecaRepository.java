package br.senai.organizows.repository;

import br.senai.organizows.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {

    List<Peca> findByNomeContainingIgnoreCase(String nome);

    List<Peca> findByCategoriaIgnoreCase(String categoria);

    Optional<Peca> findByCodigoIgnoreCase(String codigo);

    @Query("SELECT p FROM Peca p WHERE p.quantidadeEstoque <= p.quantidadeMinima")
    List<Peca> findPecasComEstoqueBaixo();
}
