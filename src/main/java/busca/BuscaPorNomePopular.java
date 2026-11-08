package busca;

import api.SearchSession;
import api.SpeciesDataSource;

/**
 * Estratégia concreta: procura pelo nome popular (ex.: "onça-pintada").
 */
public class BuscaPorNomePopular implements EstrategiaDeBusca {

    @Override
    public SearchSession buscar(SpeciesDataSource fonte, String termo, int tamanhoPagina) {
        return fonte.searchByVernacular(termo, tamanhoPagina);
    }
}
