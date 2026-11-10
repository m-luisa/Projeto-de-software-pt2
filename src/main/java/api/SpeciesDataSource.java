package api;

import model.Species;

import java.util.List;

/**
 * Contrato para qualquer fonte de dados de espécies (GBIF, EOL, ou uma
 * combinação de várias). O restante do sistema (Main, futuros serviços)
 * depende apenas desta interface, nunca de uma implementação concreta.
 *
 * Nenhum método aqui expõe offset/limit de paginação: buscas de resultados
 * paginados devolvem uma SearchSession, que resolve isso internamente,
 * buscando cada página sob demanda.
 */
public interface SpeciesDataSource {

    /**
     * Busca os detalhes completos de uma espécie a partir do seu id (taxonKey).
     */
    Species fetchSpecies(int SpeciesId);

    /**
     * Mesma ideia de fetchSpecies, mas sem qualquer enriquecimento externo
     * (ex.: Wikidata). Usado por fluxos que só precisam de dados básicos
     * (como o nome científico) e não exibem os traits enriquecidos —
     * evita chamadas de rede desnecessárias e mais lentas. Implementações
     * sem enriquecimento (ex.: GBIFApiClient puro) podem simplesmente
     * delegar para fetchSpecies.
     */
    Species fetchSpeciesSemEnriquecimento(int speciesId);

    /**
     * Inicia uma busca por nome popular. Busca apenas a primeira página
     * (tamanho pageSize) imediatamente; páginas seguintes só são buscadas na
     * API quando SearchSession.nextPage() for chamado.
     */
    SearchSession searchByVernacular(String termo, int pageSize);

    /**
     * Mesma ideia de searchByVernacular, mas por nome científico / texto livre.
     */
    SearchSession searchByScientific(String termo, int pageSize);

    /**
     * Devolve TODAS as ocorrências válidas da espécie (até um teto de
     * segurança definido pela implementação). Paginação, construção da
     * consulta e o formato bruto da API ficam escondidos dentro da
     * implementação (RF7). Registros malformados são descartados e contados
     * no ImportResult sem interromper o lote (RF9).
     */
    ImportResult fetchOcorrencias(Species especie);
}
