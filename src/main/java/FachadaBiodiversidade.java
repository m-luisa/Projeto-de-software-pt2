package facade;

import api.ImportResult;
import api.SearchSession;
import api.SpeciesDataSource;
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

    // Quantas ocorrências brutas são buscadas por espécie individual antes de
    // serem validadas/filtradas.
    private static final int LIMITE_OCORRENCIAS_POR_ESPECIE = 200;

    private final SpeciesDataSource fonte;

    public FachadaBiodiversidade(SpeciesDataSource fonte) {
        if (fonte == null) {
            throw new IllegalArgumentException("A fonte de dados não pode ser nula.");
        }
        this.fonte = fonte;
    }

    public SearchSession buscarEspecies(String termo, int tamanhoPagina) {
        SearchSession sessao = fonte.searchByVernacular(termo, tamanhoPagina);
        if (!sessao.hasResults()) {
            sessao = fonte.searchByScientific(termo, tamanhoPagina);
        }
        return sessao;
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
        List<String> brutos = fonte.fetchRawOccurrencesByScientificName(
                especie.getScientificName(), LIMITE_OCORRENCIAS_POR_ESPECIE);
        return fonte.importarOcorrencias(brutos, especie);
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
