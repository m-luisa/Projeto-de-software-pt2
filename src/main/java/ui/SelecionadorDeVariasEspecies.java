package ui;

import facade.FachadaBiodiversidade;
import model.SearchResult;
import model.Species;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Variação "escolher VÁRIAS espécies" do Template Method (antes:
 * Main.resolverEspecies). O usuário digita números separados por vírgula
 * (ex.: "1,3,5"). Usada pelo relatório de conservação (opção 3 do menu).
 *
 * Cada chamada de executar() deve usar um objeto novo, porque a lista de
 * espécies escolhidas fica guardada dentro dele.
 */
public class SelecionadorDeVariasEspecies extends NavegadorDeResultados<List<EspecieSelecionada>> {

    private final List<EspecieSelecionada> selecionadas = new ArrayList<>();

    public SelecionadorDeVariasEspecies(Scanner entrada, FachadaBiodiversidade fachada, int tamanhoPagina) {
        super(entrada, fachada, tamanhoPagina);
    }

    @Override
    protected List<EspecieSelecionada> valorQuandoNaoHaResultado() {
        return selecionadas; // lista vazia: nada foi escolhido
    }

    @Override
    protected String textoDoPedidoDeEscolha() {
        return "\nDigite o(s) número(s) do(s) resultado(s) separados por vírgula (ex.: 1,3,5), "
                + "[N] próxima página, [P] página anterior ou [0] concluir seleção: ";
    }

    @Override
    protected void aoPressionarZero() {
        concluir(selecionadas);
    }

    @Override
    protected void tratarEscolha(String escolha, List<SearchResult> paginaAtual) {
        boolean algumaValida = false;

        for (String parte : escolha.split(",")) {
            String limpo = parte.trim();
            if (limpo.isEmpty()) {
                continue;
            }

            int indice;
            try {
                indice = Integer.parseInt(limpo);
            } catch (NumberFormatException e) {
                System.out.println("Ignorado: \"" + limpo + "\" não é um número válido.");
                continue;
            }

            if (indice < 1 || indice > paginaAtual.size()) {
                System.out.println("Ignorado: número " + indice + " fora do intervalo desta página.");
                continue;
            }

            SearchResult selecionado = paginaAtual.get(indice - 1);
            Species especie = fachada().obterEspecie(selecionado.getKey());
            if (especie == null) {
                System.out.println("Não foi possível carregar os detalhes de \""
                        + selecionado.getScientificName() + "\".");
                continue;
            }

            String nomePopular = selecionado.getVernacularNames().isEmpty()
                    ? null
                    : selecionado.getVernacularNames().get(0);

            selecionadas.add(new EspecieSelecionada(especie, nomePopular));
            algumaValida = true;
            System.out.println("\"" + especie.getScientificName() + "\" adicionada.");
        }

        if (algumaValida) {
            concluir(selecionadas);
        } else {
            System.out.println("Nenhuma seleção válida. Tente novamente.\n");
        }
    }
}
