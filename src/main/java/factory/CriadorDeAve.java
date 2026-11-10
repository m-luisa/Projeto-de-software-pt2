package factory;

import model.Bird;
import model.ConservationStatus;
import model.Species;

/**
 * Criador concreto (Factory Method) das aves, classe taxonômica "Aves".
 *
 * Os quatro "-" são o marcador interno de "dado ainda desconhecido"
 * (habitat, dieta, comprimento e massa). Quem troca esses "-" pelos valores
 * reais do Wikidata, quando existirem, é o EnrichedSpeciesDataSource.
 */
public class CriadorDeAve extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Bird(id, nome, status, "-", "-", "-", "-", "-");
    }
}
