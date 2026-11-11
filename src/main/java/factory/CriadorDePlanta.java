package factory;

import model.ConservationStatus;
import model.Plant;
import model.Species;


public class CriadorDePlanta extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Plant(id, nome, status, "-", "-", "-", "-", "-");
    }
}
