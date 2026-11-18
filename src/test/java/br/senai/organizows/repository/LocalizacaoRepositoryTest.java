package br.senai.organizows.repository;

import br.senai.organizows.model.Localizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class LocalizacaoRepositoryTest {

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Test
    @DisplayName("Deve salvar e buscar uma localizacao raiz no perfil H2")
    void deveSalvarEBuscarLocalizacaoRaiz() {
        Localizacao predio = Localizacao.builder()
                .codigo("BLOCO-A")
                .nome("Bloco A")
                .tipo("Edifício")
                .descricao("Prédio de salas de aula e laboratórios")
                .build();

        Localizacao predioSalvo = localizacaoRepository.save(predio);

        assertThat(predioSalvo.getId()).isNotNull();

        Optional<Localizacao> encontrado = localizacaoRepository.findById(predioSalvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getCodigo()).isEqualTo("BLOCO-A");
        assertThat(encontrado.get().getNome()).isEqualTo("Bloco A");
        assertThat(encontrado.get().getTipo()).isEqualTo("Edifício");
        assertThat(encontrado.get().getDescricao()).isEqualTo("Prédio de salas de aula e laboratórios");
        assertThat(encontrado.get().getLocalPai()).isNull();
    }

    @Test
    @DisplayName("Deve salvar e buscar uma localizacao filha com referencia ao local pai")
    void deveSalvarEBuscarLocalizacaoComLocalPai() {
        Localizacao predio = Localizacao.builder()
                .codigo("BLOCO-B")
                .nome("Bloco B")
                .tipo("Edifício")
                .descricao("Prédio administrativo")
                .build();
        Localizacao predioSalvo = localizacaoRepository.save(predio);

        Localizacao sala = Localizacao.builder()
                .codigo("SALA-204")
                .nome("Sala 204")
                .tipo("Sala")
                .descricao("Laboratório de informática")
                .localPai(predioSalvo)
                .build();
        Localizacao salaSalva = localizacaoRepository.save(sala);

        assertThat(salaSalva.getId()).isNotNull();

        Optional<Localizacao> salaEncontrada = localizacaoRepository.findById(salaSalva.getId());

        assertThat(salaEncontrada).isPresent();
        assertThat(salaEncontrada.get().getCodigo()).isEqualTo("SALA-204");
        assertThat(salaEncontrada.get().getNome()).isEqualTo("Sala 204");
        assertThat(salaEncontrada.get().getLocalPai()).isNotNull();
        assertThat(salaEncontrada.get().getLocalPai().getId()).isEqualTo(predioSalvo.getId());
        assertThat(salaEncontrada.get().getLocalPai().getNome()).isEqualTo("Bloco B");
        assertThat(salaEncontrada.get().getLocalizacaoPai().getId()).isEqualTo(predioSalvo.getId());

        List<Localizacao> filhas = localizacaoRepository.findByLocalPaiId(predioSalvo.getId());
        assertThat(filhas).hasSize(1);
        assertThat(filhas.get(0).getNome()).isEqualTo("Sala 204");

        List<Localizacao> raizes = localizacaoRepository.findByLocalPaiIsNull();
        assertThat(raizes).extracting(Localizacao::getNome).contains("Bloco B");
    }

    @Test
    @DisplayName("Deve buscar por codigo e verificar existencia")
    void deveBuscarPorCodigoEVerificarExistencia() {
        Localizacao loc = Localizacao.builder()
                .codigo("ARM-01")
                .nome("Armário 1")
                .tipo("Armário")
                .build();
        localizacaoRepository.save(loc);

        Optional<Localizacao> encontrado = localizacaoRepository.findByCodigo("ARM-01");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Armário 1");

        assertThat(localizacaoRepository.existsByCodigo("ARM-01")).isTrue();
        assertThat(localizacaoRepository.existsByCodigo("INEXISTENTE")).isFalse();
    }

    @Test
    @DisplayName("Deve impedir salvar localizacoes com codigo duplicado violando constraint unica")
    void deveImpedirSalvarLocalizacoesComCodigoDuplicado() {
        Localizacao loc1 = Localizacao.builder()
                .codigo("COD-DUP")
                .nome("Local 1")
                .build();
        localizacaoRepository.saveAndFlush(loc1);

        Localizacao loc2 = Localizacao.builder()
                .codigo("COD-DUP")
                .nome("Local 2")
                .build();

        assertThatThrownBy(() -> localizacaoRepository.saveAndFlush(loc2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve atualizar e deletar uma localizacao")
    void deveAtualizarEDeletarLocalizacao() {
        Localizacao almoxarifado = Localizacao.builder()
                .codigo("ALMOX-01")
                .nome("Almoxarifado")
                .tipo("Depósito")
                .descricao("Depósito geral de materiais")
                .build();
        Localizacao salvo = localizacaoRepository.save(almoxarifado);

        salvo.setNome("Almoxarifado Central");
        salvo.setDescricao("Depósito central atualizado");
        Localizacao atualizado = localizacaoRepository.save(salvo);

        assertThat(atualizado.getNome()).isEqualTo("Almoxarifado Central");
        assertThat(atualizado.getDescricao()).isEqualTo("Depósito central atualizado");

        localizacaoRepository.delete(atualizado);

        Optional<Localizacao> deletado = localizacaoRepository.findById(atualizado.getId());
        assertThat(deletado).isEmpty();
    }
}
