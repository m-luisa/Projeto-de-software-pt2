package factory;

import model.ConservationStatus;
import model.Mammal;
import model.Species;

/**
 * Criador concreto (Factory Method) dos mamíferos, classe taxonômica
 * "Mammalia". O mamífero tem um campo a mais que as outras categorias (o
 * ciclo de atividade), por isso são cinco "-" em vez de quatro.
 */
public class CriadorDeMamifero extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Mammal(id, nome, status, "-", "-", "-", "-", "-");
    }
}
