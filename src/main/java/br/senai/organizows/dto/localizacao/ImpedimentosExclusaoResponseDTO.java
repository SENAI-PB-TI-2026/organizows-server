package br.senai.organizows.dto.localizacao;

import java.util.List;

public record ImpedimentosExclusaoResponseDTO(boolean podeExcluir, List<String> motivos) {
}
