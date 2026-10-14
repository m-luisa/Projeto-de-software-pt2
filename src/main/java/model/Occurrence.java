package model;

import java.util.Date;

public class Occurrence {
    private Date date;
    private double latitude;
    private double longitude;
    private Species speciesRef;
    // Nem todo eventDate do GBIF traz hora (muitos registros só têm precisão
    // de dia). true por padrão; o Builder ajusta para false quando o
    // eventDate bruto não trouxer componente de horário.
    private boolean horarioConhecido = true;

    // Construtor PRIVADO: a única forma de criar uma Occurrence é pelo
    // Occurrence.Builder (padrão Builder). Assim, todas as validações
    // acontecem num único ponto e nunca existe uma Occurrence "pela metade".
    private Occurrence(Builder builder) {
        setDate(builder.date);
        setCoordinates(builder.latitude, builder.longitude);

        if (builder.speciesRef == null) {
            throw new IllegalArgumentException("Toda ocorrência deve estar associada a uma espécie válida.");
        }
        this.speciesRef = builder.speciesRef;
        this.horarioConhecido = builder.horarioConhecido;
    }

    public void setDate(Date d) {
        if (d != null && d.after(new Date())) {
            throw new IllegalArgumentException("A data da ocorrência não pode ser no futuro.");
        }
        this.date = d;
    }

    public void setCoordinates(double lat, double lon) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude inválida (" + lat + "). Deve estar entre -90 e 90.");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("Longitude inválida (" + lon + "). Deve estar entre -180 e 180.");
        }

        this.latitude = lat;
        this.longitude = lon;
    }

    public void setHorarioConhecido(boolean horarioConhecido) {
        this.horarioConhecido = horarioConhecido;
    }

    public Date getDate() { return date; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Species getSpeciesRef() { return speciesRef; }
    public boolean isHorarioConhecido() { return horarioConhecido; }

    /**
     * PADRÃO CRIACIONAL: Builder.
     *
     * Monta uma Occurrence passo a passo, com métodos encadeados que deixam
     * claro o que cada valor significa (em vez de um construtor com vários
     * parâmetros seguidos, fáceis de trocar de ordem). Os campos opcionais
     * têm valor padrão: data = null (desconhecida) e horarioConhecido = true.
     *
     * Exemplo:
     *   Occurrence o = new Occurrence.Builder()
     *           .especie(especie)
     *           .data(data)
     *           .coordenadas(-9.66, -35.73)
     *           .horarioConhecido(false)
     *           .build();
     */
    public static class Builder {
        private Date date;
        private Double latitude;   // Double (e não double) para detectar "não informado" (null)
        private Double longitude;
        private Species speciesRef;
        private boolean horarioConhecido = true;

        public Builder data(Date date) {
            this.date = date;
            return this; // devolve o próprio builder para permitir o encadeamento
        }

        public Builder coordenadas(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        public Builder especie(Species speciesRef) {
            this.speciesRef = speciesRef;
            return this;
        }

        public Builder horarioConhecido(boolean horarioConhecido) {
            this.horarioConhecido = horarioConhecido;
            return this;
        }

        // Valida e constrói o objeto final. Lança IllegalArgumentException
        // (a mesma exceção usada antes) se algo obrigatório faltar ou for
        // inválido — o GBIFApiClient já trata essa exceção descartando o registro.
        public Occurrence build() {
            if (latitude == null || longitude == null) {
                throw new IllegalArgumentException("Ocorrência sem coordenadas.");
            }
            return new Occurrence(this);
        }
    }
}
