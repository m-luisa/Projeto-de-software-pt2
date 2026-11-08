package factory;

import model.ConservationStatus;
import model.Insect;
import model.Species;

/**
 * Criador concreto (Factory Method) dos insetos, classe taxonômica
 * "Insecta". O inseto tem um campo a mais (a envergadura das asas), por isso
 * são cinco "-" em vez de quatro.
 */
public class CriadorDeInseto extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Insect(id, nome, status, "-", "-", "-", "-", "-");
    }
}
