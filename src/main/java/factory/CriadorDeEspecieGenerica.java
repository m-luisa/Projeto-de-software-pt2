package factory;

import model.ConservationStatus;
import model.GenericSpecies;
import model.Species;

/**
 * Criador "curinga": usado pela SpeciesFactory quando a classe taxonômica
 * não está mapeada em nenhum dos outros criadores. Diferente dos demais,
 * este criador precisa guardar a classe taxonômica original que veio do
 * GBIF, porque a GenericSpecies a exibe ao usuário.
 */
public class CriadorDeEspecieGenerica extends CriadorDeEspecie {

    private final String classeTaxonomica;

    public CriadorDeEspecieGenerica(String classeTaxonomica) {
        this.classeTaxonomica = classeTaxonomica;
    }

    @Override
    protected Species criar(int id, String nome, ConservationStatus status) {
        return new GenericSpecies(id, nome, status, classeTaxonomica, "-", "-", "-", "-");
    }
}
