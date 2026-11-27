package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;

public interface MaterialCadastroDTO {

    String getCodigo();

    String getNome();

    String getDescricao();

    String getCaracteristicas();

    String getModelo();

    String getEspecificacoesTecnicas();

    String getGrupo();

    String getSubgrupo();

    CategoriaMaterial getCategoria();

    StatusMaterial getStatus();

    Long getLocalId();

    Integer getQuantidadeMinima();
}
