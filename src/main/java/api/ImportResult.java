package api;

import model.Occurrence;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado da importação de ocorrências: os registros válidos já
 * convertidos, quantos foram descartados e o motivo de cada descarte
 * (RF9: registros malformados são registrados como ignorados).
 */
public class ImportResult {
    public final List<Occurrence> ocorrencias = new ArrayList<>();
    public final List<String> motivosDescarte = new ArrayList<>();
    public int descartados = 0;
}
