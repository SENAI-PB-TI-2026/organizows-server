package br.senai.organizows.repository;

import br.senai.organizows.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByMaterialIdOrderByDataHoraDesc(Long materialId);
}
