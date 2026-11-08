package busca;

import api.SearchSession;
import api.SpeciesDataSource;

/**
 * PADRÃO COMPORTAMENTAL: Strategy.
 *
 * Esta interface é a "Strategy" do padrão: ela representa UM jeito de
 * procurar espécies (por nome popular, por nome científico, ...). Cada jeito
 * é uma classe pequena que implementa esta interface.
 *
 * Quem usa (a FachadaBiodiversidade) só conhece esta interface. Ela não sabe
 * qual método de busca está rodando, e por isso:
 *  - dá para escolher o método de busca de forma independente (RF10);
 *  - adicionar um novo método de busca é criar uma nova classe, sem editar
 *    nenhuma das que já existem.
 */
public interface EstrategiaDeBusca {

    /**
     * Inicia a busca e devolve a sessão paginada (a paginação continua
     * escondida dentro da SearchSession, como pede o RF7).
     *
     * @param fonte         de onde os dados vêm (só a interface, nunca o GBIF direto)
     * @param termo         o que o usuário digitou
     * @param tamanhoPagina quantos resultados por página
     */
    SearchSession buscar(SpeciesDataSource fonte, String termo, int tamanhoPagina);
}
