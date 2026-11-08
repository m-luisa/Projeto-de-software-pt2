package factory;

import model.ConservationStatus;
import model.Reptile;
import model.Species;

/**
 * Criador concreto (Factory Method) dos répteis, classe taxonômica
 * "Reptilia". Os "-" são o marcador de "dado ainda desconhecido".
 */
public class CriadorDeReptil extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Reptile(id, nome, status, "-", "-", "-", "-");
    }
}
