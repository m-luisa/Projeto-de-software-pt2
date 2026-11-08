package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RF8 [C] — Levantamento nomeado com agregados.
 *
 * Um levantamento é um grupo de organismos (Species) com um nome, por
 * exemplo "Mata Atlântica 2026". Ele sabe responder perguntas sobre o
 * grupo inteiro: quantos organismos tem, quantos há em cada status de
 * conservação e qual é a categoria taxonômica mais comum.
 *
 * Ele só usa o que toda Species já oferece (getStatus() e getCategoria()),
 * então funciona igual para aves, mamíferos, plantas ou uma categoria que
 * venha a ser criada depois, sem nenhum "instanceof" ou "switch" por tipo.
 */
public class Levantamento {

    private final String nome;
    private final List<Species> organismos = new ArrayList<>();

    public Levantamento(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo levantamento precisa ter um nome.");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    /**
     * Adiciona um organismo. Se a mesma espécie (mesmo id) já estiver no
     * levantamento, não adiciona de novo e devolve false, para que a
     * mesma espécie não conte duas vezes nas estatísticas.
     */
    public boolean adicionar(Species especie) {
        if (especie == null) {
            throw new IllegalArgumentException("Não é possível adicionar um organismo nulo.");
        }
        for (Species existente : organismos) {
            if (existente.getId() == especie.getId()) {
                return false;
            }
        }
        organismos.add(especie);
        return true;
    }

    /** Lista só para leitura: quem usa o levantamento não consegue alterar a coleção por fora. */
    public List<Species> getOrganismos() {
        return Collections.unmodifiableList(organismos);
    }

    public int total() {
        return organismos.size();
    }

    public boolean estaVazio() {
        return organismos.isEmpty();
    }

    /**
     * Quantos organismos há em cada status de conservação. Só aparecem os
     * status que têm pelo menos um organismo, na ordem do enum (do mais
     * crítico ao menos crítico). Reaproveita o ConservationReport (RF6).
     */
    public Map<ConservationStatus, Integer> contagemPorStatus() {
        Map<ConservationStatus, Integer> contagem = new LinkedHashMap<>();
        for (Map.Entry<ConservationStatus, List<Species>> grupo
                : ConservationReport.agruparPorStatus(organismos).entrySet()) {
            if (!grupo.getValue().isEmpty()) {
                contagem.put(grupo.getKey(), grupo.getValue().size());
            }
        }
        return contagem;
    }

    /**
     * Quantos organismos há em cada categoria taxonômica (ex.: Aves = 18),
     * na ordem em que cada categoria apareceu primeiro no levantamento.
     */
    public Map<String, Integer> contagemPorCategoria() {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (Species especie : organismos) {
            contagem.merge(especie.getCategoria(), 1, Integer::sum);
        }
        return contagem;
    }

    /**
     * A categoria com mais organismos. Se duas empatarem, vale a que
     * apareceu primeiro no levantamento. Vazio se não há organismos.
     */
    public Optional<Map.Entry<String, Integer>> categoriaMaisComum() {
        Map.Entry<String, Integer> melhor = null;
        for (Map.Entry<String, Integer> entrada : contagemPorCategoria().entrySet()) {
            if (melhor == null || entrada.getValue() > melhor.getValue()) {
                melhor = entrada;
            }
        }
        return Optional.ofNullable(melhor);
    }

    /**
     * Resumo em uma linha, no espírito do exemplo da especificação:
     * Levantamento "Mata Atlântica 2026": 45 organismos | Em Perigo: 3,
     * Vulnerável: 12 | Categoria mais comum: Aves (18)
     */
    public String resumo() {
        if (estaVazio()) {
            return "Levantamento \"" + nome + "\": nenhum organismo.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Levantamento \"").append(nome).append("\": ")
                .append(total()).append(total() == 1 ? " organismo" : " organismos");

        sb.append(" | ");
        boolean primeiro = true;
        for (Map.Entry<ConservationStatus, Integer> e : contagemPorStatus().entrySet()) {
            if (!primeiro) {
                sb.append(", ");
            }
            sb.append(e.getKey().emPortugues()).append(": ").append(e.getValue());
            primeiro = false;
        }

        categoriaMaisComum().ifPresent(c ->
                sb.append(" | Categoria mais comum: ").append(c.getKey()).append(" (").append(c.getValue()).append(")"));

        return sb.toString();
    }
}
