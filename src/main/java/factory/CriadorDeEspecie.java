package factory;

import model.ConservationStatus;
import model.Species;

public abstract class CriadorDeEspecie {

    protected abstract Species criar(int id, String nome, ConservationStatus status);

    public Species criarDaAPI(int id, String nome, String statusBruto) {
        ConservationStatus status = ConservationStatus.normalizeAPI(statusBruto);
        return criar(id, nome, status);
    }
}
