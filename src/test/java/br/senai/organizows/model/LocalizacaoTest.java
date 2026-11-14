package br.senai.organizows.model;

import br.senai.organizows.exception.HierarquiaInvalidaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalizacaoTest {

    @Test
    @DisplayName("Deve bloquear autorreferencia direta em memoria")
    void deveBloquearAutorreferenciaDireta() {
        Localizacao loc = Localizacao.builder()
                .id(1L)
                .codigo("LOC-01")
                .nome("Armário 1")
                .tipo("Armário")
                .build();

        assertThatThrownBy(() -> loc.setLocalPai(loc))
                .isInstanceOf(HierarquiaInvalidaException.class)
                .hasMessageContaining("Uma localização não pode ter a si mesma como pai");
    }

    @Test
    @DisplayName("Deve bloquear ciclo indireto em memoria A -> B -> A")
    void deveBloquearCicloIndireto() {
        Localizacao a = Localizacao.builder()
                .id(1L)
                .codigo("LOC-A")
                .nome("Armário A")
                .build();

        Localizacao b = Localizacao.builder()
                .id(2L)
                .codigo("LOC-B")
                .nome("Prateleira B")
                .localPai(a)
                .build();

        // Tentar definir B como pai de A criando ciclo A -> B -> A
        assertThatThrownBy(() -> a.setLocalPai(b))
                .isInstanceOf(HierarquiaInvalidaException.class)
                .hasMessageContaining("Ciclo detectado");
    }

    @Test
    @DisplayName("Deve bloquear ciclo indireto de múltiplos níveis A -> B -> C -> A")
    void deveBloquearCicloIndiretoMultiplosNiveis() {
        Localizacao a = Localizacao.builder().id(1L).codigo("A").nome("Nível A").build();
        Localizacao b = Localizacao.builder().id(2L).codigo("B").nome("Nível B").localPai(a).build();
        Localizacao c = Localizacao.builder().id(3L).codigo("C").nome("Nível C").localPai(b).build();

        // Tentar colocar C como pai de A fechando o ciclo
        assertThatThrownBy(() -> a.setLocalPai(c))
                .isInstanceOf(HierarquiaInvalidaException.class)
                .hasMessageContaining("Ciclo detectado");
    }

    @Test
    @DisplayName("Deve preservar o localPai anterior quando a validação de hierarquia falhar")
    void devePreservarLocalPaiAnteriorQuandoHierarquiaForInvalida() {
        Localizacao raiz = Localizacao.builder().id(1L).codigo("RAIZ").nome("Raiz").build();
        Localizacao filha = Localizacao.builder().id(2L).codigo("FILHA").nome("Filha").localPai(raiz).build();

        // Estado inicial: filha tem raiz como pai
        assertThat(filha.getLocalPai()).isEqualTo(raiz);

        // Tentativa inválida de autorreferência direta
        assertThatThrownBy(() -> filha.setLocalPai(filha))
                .isInstanceOf(HierarquiaInvalidaException.class);

        // Deve manter a raiz como pai, sem ter sido corrompido para a própria filha
        assertThat(filha.getLocalPai()).isEqualTo(raiz);
    }

    @Test
    @DisplayName("Deve montar endereço físico completo em 4 níveis (armário, prateleira, coluna, fileira)")
    void deveMontarEnderecoFisicoCompletoEmMultiplosNiveis() {
        Localizacao armario = Localizacao.builder()
                .id(1L)
                .codigo("ARM-01")
                .nome("Armário 01")
                .tipo("Armário")
                .build();

        Localizacao prateleira = Localizacao.builder()
                .id(2L)
                .codigo("PRAT-03")
                .nome("Prateleira 03")
                .tipo("Prateleira")
                .localPai(armario)
                .build();

        Localizacao coluna = Localizacao.builder()
                .id(3L)
                .codigo("COL-B")
                .nome("Coluna B")
                .tipo("Coluna")
                .localPai(prateleira)
                .build();

        Localizacao fileira = Localizacao.builder()
                .id(4L)
                .codigo("FIL-02")
                .nome("Fileira 02")
                .tipo("Fileira")
                .localPai(coluna)
                .build();

        // Endereço completo padrão com nomes
        assertThat(fileira.getEnderecoCompleto())
                .isEqualTo("Armário 01 > Prateleira 03 > Coluna B > Fileira 02");

        // Endereço com separador customizado
        assertThat(fileira.getEnderecoCompleto(" / "))
                .isEqualTo("Armário 01 / Prateleira 03 / Coluna B / Fileira 02");

        // Endereço com códigos
        assertThat(fileira.getEnderecoCodigoCompleto())
                .isEqualTo("ARM-01 > PRAT-03 > COL-B > FIL-02");

        assertThat(fileira.getEnderecoCodigoCompleto("."))
                .isEqualTo("ARM-01.PRAT-03.COL-B.FIL-02");

        // Caminho da hierarquia em lista
        List<Localizacao> caminho = fileira.getCaminhoHierarquia();
        assertThat(caminho).containsExactly(armario, prateleira, coluna, fileira);
    }

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Deve validar nome não branco e limite de 255 caracteres")
    void deveValidarRestricoesDoNome() {
        Localizacao nomeNulo = Localizacao.builder().codigo("C1").build();
        assertThat(validator.validate(nomeNulo))
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("nome");

        Localizacao nomeBranco = Localizacao.builder().codigo("C1").nome("   ").build();
        assertThat(validator.validate(nomeBranco))
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("nome");

        Localizacao nomeLongo = Localizacao.builder().codigo("C1").nome("a".repeat(256)).build();
        assertThat(validator.validate(nomeLongo))
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("nome");
    }

    @Test
    @DisplayName("Deve validar tipo com limite de 100 caracteres")
    void deveValidarLimiteTipo() {
        Localizacao tipoLongo = Localizacao.builder()
                .codigo("C1")
                .nome("Valido")
                .tipo("t".repeat(101))
                .build();

        assertThat(validator.validate(tipoLongo))
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("tipo");
    }

    @Test
    @DisplayName("Deve validar descricao com limite de 255 caracteres")
    void deveValidarLimiteDescricao() {
        Localizacao descLonga = Localizacao.builder()
                .codigo("C1")
                .nome("Valido")
                .descricao("d".repeat(256))
                .build();

        assertThat(validator.validate(descLonga))
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("descricao");
    }

    @Test
    @DisplayName("Deve validar localizacao valida sem violacoes")
    void deveValidarLocalizacaoCorreta() {
        Localizacao valida = Localizacao.builder()
                .codigo("C1")
                .nome("Nome Válido")
                .tipo("Armário")
                .descricao("Descrição Válida")
                .build();

        assertThat(validator.validate(valida)).isEmpty();
    }
}
