package factory;

import model.Amphibian;
import model.ConservationStatus;
import model.Species;

/**
 * Criador concreto (Factory Method) dos anfíbios, classe taxonomica "Amphibia". Os "-" são o marcador de "dado ainda desconhecido".
 */
public class CriadorDeAnfibio extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Amphibian(id, nome, status, "-", "-", "-", "-");
    }
}
