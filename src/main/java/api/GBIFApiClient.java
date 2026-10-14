package api;

import model.*;
import factory.SpeciesFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GBIFApiClient implements SpeciesDataSource {

    private static final String IDIOMA_PADRAO = "por";

    private static final int LIMITE_NOMES_POPULARES = 300;

    private final HttpClient client;

    public GBIFApiClient() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public Species fetchSpecies(int speciesId) {
        String url = "https://api.gbif.org/v1/species/" + speciesId;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.out.println("Erro na requisição à API. Código HTTP: " + response.statusCode());
                return null;
            }

            String jsonBody = response.body();

            String name = extractJsonField(jsonBody, "canonicalName");
            if (name == null || name.isEmpty()) {
                name = extractJsonField(jsonBody, "scientificName");
            }

            String taxonomicClass = extractJsonField(jsonBody, "class");

            if (name == null || taxonomicClass == null) {
                System.out.println("Não foi possível extrair a classe taxonômica ou o nome da resposta da API.");
                return null;
            }

            String rawStatus = fetchConservationStatus(speciesId);

            return SpeciesFactory.createFromAPI(speciesId, name, taxonomicClass, rawStatus);

        } catch (Exception e) {
            System.err.println("Erro ao comunicar com a API do GBIF: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Species fetchSpeciesSemEnriquecimento(int speciesId) {
        return fetchSpecies(speciesId);
    }

    private String fetchConservationStatus(int speciesId) {
        String url = "https://api.gbif.org/v1/species/" + speciesId + "/iucnRedListCategory";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                return extractJsonField(response.body(), "category");
            }
        } catch (Exception e) {
        }

        return null;
    }

    @Override
    public List<String> fetchRawOccurrencesByScientificName(String scientificName, int limit) {
        String encoded = URLEncoder.encode(scientificName, StandardCharsets.UTF_8);
        String url = "https://api.gbif.org/v1/occurrence/search?scientificName=" + encoded
                + "&limit=" + limit;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.out.println("Erro na requisição de ocorrências. Código HTTP: " + response.statusCode());
                return new ArrayList<>();
            }

            return extractResultObjects(response.body());
        } catch (Exception e) {
            System.err.println("Erro ao comunicar com a API do GBIF: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public SearchSession searchByVernacular(String termo, int pageSize) {
        return new GBIFSearchSession(offset -> search(termo, "VERNACULAR", offset, pageSize), pageSize);
    }

    @Override
    public SearchSession searchByScientific(String termo, int pageSize) {
        return new GBIFSearchSession(offset -> search(termo, null, offset, pageSize), pageSize);
    }

    private List<SearchResult> search(String query, String qField, int offset, int limit) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.gbif.org/v1/species/search?q=" + encodedQuery
                + "&limit=" + limit + "&offset=" + offset;
        if (qField != null) {
            url += "&qField=" + qField;
        }

        List<SearchResult> parsed = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.out.println("Erro na requisição à API. Código HTTP: " + response.statusCode());
                return parsed;
            }

            for (String raw : extractResultObjects(response.body())) {
                String keyStr = extractJsonNumericField(raw, "nubKey");
                if (keyStr == null) {
                    keyStr = extractJsonNumericField(raw, "key");
                }
                if (keyStr == null) {
                    continue;
                }

                String name = extractJsonField(raw, "canonicalName");
                if (name == null || name.isEmpty()) {
                    name = extractJsonField(raw, "scientificName");
                }
                if (name == null) {
                    continue;
                }

                String taxonomicClass = extractJsonField(raw, "class");
                int taxonKey = Integer.parseInt(keyStr);
                List<String> vernacularNames = fetchVernacularNamesByLanguage(taxonKey, IDIOMA_PADRAO);

                if ("VERNACULAR".equals(qField) && !correspondeAoTermoBuscado(vernacularNames, query)) {
                    continue;
                }

                parsed.add(new SearchResult(taxonKey, name, taxonomicClass, vernacularNames));
            }

        } catch (Exception e) {
            System.err.println("Erro ao comunicar com a API do GBIF: " + e.getMessage());
        }

        return deduplicateByScientificName(parsed);
    }

    private List<SearchResult> deduplicateByScientificName(List<SearchResult> results) {
        Map<String, SearchResult> melhoresPorNome = new LinkedHashMap<>();

        for (SearchResult atual : results) {
            String nomeCientifico = atual.getScientificName();
            SearchResult existente = melhoresPorNome.get(nomeCientifico);

            if (existente == null
                    || atual.getVernacularNames().size() > existente.getVernacularNames().size()) {
                melhoresPorNome.put(nomeCientifico, atual);
            }
        }

        return new ArrayList<>(melhoresPorNome.values());
    }

    private List<String> fetchVernacularNamesByLanguage(int taxonKey, String language) {
        List<String> names = new ArrayList<>();
        String url = "https://api.gbif.org/v1/species/" + taxonKey
                + "/vernacularNames?limit=" + LIMITE_NOMES_POPULARES;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                for (String raw : extractResultObjects(response.body())) {
                    String idiomaDoItem = extractJsonField(raw, "language");
                    if (idiomaDoItem == null || !idiomaDoItem.equals(language)) {
                        continue;
                    }

                    String nome = extractJsonField(raw, "vernacularName");
                    if (isNomePopularValido(nome) && !names.contains(nome)) {
                        names.add(nome);
                    }
                }
            }
        } catch (Exception e) {
        }

        return names;
    }

    private boolean isNomePopularValido(String nome) {
        if (nome == null) {
            return false;
        }
        String semEspacos = nome.trim();
        if (semEspacos.isEmpty()) {
            return false;
        }
        for (int i = 0; i < semEspacos.length(); i++) {
            if (semEspacos.charAt(i) != '?') {
                return true;
            }
        }
        return false;
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    }

    private boolean correspondeAoTermoBuscado(List<String> nomesPopulares, String termoBuscado) {
        String termoNormalizado = normalizar(termoBuscado);
        if (termoNormalizado.isEmpty()) {
            return true;
        }
        String[] palavras = termoNormalizado.split(" ");

        for (String nomePopular : nomesPopulares) {
            String nomeNormalizado = normalizar(nomePopular);
            boolean todasAsPalavrasEncontradas = true;
            for (String palavra : palavras) {
                if (!nomeNormalizado.contains(palavra)) {
                    todasAsPalavrasEncontradas = false;
                    break;
                }
            }
            if (todasAsPalavrasEncontradas) {
                return true;
            }
        }
        return false;
    }

    private boolean possuiHorario(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        String primeira = texto.split("/")[0].trim();
        return primeira.length() > 10;
    }

    private Date parseData(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }

        String primeira = texto.split("/")[0].trim();

        try {
            if (primeira.length() > 10) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return sdf.parse(primeira.substring(0, Math.min(19, primeira.length())));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(primeira);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Data de ocorrência em formato inválido: " + texto);
        }
    }

    @Override
    public ImportResult importarOcorrencias(List<String> jsonsBrutos, Species especie) {
        ImportResult resultado = new ImportResult();

        for (String raw : jsonsBrutos) {
            try {
                String latStr = extractJsonNumericField(raw, "decimalLatitude");
                String lonStr = extractJsonNumericField(raw, "decimalLongitude");
                String dataStr = extractJsonField(raw, "eventDate");

                if (latStr == null || lonStr == null) {
                    throw new IllegalArgumentException("Ocorrência sem coordenadas.");
                }

                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                Date data = (dataStr != null) ? parseData(dataStr) : null;

                Occurrence occ = new Occurrence.Builder()
                        .especie(especie)
                        .data(data)
                        .coordenadas(lat, lon)
                        .horarioConhecido(possuiHorario(dataStr))
                        .build();
                resultado.ocorrencias.add(occ);

            } catch (IllegalArgumentException e) {
                resultado.descartados++;
            }
        }

        return resultado;
    }

    private List<String> extractResultObjects(String json) {
        List<String> objects = new ArrayList<>();

        int resultsIdx = json.indexOf("\"results\"");
        if (resultsIdx == -1) return objects;

        int arrayStart = json.indexOf('[', resultsIdx);
        if (arrayStart == -1) return objects;

        int i = arrayStart + 1;
        while (i < json.length()) {
            while (i < json.length() && json.charAt(i) != '{' && json.charAt(i) != ']') {
                i++;
            }
            if (i >= json.length() || json.charAt(i) == ']') break;

            int objStart = i;
            int depth = 0;
            for (; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        i++;
                        break;
                    }
                }
            }
            objects.add(json.substring(objStart, i));
        }

        return objects;
    }

    private String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractJsonNumericField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
