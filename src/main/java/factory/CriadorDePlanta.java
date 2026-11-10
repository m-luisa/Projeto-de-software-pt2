package factory;

import model.ConservationStatus;
import model.Plant;
import model.Species;

/**
 * Criador concreto (Factory Method) das plantas, classe taxonômica
 * "Magnoliopsida". Os "-" são o marcador de "dado ainda desconhecido".
 */
public class CriadorDePlanta extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Plant(id, nome, status, "-", "-", "-", "-", "-");
    }
}
