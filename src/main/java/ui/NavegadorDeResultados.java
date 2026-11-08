package ui;

import api.SearchSession;
import facade.FachadaBiodiversidade;
import model.SearchResult;

import java.util.List;
import java.util.Scanner;

/**
 * PADRÃO COMPORTAMENTAL: Template Method.
 *
 * Antes, o Main tinha duas funções quase idênticas (resolverEspecie e
 * resolverEspecies). As duas faziam o mesmo "esqueleto":
 *
 *   1. pedir o nome ao usuário e validar que não está vazio;
 *   2. buscar na fachada e avisar se não achou nada;
 *   3. repetir: mostrar a página, ler a escolha, tratar [N] e [P];
 *   4. tratar o que o usuário escolheu.
 *
 * Só os passos "tratar [0]" e "tratar a escolha" eram diferentes. Esta
 * classe guarda o esqueleto UMA vez, dentro do método executar(), que é
 * "final" (as subclasses não podem mudar a ordem dos passos). Os passos que
 * variam viram métodos abstratos (os "ganchos"), que cada subclasse preenche.
 *
 * @param <T> o tipo do resultado final (uma espécie, ou uma lista delas)
 */
public abstract class NavegadorDeResultados<T> {

    private final Scanner entrada;
    private final FachadaBiodiversidade fachada;
    private final int tamanhoPagina;

    // Usamos um "concluido" separado do "resultado" porque o resultado pode
    // ser null (ex.: usuário cancelou) e mesmo assim a navegação terminou.
    private boolean concluido;
    private T resultado;

    protected NavegadorDeResultados(Scanner entrada, FachadaBiodiversidade fachada, int tamanhoPagina) {
        this.entrada = entrada;
        this.fachada = fachada;
        this.tamanhoPagina = tamanhoPagina;
    }

    // ------------------------------------------------------------------
    // TEMPLATE METHOD: o esqueleto fixo. É final de propósito.
    // ------------------------------------------------------------------
    public final T executar() {
        System.out.print("\nDigite o nome (comum ou científico) da espécie: ");
        String termo = entrada.nextLine().trim();

        if (termo.isEmpty()) {
            System.out.println("Termo de busca não pode ser vazio.\n");
            return valorQuandoNaoHaResultado();
        }

        System.out.println("Buscando \"" + termo + "\" na base do GBIF...");

        // RF7 [A]: quem controla a paginação é a SearchSession, não esta classe.
        // [N] só busca página nova na API se o usuário realmente avançar;
        // [P] nunca busca nada, só volta no cache da sessão.
        SearchSession sessao = fachada.buscarEspecies(termo, tamanhoPagina);

        if (!sessao.hasResults()) {
            System.out.println("Nenhum resultado encontrado para \"" + termo + "\".\n");
            return valorQuandoNaoHaResultado();
        }

        while (!concluido) {
            List<SearchResult> pagina = sessao.currentPage();
            exibirPagina(pagina, sessao.pageIndex() * tamanhoPagina);
            System.out.print(textoDoPedidoDeEscolha());
            String escolha = entrada.nextLine().trim();

            // Passos comuns às duas variações: navegar entre páginas.
            if (escolha.equalsIgnoreCase("N")) {
                if (!sessao.nextPage()) {
                    System.out.println("Não há mais resultados.\n");
                }
            } else if (escolha.equalsIgnoreCase("P")) {
                if (!sessao.previousPage()) {
                    System.out.println("Você já está na primeira página.\n");
                }
            } else if (escolha.equals("0")) {
                aoPressionarZero();            // gancho 1
            } else {
                tratarEscolha(escolha, pagina); // gancho 2
            }
        }

        return resultado;
    }

    // ------------------------------------------------------------------
    // GANCHOS: cada subclasse decide o seu comportamento.
    // ------------------------------------------------------------------

    /** Valor devolvido quando o termo é vazio ou a busca não acha nada. */
    protected abstract T valorQuandoNaoHaResultado();

    /** Texto mostrado ao pedir a escolha do usuário. */
    protected abstract String textoDoPedidoDeEscolha();

    /** O que fazer quando o usuário digita [0]. Deve chamar concluir(...). */
    protected abstract void aoPressionarZero();

    /**
     * O que fazer com qualquer outra escolha (um número, vários números...).
     * Chama concluir(...) quando terminou; se a escolha foi inválida, só
     * avisa o usuário e retorna (a navegação continua).
     */
    protected abstract void tratarEscolha(String escolha, List<SearchResult> paginaAtual);

    // ------------------------------------------------------------------
    // Ferramentas que as subclasses usam.
    // ------------------------------------------------------------------

    /** Encerra a navegação e define o resultado de executar(). */
    protected final void concluir(T valor) {
        this.resultado = valor;
        this.concluido = true;
    }

    protected final FachadaBiodiversidade fachada() {
        return fachada;
    }

    // Mostra uma página de resultados (antes era Main.exibirResultados).
    private void exibirPagina(List<SearchResult> pagina, int deslocamento) {
        System.out.println("\n--- Resultados (" + (deslocamento + 1) + " a " + (deslocamento + pagina.size()) + ") ---");
        for (int i = 0; i < pagina.size(); i++) {
            SearchResult r = pagina.get(i);

            String nomesPopulares;
            if (r.getVernacularNames().isEmpty()) {
                nomesPopulares = "-";
            } else {
                int qtd = Math.min(3, r.getVernacularNames().size());
                nomesPopulares = String.join(", ", r.getVernacularNames().subList(0, qtd));
            }

            String classe = r.getTaxonomicClass() == null ? "-" : r.getTaxonomicClass();

            System.out.printf("%2d. %-35s | Popular: %-30s | Classe: %s%n",
                    i + 1, r.getScientificName(), nomesPopulares, classe);
        }
    }
}
