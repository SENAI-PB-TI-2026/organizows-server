package br.senai.organizows.dto;

import java.util.List;

public record ClassificacaoMaterialResponseDTO(
        String categoria,
        String grupo,
        List<String> subgrupos
) {
}
