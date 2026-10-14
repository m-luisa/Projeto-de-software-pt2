package factory;

import model.*;

import java.util.Map;

/**
 * Ponto de entrada para criar Species a partir dos dados da API.
 *
 * ANTES: um switch gigante sobre a classe taxonômica instanciava cada
 * subclasse diretamente.
 * AGORA: usa o padrão Factory Method. Cada categoria tem seu próprio
 * CriadorDeEspecie, e esta classe só escolhe QUAL criador usar, consultando
 * um mapa. Para suportar uma nova categoria: criar o criador e adicionar
 * uma linha no mapa abaixo.
 *
 * A assinatura de createFromAPI foi mantida igual, então quem já a chama
 * (GBIFApiClient) não precisou ser alterado.
 */
public class SpeciesFactory {

    // Classe taxonômica (como vem do GBIF) -> criador responsável por ela.
    private static final Map<String, CriadorDeEspecie> CRIADORES = Map.of(
            "Aves", new CriadorDeAve(),
            "Magnoliopsida", new CriadorDePlanta(),
            "Mammalia", new CriadorDeMamifero(),
            "Amphibia", new CriadorDeAnfibio(),
            "Reptilia", new CriadorDeReptil(),
            "Insecta", new CriadorDeInseto(),
            "Actinopterygii", new CriadorDePeixe(),
            "Gastropoda", new CriadorDeMolusco(),
            "Malacostraca", new CriadorDeCrustaceo()
    );

    public static Species createFromAPI(int id, String name, String taxonomicClass, String rawStatus) {

        if (taxonomicClass == null) {
            taxonomicClass = "Desconhecida";
        }

        // Escolhe o criador da categoria; se não existir um, cai no criador
        // genérico (que guarda o nome original da classe taxonômica).
        CriadorDeEspecie criador = CRIADORES.getOrDefault(
                taxonomicClass, new CriadorDeEspecieGenerica(taxonomicClass));

        // O criador normaliza o status e chama o Factory Method.
        return criador.criarDaAPI(id, name, rawStatus);
    }
}
