package factory;

import model.ConservationStatus;
import model.Species;

/**
 * PADRÃO CRIACIONAL: Factory Method.
 *
 * Esta é a classe "Creator" (criador) do padrão. Ela define o método de
 * fábrica (criar), que é abstrato: cada subclasse decide QUAL tipo concreto
 * de Species instanciar (Bird, Mammal, Plant, etc.).
 *
 * Quem usa um CriadorDeEspecie só conhece Species (o "Product"), nunca as
 * subclasses concretas. Para suportar uma nova categoria taxonômica basta
 * criar um novo criador e registrá-lo no SpeciesFactory, sem mexer em
 * nenhum switch (princípio Aberto/Fechado).
 */
public abstract class CriadorDeEspecie {

    /**
     * FACTORY METHOD: as subclasses implementam este método e devolvem a
     * Species concreta da sua categoria. O status já chega normalizado.
     */
    protected abstract Species criar(int id, String nome, ConservationStatus status);

    /**
     * Passo comum a todos os criadores: converte o status bruto da API
     * (ex.: "LC", "EN") para o enum e só então chama o Factory Method.
     * Assim a normalização fica em um único lugar, em vez de repetida em
     * cada subclasse.
     */
    public Species criarDaAPI(int id, String nome, String statusBruto) {
        ConservationStatus status = ConservationStatus.normalizeAPI(statusBruto);
        return criar(id, nome, status);
    }
}
