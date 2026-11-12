package br.senai.organizows.repository;

import br.senai.organizows.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    Optional<Localizacao> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Localizacao> findByLocalPaiIsNull();

    List<Localizacao> findByLocalPaiId(Long localPaiId);
}
