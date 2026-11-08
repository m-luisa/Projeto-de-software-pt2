package ui;

import facade.FachadaBiodiversidade;
import model.SearchResult;
import model.Species;

import java.util.List;
import java.util.Scanner;

/**
 * Variação "escolher UMA espécie" do Template Method (antes: Main.resolverEspecie).
 * Usada pela consulta geral (opção 1 do menu).
 */
public class SelecionadorDeUmaEspecie extends NavegadorDeResultados<Species> {

    public SelecionadorDeUmaEspecie(Scanner entrada, FachadaBiodiversidade fachada, int tamanhoPagina) {
        super(entrada, fachada, tamanhoPagina);
    }

    @Override
    protected Species valorQuandoNaoHaResultado() {
        return null; // quem chamou entende null como "nenhuma espécie escolhida"
    }

    @Override
    protected String textoDoPedidoDeEscolha() {
        return "\nDigite o número do resultado, [N] próxima página, "
                + "[P] página anterior ou [0] cancelar: ";
    }

    @Override
    protected void aoPressionarZero() {
        System.out.println("Busca cancelada.\n");
        concluir(null);
    }

    @Override
    protected void tratarEscolha(String escolha, List<SearchResult> paginaAtual) {
        int indice;
        try {
            indice = Integer.parseInt(escolha);
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida.\n");
            return;
        }

        if (indice < 1 || indice > paginaAtual.size()) {
            System.out.println("Opção inválida.\n");
            return;
        }

        SearchResult selecionado = paginaAtual.get(indice - 1);
        concluir(fachada().obterEspecie(selecionado.getKey()));
    }
}
