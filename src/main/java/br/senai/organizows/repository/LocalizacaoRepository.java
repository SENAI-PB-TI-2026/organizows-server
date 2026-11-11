package br.senai.organizows.repository;

import br.senai.organizows.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    List<Localizacao> findByLocalPaiIsNull();

    List<Localizacao> findByLocalPaiId(Long localPaiId);
}
