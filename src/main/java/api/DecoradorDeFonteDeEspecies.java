package api;

import model.Species;

import java.util.List;

public abstract class DecoradorDeFonteDeEspecies implements SpeciesDataSource {

    // A fonte "de dentro" (pode ser o GBIFApiClient ou outro decorador).
    // protected para as subclasses poderem chamá-la quando precisarem.
    protected final SpeciesDataSource fonte;

    protected DecoradorDeFonteDeEspecies(SpeciesDataSource fonte) {
        if (fonte == null) {
            throw new IllegalArgumentException("A fonte decorada não pode ser nula.");
        }
        this.fonte = fonte;
    }

    // Todos os métodos abaixo apenas repassam a chamada para a fonte de
    // dentro. Os decoradores concretos sobrescrevem só o que querem mudar.

    @Override
    public Species fetchSpecies(int speciesId) {
        return fonte.fetchSpecies(speciesId);
    }

    @Override
    public Species fetchSpeciesSemEnriquecimento(int speciesId) {
        return fonte.fetchSpeciesSemEnriquecimento(speciesId);
    }

    @Override
    public SearchSession searchByVernacular(String termo, int pageSize) {
        return fonte.searchByVernacular(termo, pageSize);
    }

    @Override
    public SearchSession searchByScientific(String termo, int pageSize) {
        return fonte.searchByScientific(termo, pageSize);
    }

    @Override
    public List<String> fetchRawOccurrencesByScientificName(String scientificName, int limit) {
        return fonte.fetchRawOccurrencesByScientificName(scientificName, limit);
    }

    @Override
    public ImportResult importarOcorrencias(List<String> jsonsBrutos, Species especie) {
        return fonte.importarOcorrencias(jsonsBrutos, especie);
    }
}
