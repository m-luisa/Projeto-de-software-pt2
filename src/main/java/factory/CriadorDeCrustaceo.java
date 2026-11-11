package factory;

import model.ConservationStatus;
import model.Crustacean;
import model.Species;

/**
 * Criador concreto (Factory Method) dos crustáceos, classe taxonômica "Malacostraca". Os "-" são o marcador de "dado ainda desconhecido"
 */
public class CriadorDeCrustaceo extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Crustacean(id, nome, status, "-", "-", "-", "-");
    }
}
