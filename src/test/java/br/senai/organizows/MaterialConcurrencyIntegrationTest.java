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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    void deveGarantirIntegridadeSaldoConcorrente() throws InterruptedException {
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
        CountDownLatch endLatch = new CountDownLatch(numThreads * 2);

        // 10 threads somando +5
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    materialService.ajustarSaldo(materialId, 5);
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 10 threads subtraindo -3
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    materialService.ajustarSaldo(materialId, -3);
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        MaterialResponseDTO resultado = materialService.buscarPorId(materialId);
        // Saldo esperado: 100 + (10 * 5) - (10 * 3) = 100 + 50 - 30 = 120
        assertEquals(120, resultado.getQuantidadeDisponivel(),
                "O saldo final deve ser exatamente 120 após operações concorrentes com bloqueio pessimista");
    }

    @Test
    @DisplayName("Deve impedir que concorrência leve o saldo a ficar negativo")
    void deveImpedirSaldoNegativoConcorrente() throws InterruptedException {
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
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        // 10 threads tentando subtrair -2 cada (total demandado = 20, mas só temos 5 disponíveis)
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    materialService.ajustarSaldo(materialId, -2);
                    sucessos.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    falhas.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // No máximo 2 operações de -2 podem ter sucesso (5 - 4 = 1)
        assertEquals(2, sucessos.get(), "Apenas 2 operações devem ter sucesso sem deixar o saldo negativo");
        assertEquals(8, falhas.get(), "8 operações devem ser rejeitadas");

        MaterialResponseDTO resultado = materialService.buscarPorId(materialId);
        assertEquals(1, resultado.getQuantidadeDisponivel());
        assertTrue(resultado.getQuantidadeDisponivel() >= 0, "O saldo nunca deve ser negativo");
    }
}
