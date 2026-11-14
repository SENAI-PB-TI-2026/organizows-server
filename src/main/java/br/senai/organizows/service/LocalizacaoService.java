package br.senai.organizows.service;

import br.senai.organizows.dto.localizacao.LocalizacaoRequestDTO;
import br.senai.organizows.dto.localizacao.LocalizacaoResponseDTO;
import br.senai.organizows.exception.CodigoLocalizacaoDuplicadoException;
import br.senai.organizows.exception.HierarquiaInvalidaException;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.repository.LocalizacaoRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LocalizacaoService {

    private final LocalizacaoRepository localizacaoRepository;

    public LocalizacaoService(LocalizacaoRepository localizacaoRepository) {
        this.localizacaoRepository = localizacaoRepository;
    }

    public Localizacao salvar(Localizacao localizacao) {
        validarCodigoUnico(localizacao);
        validarHierarquia(localizacao);
        try {
            return localizacaoRepository.save(localizacao);
        } catch (DataIntegrityViolationException ex) {
            if (isViolacaoRestricaoCodigo(ex)) {
                throw new CodigoLocalizacaoDuplicadoException(
                        "Já existe uma localização cadastrada com o código: " + localizacao.getCodigo()
                );
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Localizacao> buscarPorId(Long id) {
        return localizacaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Localizacao> buscarPorCodigo(String codigo) {
        return localizacaoRepository.findByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public List<Localizacao> listarTodas() {
        return localizacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Localizacao> listarRaizes() {
        return localizacaoRepository.findByLocalPaiIsNull();
    }

    @Transactional(readOnly = true)
    public List<Localizacao> listarFilhas(Long localPaiId) {
        return localizacaoRepository.findByLocalPaiId(localPaiId);
    }

    public LocalizacaoResponseDTO criar(LocalizacaoRequestDTO dto) {
        Localizacao pai = null;
        if (dto.getLocalPaiId() != null) {
            pai = localizacaoRepository.findById(dto.getLocalPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localização pai não encontrada com ID: " + dto.getLocalPaiId()));
        }

        Localizacao nova = Localizacao.builder()
                .codigo(dto.getCodigo())
                .nome(dto.getNome())
                .tipo(dto.getTipo())
                .descricao(dto.getDescricao())
                .localPai(pai)
                .build();

        Localizacao salva = salvar(nova);
        return LocalizacaoResponseDTO.fromEntity(salva);
    }

    public LocalizacaoResponseDTO atualizar(Long id, LocalizacaoRequestDTO dto) {
        Localizacao existente = localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada com ID: " + id));

        Localizacao pai = null;
        if (dto.getLocalPaiId() != null) {
            pai = localizacaoRepository.findById(dto.getLocalPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localização pai não encontrada com ID: " + dto.getLocalPaiId()));
        }

        existente.setCodigo(dto.getCodigo());
        existente.setNome(dto.getNome());
        existente.setTipo(dto.getTipo());
        existente.setDescricao(dto.getDescricao());
        existente.setLocalPai(pai);

        Localizacao salva = salvar(existente);
        return LocalizacaoResponseDTO.fromEntity(salva);
    }

    public void excluir(Long id) {
        if (!localizacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Localização não encontrada com ID: " + id);
        }
        localizacaoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String obterEnderecoCompleto(Long id) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada com id: " + id));
        return localizacao.getEnderecoCompleto();
    }

    @Transactional(readOnly = true)
    public String obterEnderecoCompleto(Long id, String separador) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada com id: " + id));
        return localizacao.getEnderecoCompleto(separador);
    }

    @Transactional(readOnly = true)
    public List<Localizacao> obterCaminhoHierarquia(Long id) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada com id: " + id));
        return localizacao.getCaminhoHierarquia();
    }

    private void validarCodigoUnico(Localizacao localizacao) {
        if (localizacao.getCodigo() == null || localizacao.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("O código da localização não pode ser nulo ou vazio.");
        }

        Optional<Localizacao> existente = localizacaoRepository.findByCodigo(localizacao.getCodigo());
        if (existente.isPresent()) {
            boolean isMesmoRegistro = localizacao.getId() != null && localizacao.getId().equals(existente.get().getId());
            if (!isMesmoRegistro) {
                throw new CodigoLocalizacaoDuplicadoException(
                        "Já existe uma localização cadastrada com o código: " + localizacao.getCodigo()
                );
            }
        }
    }

    private void validarHierarquia(Localizacao localizacao) {
        if (localizacao.getLocalPai() == null) {
            return;
        }

        // 1. Autorreferência direta em memória
        if (localizacao == localizacao.getLocalPai()) {
            throw new HierarquiaInvalidaException("Uma localização não pode ter a si mesma como pai.");
        }

        // 2. Autorreferência direta por ID
        if (localizacao.getId() != null && localizacao.getLocalPai().getId() != null) {
            if (localizacao.getId().equals(localizacao.getLocalPai().getId())) {
                throw new HierarquiaInvalidaException("Uma localização não pode ter a si mesma como pai.");
            }
        }

        // 3. Prevenção de ciclo indireto: navegar recursivamente pela árvore no banco
        if (localizacao.getId() != null) {
            Long proximoPaiId = localizacao.getLocalPai().getId();
            Set<Long> visitados = new HashSet<>();
            visitados.add(localizacao.getId());

            while (proximoPaiId != null) {
                if (!visitados.add(proximoPaiId)) {
                    throw new HierarquiaInvalidaException(
                            "Ciclo detectado na hierarquia de localizações: uma localização não pode ser descendente de si mesma."
                    );
                }

                Optional<Localizacao> paiOpt = localizacaoRepository.findById(proximoPaiId);
                if (paiOpt.isPresent() && paiOpt.get().getLocalPai() != null) {
                    proximoPaiId = paiOpt.get().getLocalPai().getId();
                } else {
                    proximoPaiId = null;
                }
            }
        }
    }

    private boolean isViolacaoRestricaoCodigo(DataIntegrityViolationException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                if ("uk_localizacao_codigo".equalsIgnoreCase(cve.getConstraintName())) {
                    return true;
                }
            }
            String message = cause.getMessage();
            if (message != null && message.toLowerCase().contains("uk_localizacao_codigo")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
