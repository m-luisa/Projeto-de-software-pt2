package factory;

import model.*;

import java.util.Map;

public class SpeciesFactory {

  
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

        CriadorDeEspecie criador = CRIADORES.getOrDefault(
                taxonomicClass, new CriadorDeEspecieGenerica(taxonomicClass));

        return criador.criarDaAPI(id, name, rawStatus);
    }
}
