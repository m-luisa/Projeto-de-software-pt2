package factory;

import model.ConservationStatus;
import model.Reptile;
import model.Species;


public class CriadorDeReptil extends CriadorDeEspecie {

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new Reptile(id, nome, status, "-", "-", "-", "-");
    }
}
