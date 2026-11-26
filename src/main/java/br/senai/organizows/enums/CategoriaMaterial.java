package br.senai.organizows.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public enum CategoriaMaterial {
    PERIFERICO("Periféricos", "Entrada", "Saída"),
    HARDWARE("Hardware", "Armazenamento", "Processamento"),
    CABO("Cabos", "Rede", "Conectividade", "Alimentação"),
    COMPONENTE_ELETRONICO("Componentes", "Passivos", "Semicondutores"),
    EQUIPAMENTO("Equipamentos", "Medição", "Informática"),
    FERRAMENTA("Ferramentas", "Manuais", "Elétricas"),
    CONSUMIVEL("Consumíveis", "Limpeza", "Escritório");

    private final String grupo;
    private final List<String> subgrupos;

    CategoriaMaterial(String grupo, String... subgrupos) {
        this.grupo = grupo;
        this.subgrupos = List.of(subgrupos);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CategoriaMaterial porNome(String nome) {
        return CategoriaMaterial.valueOf(nome);
    }

    public String getGrupo() {
        return grupo;
    }

    public List<String> getSubgrupos() {
        return subgrupos;
    }

    public boolean aceitaGrupo(String grupo) {
        return this.grupo.equalsIgnoreCase(grupo);
    }

    public boolean aceitaSubgrupo(String subgrupo) {
        return subgrupos.stream().anyMatch(subgrupo::equalsIgnoreCase);
    }
}
