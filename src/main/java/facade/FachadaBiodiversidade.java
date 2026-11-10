package facade;

import api.ImportResult;
import api.SearchSession;
import api.SpeciesDataSource;
import busca.BuscaPorNomeCientifico;
import busca.BuscaPorNomePopular;
import busca.EstrategiaDeBusca;
import model.ConservationReport;
import model.ConservationStatus;
import model.SearchResult;
import model.Species;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FachadaBiodiversidade {

    // Tamanho de página usado quando precisamos buscar TODAS as correspondências
    // de uma vez (fluxo de ocorrências, que agrega dados de todas as espécies
    // encontradas). Maior que as páginas de exibição em tela porque, nesse
    // caso, sabemos de antemão que vamos consumir a lista inteira.
    private static final int TAMANHO_PAGINA_LOTE = 50;

    private final SpeciesDataSource fonte;

    // PADRÃO STRATEGY: lista ORDENADA de jeitos de buscar. A fachada tenta um
    // por vez, na ordem, e fica com o primeiro que achar resultado. A ordem
    // padrão é a mesma de antes do padrão: nome popular primeiro, depois o
    // científico.
    private final List<EstrategiaDeBusca> estrategias;

    // Construtor usado pelo Main: monta a ordem padrão (popular, científico).
    public FachadaBiodiversidade(SpeciesDataSource fonte) {
        this(fonte, List.of(new BuscaPorNomePopular(), new BuscaPorNomeCientifico()));
    }

    // Construtor que permite escolher quais estratégias usar e em que ordem
    // (por exemplo, só a científica, ou uma nova estratégia criada depois).
    public FachadaBiodiversidade(SpeciesDataSource fonte, List<EstrategiaDeBusca> estrategias) {
        if (fonte == null) {
            throw new IllegalArgumentException("A fonte de dados não pode ser nula.");
        }
        if (estrategias == null || estrategias.isEmpty()) {
            throw new IllegalArgumentException("É preciso informar pelo menos uma estratégia de busca.");
        }
        this.fonte = fonte;
        this.estrategias = List.copyOf(estrategias);
    }

    /**
     * Busca espécies tentando cada estratégia, na ordem, até uma achar
     * resultado. Se nenhuma achar, devolve a sessão da última tentativa
     * (que simplesmente responde hasResults() == false), igual ao
     * comportamento de antes do padrão.
     */
    public SearchSession buscarEspecies(String termo, int tamanhoPagina) {
        SearchSession sessao = null;
        for (EstrategiaDeBusca estrategia : estrategias) {
            sessao = estrategia.buscar(fonte, termo, tamanhoPagina);
            if (sessao.hasResults()) {
                return sessao;
            }
        }
        return sessao;
    }

    /**
     * RF10: busca usando UMA estratégia escolhida de forma independente, sem
     * fallback. Quem chama decide o método; o código que exibe o resultado
     * não sabe nem se importa com qual foi.
     */
    public SearchSession buscarEspecies(String termo, int tamanhoPagina, EstrategiaDeBusca estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("A estratégia de busca não pode ser nula.");
        }
        return estrategia.buscar(fonte, termo, tamanhoPagina);
    }

    public List<SearchResult> buscarTodasAsCorrespondencias(String termo) {
        SearchSession sessao = buscarEspecies(termo, TAMANHO_PAGINA_LOTE);
        List<SearchResult> todos = new ArrayList<>();

        if (!sessao.hasResults()) {
            return todos;
        }

        todos.addAll(sessao.currentPage());
        while (sessao.nextPage()) {
            todos.addAll(sessao.currentPage());
        }
        return todos;
    }

    public Species obterEspecie(int chave) {
        return fonte.fetchSpecies(chave);
    }

    public ImportResult ocorrenciasDaEspecie(Species especie) {
        return fonte.fetchOcorrencias(especie);
    }

    public ImportResult ocorrenciasDasCorrespondencias(List<SearchResult> correspondencias) {
        ImportResult agregado = new ImportResult();

        for (SearchResult correspondencia : correspondencias) {
            Species especie = fonte.fetchSpeciesSemEnriquecimento(correspondencia.getKey());
            if (especie == null) {
                continue; // não conseguiu carregar esta espécie: segue para a próxima
            }

            ImportResult parcial = ocorrenciasDaEspecie(especie);
            agregado.ocorrencias.addAll(parcial.ocorrencias);
            agregado.descartados += parcial.descartados;
            agregado.motivosDescarte.addAll(parcial.motivosDescarte);
        }

        return agregado;
    }

    /**
     * RF6: agrupa espécies de qualquer categoria por status de conservação,
     * sem lógica específica de subtipo (delegado ao ConservationReport).
     */
    public Map<ConservationStatus, List<Species>> agruparPorStatus(List<Species> especies) {
        return ConservationReport.agruparPorStatus(especies);
    }
}
