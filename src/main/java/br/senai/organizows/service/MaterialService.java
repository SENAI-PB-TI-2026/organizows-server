package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.ClassificacaoMaterialResponseDTO;
import br.senai.organizows.dto.MaterialCadastroDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.MaterialUpdateRequestDTO;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.exception.CodigoMaterialDuplicadoException;
import br.senai.organizows.exception.HierarquiaInvalidaException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private static final String NUMERO = "[0-9]+(?:[.,][0-9]+)?";
    private static final String UNIDADE_ELETRICA = "[kMmµμun]?(?:Ω|W|V|A|Hz|F|H)";
    private static final Pattern TERMOS_BUSCA = Pattern.compile(
            "(?<![\\p{L}\\p{N}])(?<medida>" + NUMERO + ")\\s*(?<unidade>" + UNIDADE_ELETRICA
                    + ")(?![\\p{L}\\p{N}])|(?<numero>" + NUMERO + ")|\\p{L}+",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final MaterialRepository materialRepository;
    private final LocalizacaoRepository localizacaoRepository;

    public MaterialService(MaterialRepository materialRepository, LocalizacaoRepository localizacaoRepository) {
        this.materialRepository = materialRepository;
        this.localizacaoRepository = localizacaoRepository;
    }

    @Transactional
    public MaterialResponseDTO criar(MaterialRequestDTO dto) {
        validarQuantidades(dto);
        if (materialRepository.existsByCodigo(dto.getCodigo().trim())) {
            throw new CodigoMaterialDuplicadoException(
                    "Já existe um material cadastrado com o código: " + dto.getCodigo().trim());
        }
        Material material = new Material();
        preencherCadastro(material, dto);
        material.setQuantidadeDisponivel(dto.getQuantidadeDisponivel());
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    public List<MaterialResponseDTO> listarTodos() {
        return materialRepository.findAll().stream().map(MaterialResponseDTO::new).toList();
    }

    public List<ClassificacaoMaterialResponseDTO> listarClassificacoes() {
        return Arrays.stream(CategoriaMaterial.values())
                .map(categoria -> new ClassificacaoMaterialResponseDTO(
                        categoria.name(), categoria.getGrupo(), categoria.getSubgrupos()))
                .toList();
    }

    public List<MaterialResponseDTO> listarPorGrupo(String grupo) {
        return materialRepository.findByGrupoIgnoreCase(grupo.trim()).stream()
                .map(MaterialResponseDTO::new).toList();
    }

    public List<MaterialResponseDTO> buscar(String consulta, String grupo) {
        List<String> termos = termosDeBusca(consulta).stream()
                .filter(termo -> !termo.equals("de"))
                .distinct()
                .toList();
        if (termos.isEmpty()) {
            return List.of();
        }

        List<Material> materiais = grupo == null || grupo.isBlank()
                ? materialRepository.findAll()
                : materialRepository.findByGrupoIgnoreCase(grupo.trim());
        return materiais.stream()
                .filter(material -> correspondeAosTermos(material, termos))
                .sorted(Comparator.comparingLong((Material material) -> relevanciaNoNome(material, termos))
                        .reversed().thenComparing(Material::getId))
                .map(MaterialResponseDTO::new)
                .toList();
    }

    private boolean correspondeAosTermos(Material material, List<String> termos) {
        List<String> campos = Stream.of(
                material.getNome(),
                Objects.toString(material.getModelo(), ""),
                Objects.toString(material.getEspecificacoesTecnicas(), ""))
                .flatMap(campo -> termosDeBusca(campo).stream())
                .toList();
        return termos.stream().allMatch(termo -> contemTermo(campos, termo));
    }

    private long relevanciaNoNome(Material material, List<String> termos) {
        List<String> nome = termosDeBusca(material.getNome());
        return termos.stream().filter(termo -> contemTermo(nome, termo)).count();
    }

    private boolean contemTermo(List<String> campos, String termo) {
        // Consultas por valor ou unidade isolados também encontram medidas completas.
        return campos.stream().anyMatch(campo -> campo.equals(termo)
                || campo.startsWith(termo + " ") || campo.endsWith(" " + termo));
    }

    private List<String> termosDeBusca(String texto) {
        List<String> termos = new ArrayList<>();
        Matcher matcher = TERMOS_BUSCA.matcher(texto);
        while (matcher.find()) {
            String unidade = matcher.group("unidade");
            String numero = matcher.group("numero");
            if (unidade != null) {
                String unidadeNormalizada = unidade.toLowerCase(Locale.ROOT);
                // M e m representam prefixos diferentes no SI.
                if (unidade.startsWith("M")) {
                    unidadeNormalizada = "M" + unidadeNormalizada.substring(1);
                }
                termos.add(normalizarNumero(matcher.group("medida")) + " " + unidadeNormalizada);
            } else if (numero != null) {
                termos.add(normalizarNumero(numero));
            } else {
                termos.add(matcher.group().toLowerCase(Locale.ROOT));
            }
        }
        return termos;
    }

    private String normalizarNumero(String numero) {
        return new BigDecimal(numero.replace(',', '.')).stripTrailingZeros().toPlainString();
    }

    public MaterialResponseDTO buscarPorCodigo(String codigo) {
        Material material = materialRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com código: " + codigo));
        return new MaterialResponseDTO(material);
    }

    public MaterialResponseDTO buscarPorId(Long id) {
        return new MaterialResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialUpdateRequestDTO dto) {
        return atualizarCadastro(id, dto);
    }

    private MaterialResponseDTO atualizarCadastro(Long id, MaterialCadastroDTO dto) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
        validarQuantidadeMinima(dto.getQuantidadeMinima());
        preencherCadastro(material, dto);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    @Transactional
    public void deletar(Long id) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
        materialRepository.delete(material);
    }

    public List<MaterialResponseDTO> listarAlertaReposicao() {
        return materialRepository.findMateriaisParaReposicao().stream()
                .map(MaterialResponseDTO::new).toList();
    }

    @Transactional
    public MaterialResponseDTO ajustarSaldo(Long materialId, Integer deltaQuantidade) {
        if (deltaQuantidade == null || deltaQuantidade == 0) {
            throw new IllegalArgumentException("A quantidade do ajuste deve ser diferente de zero.");
        }
        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + materialId));

        int novoSaldo;
        try {
            novoSaldo = Math.addExact(material.getQuantidadeDisponivel(), deltaQuantidade);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("O ajuste de saldo excede o limite permitido (overflow).", ex);
        }

        if (novoSaldo < 0) {
            throw new IllegalArgumentException("O saldo do material não pode ficar negativo.");
        }
        material.setQuantidadeDisponivel(novoSaldo);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    private void preencherCadastro(Material material, MaterialCadastroDTO dto) {
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()
                || dto.getNome() == null || dto.getNome().isBlank()
                || dto.getDescricao() == null || dto.getDescricao().isBlank()
                || dto.getGrupo() == null || dto.getGrupo().isBlank()
                || dto.getCategoria() == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("Código, nome, descrição, grupo, categoria e status são obrigatórios.");
        }
        if (dto.getLocalId() == null) {
            throw new IllegalArgumentException("A localização do material é obrigatória.");
        }
        String grupo = dto.getGrupo().trim();
        String subgrupo = normalizarSubgrupo(dto.getSubgrupo());
        atualizarClassificacao(material, dto.getCategoria(), grupo, subgrupo);

        material.setCodigo(dto.getCodigo().trim());
        material.setNome(dto.getNome().trim());
        material.setDescricao(dto.getDescricao() == null ? null : dto.getDescricao().trim());
        material.setCaracteristicas(dto.getCaracteristicas());
        material.setModelo(dto.getModelo());
        material.setEspecificacoesTecnicas(dto.getEspecificacoesTecnicas());
        material.setStatus(dto.getStatus());
        material.setQuantidadeMinima(dto.getQuantidadeMinima());

        Localizacao localizacao = localizacaoRepository.findById(dto.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Localização não encontrada com ID: " + dto.getLocalId()));
        material.setLocalizacao(localizacao);

        materialRepository.findByCodigo(material.getCodigo())
                .filter(existente -> !existente.getId().equals(material.getId()))
                .ifPresent(existente -> { throw new CodigoMaterialDuplicadoException(
                        "Já existe um material cadastrado com o código: " + material.getCodigo()); });
    }

    private void atualizarClassificacao(Material material, CategoriaMaterial categoria, String grupo, String subgrupo) {
        // Preserva a classificação legada quando a edição muda apenas outros campos.
        if (categoria == material.getCategoria() && grupo.equals(material.getGrupo())
                && Objects.equals(subgrupo, normalizarSubgrupo(material.getSubgrupo()))) {
            return;
        }
        if (!categoria.aceitaGrupo(grupo)) {
            throw new HierarquiaInvalidaException("O grupo informado não pertence à categoria do material.");
        }
        if (subgrupo != null && !categoria.aceitaSubgrupo(subgrupo)) {
            throw new HierarquiaInvalidaException("O subgrupo informado não pertence ao grupo e à categoria do material.");
        }
        material.setCategoria(categoria);
        material.setGrupo(grupo);
        material.setSubgrupo(subgrupo);
    }

    private String normalizarSubgrupo(String subgrupo) {
        return subgrupo == null || subgrupo.isBlank() ? null : subgrupo.trim();
    }

    private void validarQuantidades(MaterialRequestDTO dto) {
        if (dto.getQuantidadeDisponivel() == null || dto.getQuantidadeDisponivel() < 0) {
            throw new IllegalArgumentException("A quantidade disponível é obrigatória e não pode ser negativa.");
        }
        validarQuantidadeMinima(dto.getQuantidadeMinima());
    }

    private void validarQuantidadeMinima(Integer quantidadeMinima) {
        if (quantidadeMinima == null || quantidadeMinima < 0) {
            throw new IllegalArgumentException("A quantidade mínima é obrigatória e não pode ser negativa.");
        }
    }

    private Material buscarEntidadePorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material não encontrado com ID: " + id));
    }
}
