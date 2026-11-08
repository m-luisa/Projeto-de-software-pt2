package factory;

import model.ConservationStatus;
import model.Mollusk;
import model.Species;

/**
 * Criador concreto (Factory Method) dos moluscos, classe taxonômica
 * "Gastropoda". Os "-" são o marcador de "dado ainda desconhecido".
 */
public class CriadorDeMolusco extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Mollusk(id, nome, status, "-", "-", "-", "-");
    }
}
