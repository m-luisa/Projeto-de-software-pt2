package busca;

import api.SearchSession;
import api.SpeciesDataSource;

/**
 * Estratégia concreta: procura pelo nome científico ou texto livre
 * (ex.: "Panthera onca").
 */
public class BuscaPorNomeCientifico implements EstrategiaDeBusca {

    @Override
    public SearchSession buscar(SpeciesDataSource fonte, String termo, int tamanhoPagina) {
        return fonte.searchByScientific(termo, tamanhoPagina);
    }
}
