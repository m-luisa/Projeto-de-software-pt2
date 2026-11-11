package factory;

import model.ConservationStatus;
import model.Fish;
import model.Species;

/**
 * criador concreto (Factory Method) dos peixes, classe taxonômica "Actinopterygii". Os "-" são o marcador de "dado ainda desconhecido"
 */
public class CriadorDePeixe extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Fish(id, nome, status, "-", "-", "-", "-");
    }
}
