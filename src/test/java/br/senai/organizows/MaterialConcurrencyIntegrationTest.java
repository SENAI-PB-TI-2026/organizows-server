package br.senai.organizows;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import br.senai.organizows.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MaterialConcurrencyIntegrationTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    private Long localizacaoId;

    @BeforeEach
    void setUp() {
        materialRepository.deleteAll();
        localizacaoRepository.deleteAll();

        Localizacao loc = Localizacao.builder()
                .codigo("CONC-LOC-1")
                .nome("Prateleira de Concorrência")
                .tipo("ESTOQUE")
                .build();
        localizacaoId = localizacaoRepository.save(loc).getId();
    }

    @Test
    @DisplayName("Deve garantir integridade de saldo sob concorrência usando bloqueio pessimista")
    void deveGarantirIntegridadeSaldoConcorrente() throws Exception {
        MaterialRequestDTO dto = MaterialRequestDTO.builder()
                .codigo("CONC-MAT-1")
                .nome("Material Teste Concorrência")
                .descricao("Material para teste concorrente")
                .grupo("Testes")
                .categoria(CategoriaMaterial.CONSUMIVEL)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(100)
                .quantidadeMinima(10)
                .localId(localizacaoId)
                .build();

        MaterialResponseDTO criado = materialService.criar(dto);
        Long materialId = criado.getId();

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> tarefas = new ArrayList<>();

        try {
            // 10 threads somando +5
            for (int i = 0; i < numThreads; i++) {
                tarefas.add(executor.submit(() -> {
                    aguardarInicio(startLatch);
                    materialService.ajustarSaldo(materialId, 5);
                }));
            }

            // 10 threads subtraindo -3
            for (int i = 0; i < numThreads; i++) {
                tarefas.add(executor.submit(() -> {
                    aguardarInicio(startLatch);
                    materialService.ajustarSaldo(materialId, -3);
                }));
            }

            startLatch.countDown();
            for (Future<?> tarefa : tarefas) {
                tarefa.get(15, TimeUnit.SECONDS);
            }
        } finally {
            startLatch.countDown();
            encerrarExecutor(executor);
        }

        MaterialResponseDTO resultado = materialService.buscarPorId(materialId);
        // Saldo esperado: 100 + (10 * 5) - (10 * 3) = 100 + 50 - 30 = 120
        assertEquals(120, resultado.getQuantidadeDisponivel(),
                "O saldo final deve ser exatamente 120 após operações concorrentes com bloqueio pessimista");
    }

    @Test
    @DisplayName("Deve impedir que concorrência leve o saldo a ficar negativo")
    void deveImpedirSaldoNegativoConcorrente() throws Exception {
        MaterialRequestDTO dto = MaterialRequestDTO.builder()
                .codigo("CONC-MAT-2")
                .nome("Material Teste Estoque Limitado")
                .descricao("Material para teste de estoque")
                .grupo("Testes")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(5)
                .quantidadeMinima(2)
                .localId(localizacaoId)
                .build();

        MaterialResponseDTO criado = materialService.criar(dto);
        Long materialId = criado.getId();

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> tarefas = new ArrayList<>();

        int sucessos = 0;
        int rejeicoesPorSaldoInsuficiente = 0;
        try {
            // 10 threads tentando subtrair -2 cada (total demandado = 20, mas só temos 5 disponíveis)
            for (int i = 0; i < numThreads; i++) {
                tarefas.add(executor.submit(() -> {
                    aguardarInicio(startLatch);
                    try {
                        materialService.ajustarSaldo(materialId, -2);
                        return true;
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage() == null || !e.getMessage().contains("saldo")) {
                            throw e;
                        }
                        return false;
                    }
                }));
            }

            startLatch.countDown();
            for (Future<Boolean> tarefa : tarefas) {
                if (tarefa.get(15, TimeUnit.SECONDS)) {
                    sucessos++;
                } else {
                    rejeicoesPorSaldoInsuficiente++;
                }
            }
        } finally {
            encerrarExecutor(executor);
        }

        // No máximo 2 operações de -2 podem ter sucesso (5 - 4 = 1)
        assertEquals(2, sucessos, "Apenas 2 operações devem ter sucesso sem deixar o saldo negativo");
        assertEquals(8, rejeicoesPorSaldoInsuficiente, "8 operações devem ser rejeitadas por saldo insuficiente");

        MaterialResponseDTO resultado = materialService.buscarPorId(materialId);
        assertEquals(1, resultado.getQuantidadeDisponivel());
        assertTrue(resultado.getQuantidadeDisponivel() >= 0, "O saldo nunca deve ser negativo");
    }

    private void aguardarInicio(CountDownLatch startLatch) {
        try {
            if (!startLatch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("As tarefas concorrentes não iniciaram no tempo esperado.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A tarefa concorrente foi interrompida antes do início.", e);
        }
    }

    private void encerrarExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
                "O executor concorrente não finalizou dentro do limite de tempo.");
    }
}
