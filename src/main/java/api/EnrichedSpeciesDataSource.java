package api;

import model.*;

public class EnrichedSpeciesDataSource extends DecoradorDeFonteDeEspecies {

    private final WikiDataClient wikidata;

    // A assinatura do construtor foi mantida: o primeiro parâmetro continua
    // sendo a fonte base (GBIF), agora guardada pela classe pai em "fonte".
    public EnrichedSpeciesDataSource(SpeciesDataSource gbif, WikiDataClient wikidata) {
        super(gbif);
        this.wikidata = wikidata;
    }

    @Override
    public Species fetchSpecies(int speciesId) {
        Species base = fonte.fetchSpecies(speciesId);
        if (base == null) {
            return null;
        }
        WikiDataTraits traits = wikidata.fetchTraits(base.getScientificName());
        return enrich(base, traits);
    }

    private Species enrich(Species base, WikiDataTraits traits) {
        int id = base.getId();
        String name = base.getScientificName();
        ConservationStatus status = base.getStatus();

        String habitat = valorOuPlaceholder(traits.habitat());
        String diet = valorOuPlaceholder(traits.diet());
        String length = valorOuPlaceholder(traits.length());
        String mass = valorOuPlaceholder(traits.mass());

        return switch (base) {
            // Correspondência real: P2050 (wingspan) é o campo específico em si.
            case Insect i -> new Insect(id, name, status, valorOuPlaceholder(traits.wingspan()), habitat, diet, length, mass);

            // Correspondência real: P9566 (diel cycle) é o campo específico em si.
            case Mammal m -> new Mammal(id, name, status, valorOuPlaceholder(traits.dielCycle()), habitat, diet, length, mass);

            case Bird b -> new Bird(id, name, status, habitat, diet, length, mass);
            case Amphibian a -> new Amphibian(id, name, status, habitat, diet, length, mass);
            case Crustacean c -> new Crustacean(id, name, status, habitat, diet, length, mass);
            case Fish f -> new Fish(id, name, status, habitat, diet, length, mass);
            case Mollusk mo -> new Mollusk(id, name, status, habitat, diet, length, mass);
            case Reptile r -> new Reptile(id, name, status, habitat, diet, length, mass);
            case Plant p -> new Plant(id, name, status, habitat, diet, length, mass);

            // taxonomicClass não é trait ecológico — preservado como veio do GBIF.
            case GenericSpecies g -> new GenericSpecies(id, name, status, g.getTaxonomicClass(), habitat, diet, length, mass);

            default -> base;
        };
    }

    private String valorOuPlaceholder(String valor) {
        return valor != null ? valor : "-";
    }

}