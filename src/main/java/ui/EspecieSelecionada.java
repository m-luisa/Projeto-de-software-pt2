package ui;

import model.Species;

/**
 * Par simples (espécie + nome popular capturado no momento da busca). Existe só para o relatório de conservação não perder o nome popular ao converter SearchResult -> Species (Species não guarda essa informação).
 */
public class EspecieSelecionada {
    public final Species especie;
    public final String nomePopular;

    public EspecieSelecionada(Species especie, String nomePopular) {
        this.especie = especie;
        this.nomePopular = nomePopular;
    }
}
