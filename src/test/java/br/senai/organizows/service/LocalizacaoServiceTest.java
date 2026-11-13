package br.senai.organizows.service;

import br.senai.organizows.exception.CodigoLocalizacaoDuplicadoException;
import br.senai.organizows.exception.HierarquiaInvalidaException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.repository.LocalizacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class LocalizacaoServiceTest {

    @Autowired
    private LocalizacaoService localizacaoService;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Test
    @DisplayName("Deve bloquear autorreferência direta ao salvar localização")
    void deveBloquearAutorreferenciaDiretaAoSalvar() {
        Localizacao loc = Localizacao.builder()
                .codigo("LOC-AUTO")
                .nome("Local Auto")
                .build();
        Localizacao salvo = localizacaoRepository.save(loc);

        assertThatThrownBy(() -> {
            salvo.setLocalPai(salvo);
            localizacaoService.salvar(salvo);
        }).isInstanceOf(HierarquiaInvalidaException.class)
          .hasMessageContaining("Uma localização não pode ter a si mesma como pai");
    }

    @Test
    @DisplayName("Deve bloquear ciclo indireto A -> B -> A ao atualizar pai no banco")
    void deveBloquearCicloIndireto() {
        Localizacao a = localizacaoService.salvar(Localizacao.builder()
                .codigo("CICLO-A")
                .nome("Local A")
                .tipo("Galpão")
                .build());

        Localizacao b = localizacaoService.salvar(Localizacao.builder()
                .codigo("CICLO-B")
                .nome("Local B")
                .tipo("Corredor")
                .localPai(a)
                .build());

        // B já tem A como pai no banco. Criamos uma referência rasa de B para a árvore em memória não ter o pai
        Localizacao bRasa = Localizacao.builder()
                .id(b.getId())
                .codigo(b.getCodigo())
                .nome(b.getNome())
                .build();

        a.setLocalPai(bRasa);

        // Deve ser bloqueado pelo Service ao navegar a árvore no banco de dados
        assertThatThrownBy(() -> localizacaoService.salvar(a))
                .isInstanceOf(HierarquiaInvalidaException.class)
                .hasMessageContaining("Ciclo detectado");
    }

    @Test
    @DisplayName("Deve bloquear ciclo indireto em cadeia longa A -> B -> C -> D -> A")
    void deveBloquearCicloIndiretoCadeiaLonga() {
        Localizacao a = localizacaoService.salvar(Localizacao.builder().codigo("C-A").nome("Nível A").build());
        Localizacao b = localizacaoService.salvar(Localizacao.builder().codigo("C-B").nome("Nível B").localPai(a).build());
        Localizacao c = localizacaoService.salvar(Localizacao.builder().codigo("C-C").nome("Nível C").localPai(b).build());
        Localizacao d = localizacaoService.salvar(Localizacao.builder().codigo("C-D").nome("Nível D").localPai(c).build());

        // Referência rasa de D para garantir que a validação ocorre pela consulta no banco
        Localizacao dRasa = Localizacao.builder()
                .id(d.getId())
                .codigo(d.getCodigo())
                .nome(d.getNome())
                .build();

        a.setLocalPai(dRasa);

        assertThatThrownBy(() -> localizacaoService.salvar(a))
                .isInstanceOf(HierarquiaInvalidaException.class)
                .hasMessageContaining("Ciclo detectado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar localização com código duplicado")
    void deveLancarExcecaoParaCodigoDuplicado() {
        localizacaoService.salvar(Localizacao.builder()
                .codigo("COD-UNICO-1")
                .nome("Localização Original")
                .build());

        Localizacao duplicada = Localizacao.builder()
                .codigo("COD-UNICO-1")
                .nome("Outra Localização com mesmo código")
                .build();

        assertThatThrownBy(() -> localizacaoService.salvar(duplicada))
                .isInstanceOf(CodigoLocalizacaoDuplicadoException.class)
                .hasMessageContaining("Já existe uma localização cadastrada com o código");
    }

    @Test
    @DisplayName("Deve permitir atualizar outros campos da mesma localização mantendo o mesmo código")
    void devePermitirAtualizarMesmaLocalizacaoMantendoCodigo() {
        Localizacao salvo = localizacaoService.salvar(Localizacao.builder()
                .codigo("COD-MANTER")
                .nome("Nome Inicial")
                .build());

        salvo.setNome("Nome Atualizado");
        Localizacao atualizado = localizacaoService.salvar(salvo);

        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getCodigo()).isEqualTo("COD-MANTER");
    }

    @Test
    @DisplayName("Deve validar código não nulo e não vazio")
    void deveValidarCodigoNaoNuloEOuVazio() {
        Localizacao semCodigo = Localizacao.builder()
                .nome("Sem Código")
                .build();

        assertThatThrownBy(() -> localizacaoService.salvar(semCodigo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");

        Localizacao codigoVazio = Localizacao.builder()
                .codigo("   ")
                .nome("Código Espaços")
                .build();

        assertThatThrownBy(() -> localizacaoService.salvar(codigoVazio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve montar endereço físico completo em 4 níveis (armário, prateleira, coluna, fileira)")
    void deveMontarEnderecoFisicoCompletoEmMultiplosNiveis() {
        Localizacao armario = localizacaoService.salvar(Localizacao.builder()
                .codigo("ARM-A")
                .nome("Armário A")
                .tipo("Armário")
                .descricao("Armário de componentes")
                .build());

        Localizacao prateleira = localizacaoService.salvar(Localizacao.builder()
                .codigo("PRAT-01")
                .nome("Prateleira 1")
                .tipo("Prateleira")
                .localPai(armario)
                .build());

        Localizacao coluna = localizacaoService.salvar(Localizacao.builder()
                .codigo("COL-02")
                .nome("Coluna 2")
                .tipo("Coluna")
                .localPai(prateleira)
                .build());

        Localizacao fileira = localizacaoService.salvar(Localizacao.builder()
                .codigo("FIL-03")
                .nome("Fileira 3")
                .tipo("Fileira")
                .localPai(coluna)
                .build());

        // Obter endereço completo via Service
        String enderecoCompleto = localizacaoService.obterEnderecoCompleto(fileira.getId());
        assertThat(enderecoCompleto).isEqualTo("Armário A > Prateleira 1 > Coluna 2 > Fileira 3");

        // Obter endereço completo com separador customizado
        String enderecoCustomizado = localizacaoService.obterEnderecoCompleto(fileira.getId(), " / ");
        assertThat(enderecoCustomizado).isEqualTo("Armário A / Prateleira 1 / Coluna 2 / Fileira 3");

        // Obter caminho da hierarquia
        List<Localizacao> caminho = localizacaoService.obterCaminhoHierarquia(fileira.getId());
        assertThat(caminho).hasSize(4);
        assertThat(caminho.get(0).getNome()).isEqualTo("Armário A");
        assertThat(caminho.get(1).getNome()).isEqualTo("Prateleira 1");
        assertThat(caminho.get(2).getNome()).isEqualTo("Coluna 2");
        assertThat(caminho.get(3).getNome()).isEqualTo("Fileira 3");

        // Recuperação simulando um item vinculado à localização final
        assertThat(fileira.getEnderecoCompleto())
                .isEqualTo("Armário A > Prateleira 1 > Coluna 2 > Fileira 3");
    }

    @Test
    @DisplayName("Deve traduzir DataIntegrityViolationException para CodigoLocalizacaoDuplicadoException quando violar uk_localizacao_codigo")
    void deveTraduzirDataIntegrityViolationExceptionParaCodigoDuplicado() {
        LocalizacaoRepository mockRepo = org.mockito.Mockito.mock(LocalizacaoRepository.class);
        LocalizacaoService service = new LocalizacaoService(mockRepo);

        Localizacao loc = Localizacao.builder()
                .codigo("COD-CONCORRENTE")
                .nome("Local Concorrente")
                .build();

        org.mockito.Mockito.when(mockRepo.findByCodigo("COD-CONCORRENTE")).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(mockRepo.save(loc)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                        "Unique index or primary key violation: PUBLIC.UK_LOCALIZACAO_CODIGO"
                )
        );

        assertThatThrownBy(() -> service.salvar(loc))
                .isInstanceOf(CodigoLocalizacaoDuplicadoException.class)
                .hasMessageContaining("Já existe uma localização cadastrada com o código: COD-CONCORRENTE");
    }

    @Test
    @DisplayName("Deve propagar DataIntegrityViolationException original quando violar outra restrição")
    void devePropagarDataIntegrityViolationExceptionQuandoOutraRestricao() {
        LocalizacaoRepository mockRepo = org.mockito.Mockito.mock(LocalizacaoRepository.class);
        LocalizacaoService service = new LocalizacaoService(mockRepo);

        Localizacao loc = Localizacao.builder()
                .codigo("COD-OUTRO")
                .nome("Local Outro")
                .build();

        org.mockito.Mockito.when(mockRepo.findByCodigo("COD-OUTRO")).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(mockRepo.save(loc)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                        "Referential integrity constraint violation: FK_OUTRA_RESTRICAO"
                )
        );

        assertThatThrownBy(() -> service.salvar(loc))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class)
                .hasMessageContaining("FK_OUTRA_RESTRICAO");
    }
}
