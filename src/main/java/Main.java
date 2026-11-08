import api.GBIFApiClient;
import api.EnrichedSpeciesDataSource;
import api.WikiDataClient; 
import api.ImportResult;
import api.SpeciesDataSource;
import busca.BuscaPorNomeCientifico;
import busca.BuscaPorNomePopular;
import facade.FachadaBiodiversidade;
import model.ConservationStatus;
import model.Levantamento;
import model.Occurrence;
import model.SearchResult;
import model.Species;
import ui.EspecieSelecionada;
import ui.SelecionadorDeUmaEspecie;
import ui.SelecionadorDeVariasEspecies;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {

    // Tamanho da página mostrada na tela. É decisão de apresentação, por isso
    // continua aqui no Main. As constantes de processamento em lote
    // (tamanho de página e limite de ocorrências) agora vivem na fachada.
    private static final int PAGINA = 20;

    public static void main(String[] args) {
        configurarConsoleUtf8();

        // Força saída e leitura padrão em UTF-8, independente da code page
        // nativa do console (no Windows, o cmd.exe costuma usar uma code page
        // antiga que não representa acentos/caracteres especiais, fazendo o
        // Java imprimir "?" mesmo quando o dado em memória está correto).
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        // RF7 [IF]: Main só conhece a interface, nunca a implementação concreta.
        SpeciesDataSource fonte = new EnrichedSpeciesDataSource(new GBIFApiClient(), new WikiDataClient());

        // O Main é o único lugar que instancia as classes concretas; daqui
        // para baixo ele só conversa com a fachada.
        FachadaBiodiversidade fachada = new FachadaBiodiversidade(fonte);
        boolean running = true;

        while (running) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    consultaGeral(scanner, fachada);
                    break;
                case "2":
                    listarOcorrenciasPorNome(scanner, fachada);
                    break;
                case "3":
                    relatorioConservacaoPorNome(scanner, fachada);
                    break;
                case "4":
                    // RF10: escolher o método de busca (padrão Strategy)
                    fachada = escolherMetodoDeBusca(scanner, fonte, fachada);
                    break;
                case "0":
                    running = false;
                    System.out.println("Encerrando...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.\n");
            }
        }

        scanner.close();
    }

    // No Windows, o console (cmd.exe/PowerShell) costuma abrir usando uma code
    // page antiga (ex.: 850), que não representa acentos e caracteres especiais
    // corretamente, mesmo que o Java esteja emitindo os bytes certos em UTF-8.
    // Para o usuário não precisar configurar nada manualmente antes de usar o
    // programa, ajustamos a codificação do console automaticamente aqui, via
    // um subprocesso que herda o console atual (por isso a mudança afeta a
    // janela inteira, e não só esse subprocesso). Em sistemas que já usam
    // UTF-8 nativamente (Linux/macOS), isso simplesmente não é executado.
    private static void configurarConsoleUtf8() {
        String sistemaOperacional = System.getProperty("os.name", "").toLowerCase();
        if (!sistemaOperacional.contains("win")) {
            return;
        }

        try {
            // Testado e confirmado: ajustar a codificação de saída do console
            // via PowerShell resolve o problema de forma mais confiável do que
            // o comando "chcp 65001" isolado no ambiente do usuário. Também
            // precisamos ajustar a ENTRADA (InputEncoding) — sem isso, o texto
            // digitado pelo usuário com acentos chega corrompido no Java, mesmo
            // com a saída já correta.
            new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; "
                            + "[Console]::InputEncoding = [System.Text.Encoding]::UTF8")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            // Se o PowerShell não estiver disponível por algum motivo, tenta o
            // chcp como alternativa antes de desistir.
            try {
                new ProcessBuilder("cmd.exe", "/c", "chcp 65001>nul")
                        .inheritIO()
                        .start()
                        .waitFor();
            } catch (Exception ignored) {
                // Se nada funcionar, seguimos mesmo assim — o pior caso é
                // voltar a ter mojibake, mas o programa continua funcionando.
            }
        }
    }

    // RF10 [A]: dois métodos de busca selecionáveis independentemente. A
    // Main só troca a lista de estratégias da fachada; o resto do programa
    // não sabe qual método está em uso.
    private static FachadaBiodiversidade escolherMetodoDeBusca(Scanner scanner, SpeciesDataSource fonte,
                                                               FachadaBiodiversidade atual) {
        System.out.println("\nMétodo de busca:");
        System.out.println("1. Automático (tenta nome popular e, se não achar, nome científico)");
        System.out.println("2. Só por nome popular (ex.: onça-pintada)");
        System.out.println("3. Só por nome científico (ex.: Panthera onca)");
        System.out.print("Escolha: ");
        String escolha = scanner.nextLine().trim();

        switch (escolha) {
            case "1":
                System.out.println("Busca automática ativada.\n");
                return new FachadaBiodiversidade(fonte);
            case "2":
                System.out.println("Busca por nome popular ativada.\n");
                return new FachadaBiodiversidade(fonte, List.of(new BuscaPorNomePopular()));
            case "3":
                System.out.println("Busca por nome científico ativada.\n");
                return new FachadaBiodiversidade(fonte, List.of(new BuscaPorNomeCientifico()));
            default:
                System.out.println("Opção inválida. O método de busca não foi alterado.\n");
                return atual;
        }
    }

    private static void exibirMenu() {
        System.out.println("=========================================");
        System.out.println(" SISTEMA DE CONSULTA DE ESPÉCIES - GBIF");
        System.out.println("=========================================");
        System.out.println("1. Consulta geral (buscar espécie por nome)");
        System.out.println("2. Listar ocorrências");
        System.out.println("3. Gerar relatório de conservação");
        System.out.println("4. Escolher método de busca (automático, nome popular ou nome científico)");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    // Usado após a consulta geral (opção 1), quando o usuário já escolheu uma
    // espécie específica e pede para ver as ocorrências dela.
    private static void listarOcorrencias(Species especie, FachadaBiodiversidade fachada, Scanner scanner) {
        ImportResult resultado = fachada.ocorrenciasDaEspecie(especie);

        exibirOcorrenciasPaginadas(resultado.ocorrencias, resultado.descartados, "\"" + especie.getScientificName() + "\"", scanner);
    }

    // Opção "2. Listar ocorrências": o usuário digita um nome (comum ou
    // científico), e TODAS as ocorrências encontradas para esse termo são
    // listadas diretamente — não há passo de "escolha um resultado". Como um
    // termo pode casar com mais de uma espécie (ex.: subespécies de
    // Panthera leo), buscamos ocorrências de cada espécie encontrada e
    // juntamos tudo em uma única lista.
    private static void listarOcorrenciasPorNome(Scanner scanner, FachadaBiodiversidade fachada) {
        System.out.print("\nDigite o nome (comum ou científico) da espécie: ");
        String termo = scanner.nextLine().trim();

        if (termo.isEmpty()) {
            System.out.println("Termo de busca não pode ser vazio.\n");
            return;
        }

        System.out.println("Buscando ocorrências de \"" + termo + "\" na base do GBIF...");

        // A fachada aplica a mesma regra de fallback da consulta geral e
        // percorre todas as páginas de correspondências.
        List<SearchResult> correspondencias = fachada.buscarTodasAsCorrespondencias(termo);

        if (correspondencias.isEmpty()) {
            System.out.println("Nenhuma espécie encontrada para \"" + termo + "\".\n");
            return;
        }

        // Busca as ocorrências de cada espécie encontrada e junta tudo.
        ImportResult resultado = fachada.ocorrenciasDasCorrespondencias(correspondencias);

        exibirOcorrenciasPaginadas(resultado.ocorrencias, resultado.descartados, "\"" + termo + "\"", scanner);
    }

    // Opção "3. Relatório de conservação por status": o usuário adiciona
    // espécies uma a uma (reaproveitando resolverEspecie, a mesma busca com
    // fallback popular->científico da consulta geral) até dizer que não quer
    // mais adicionar. Só então o relatório é montado, agrupando por
    // ConservationStatus via ConservationReport (RF6 [P] — nenhuma lógica
    // específica de subtipo aqui ou lá).
    private static void relatorioConservacaoPorNome(Scanner scanner, FachadaBiodiversidade fachada) {
        Map<Species, String> nomesPopulares = new IdentityHashMap<>();

        System.out.println("\n--- Gerar relatório de conservação ---");

        // RF8: o relatório agora é um levantamento NOMEADO.
        System.out.print("Digite um nome para o levantamento (ex.: Mata Atlântica 2026): ");
        String nomeDigitado = scanner.nextLine().trim();
        Levantamento levantamento = new Levantamento(nomeDigitado.isEmpty() ? "Levantamento sem nome" : nomeDigitado);

        System.out.println("Adicione as espécies que deseja incluir no levantamento \"" + levantamento.getNome() + "\".");

        while (true) {
            // Um objeto novo a cada rodada, pois ele guarda as espécies escolhidas.
            List<EspecieSelecionada> novas = new SelecionadorDeVariasEspecies(scanner, fachada, PAGINA).executar();
            for (EspecieSelecionada selecionada : novas) {
                if (levantamento.adicionar(selecionada.especie)) {
                    nomesPopulares.put(selecionada.especie, selecionada.nomePopular);
                } else {
                    System.out.println("\"" + selecionada.especie.getScientificName()
                            + "\" já está no levantamento e não foi adicionada de novo.");
                }
            }

            if (!novas.isEmpty()) {
                System.out.println("\n" + levantamento.total() + " espécie(s) no levantamento até agora.");
            }

            System.out.print("\nDeseja adicionar mais espécies ao levantamento? (S/N): ");
            String continuar = scanner.nextLine().trim();
            if (!continuar.equalsIgnoreCase("S")) {
                break;
            }
        }

        if (levantamento.estaVazio()) {
            System.out.println("\nNenhuma espécie adicionada. Relatório cancelado.\n");
            return;
        }

        // Resumo do levantamento: nome, total, contagem por status e categoria mais comum.
        System.out.println("\n" + levantamento.resumo());

        Map<ConservationStatus, List<Species>> grupos = fachada.agruparPorStatus(levantamento.getOrganismos());
        exibirCategoriasDeRisco(grupos, nomesPopulares, scanner);
    }

    // Menu de navegação por categoria de risco: mostra só as classificações
    // que de fato têm espécie associada (com a contagem de cada uma, e o
    // nome traduzido para português via ConservationStatus.emPortugues()), e
    // deixa o usuário escolher qual quer detalhar, até apertar [0] para
    // voltar.
    private static void exibirCategoriasDeRisco(Map<ConservationStatus, List<Species>> grupos,
                                                  Map<Species, String> nomesPopulares, Scanner scanner) {
        while (true) {
            List<ConservationStatus> statusComEspecies = new ArrayList<>();
            for (Map.Entry<ConservationStatus, List<Species>> entrada : grupos.entrySet()) {
                if (!entrada.getValue().isEmpty()) {
                    statusComEspecies.add(entrada.getKey());
                }
            }

            System.out.println("\n--- Classificações de risco no relatório ---");
            for (int i = 0; i < statusComEspecies.size(); i++) {
                ConservationStatus status = statusComEspecies.get(i);
                System.out.printf("%2d. %-25s (%d espécies)%n",
                        i + 1, status.emPortugues(), grupos.get(status).size());
            }
            System.out.print("\nDigite o número da classificação para detalhar ou [0] voltar: ");
            String escolha = scanner.nextLine().trim();

            if (escolha.equals("0")) {
                System.out.println();
                return;
            }

            int indice;
            try {
                indice = Integer.parseInt(escolha);
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida.\n");
                continue;
            }

            if (indice < 1 || indice > statusComEspecies.size()) {
                System.out.println("Opção inválida.\n");
                continue;
            }

            ConservationStatus escolhido = statusComEspecies.get(indice - 1);
            List<Species> grupo = grupos.get(escolhido);

            System.out.println("\n--- " + escolhido.emPortugues() + " (" + grupo.size() + " espécies) ---");
            for (Species especie : grupo) {
                String nomePopular = nomesPopulares.get(especie);
                String sufixoNomePopular = (nomePopular == null || nomePopular.isEmpty())
                        ? ""
                        : " (" + nomePopular + ")";

                System.out.println("  - " + especie.getScientificName() + sufixoNomePopular
                        + " [" + especie.getClass().getSimpleName() + "]");
            }
            System.out.println();
        }
    }

    // Paginação/exibição compartilhada pelos dois fluxos acima.
    private static void exibirOcorrenciasPaginadas(List<Occurrence> ocorrencias, int descartados,
                                                     String rotuloBusca, Scanner scanner) {
        if (ocorrencias.isEmpty()) {
            System.out.println("\nNenhuma ocorrência válida encontrada para " + rotuloBusca + ".");
            System.out.println(descartados + " registros ignorados por dados inválidos.\n");
            return;
        }

        int offset = 0;
        while (true) {
            exibirOcorrencias(ocorrencias, offset);

            boolean temProxima = offset + PAGINA < ocorrencias.size();
            boolean temAnterior = offset > 0;

            System.out.print("\n" + (temProxima ? "[N] próxima página, " : "")
                    + (temAnterior ? "[P] página anterior, " : "") + "[0] voltar: ");
            String escolha = scanner.nextLine().trim();

            if (escolha.equalsIgnoreCase("N") && temProxima) {
                offset += PAGINA;
            } else if (escolha.equalsIgnoreCase("P") && temAnterior) {
                offset = Math.max(0, offset - PAGINA);
            } else if (escolha.equals("0")) {
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }

        System.out.println("\n" + ocorrencias.size() + " ocorrências processadas, "
                + descartados + " registros ignorados por dados inválidos.\n");
    }

    // dd/MM/yyyy (+ HH:mm quando disponível) em vez do Date.toString() padrão
    // (ex: "Mon Jan 05 18:37:43 GMT-03:00 2026"), que é ilegível para o
    // usuário final.
    private static final SimpleDateFormat FORMATO_DATA = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final SimpleDateFormat FORMATO_DATA_OCORRENCIA = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    private static void exibirOcorrencias(List<Occurrence> ocorrencias, int offset) {
        int fim = Math.min(offset + PAGINA, ocorrencias.size());
        System.out.println("\n--- Ocorrências (" + (offset + 1) + " a " + fim + " de " + ocorrencias.size() + ") ---");
        for (int i = offset; i < fim; i++) {
            Occurrence o = ocorrencias.get(i);
            String dataFormatada;
            if (o.getDate() == null) {
                dataFormatada = "Data desconhecida";
            } else if (o.isHorarioConhecido()) {
                dataFormatada = FORMATO_DATA_OCORRENCIA.format(o.getDate());
            } else {
                dataFormatada = FORMATO_DATA.format(o.getDate());
            }
            System.out.printf(Locale.forLanguageTag("pt-BR"), "%2d. %-25s | %-16s | Lat: %8.4f, Lon: %9.4f%n", i + 1,
                    o.getSpeciesRef().getScientificName(), dataFormatada, o.getLatitude(), o.getLongitude());
        }
    }

    private static void consultaGeral(Scanner scanner, FachadaBiodiversidade fachada) {
        // Template Method: o esqueleto (pedir nome, buscar, navegar entre
        // páginas) vive em NavegadorDeResultados; aqui só escolhemos a variação.
        Species especie = new SelecionadorDeUmaEspecie(scanner, fachada, PAGINA).executar();

        if (especie == null) {
            return;
        }

        System.out.println("\n--- Resultado ---");
        // System.out.println("ID (taxonKey): " + especie.getId());
        System.out.println("Nome científico: " + especie.getScientificName());
        System.out.println("Categoria: " + especie.getClass().getSimpleName());
        System.out.println("Detalhes: " + especie.describeHabitat());

        System.out.print("\nDeseja ver as ocorrências desta espécie? (S/N): ");
        String verOcorrencias = scanner.nextLine().trim();

        if (verOcorrencias.equalsIgnoreCase("S")) {
            listarOcorrencias(especie, fachada, scanner);
        } else {
            System.out.println();
        }
    }
}