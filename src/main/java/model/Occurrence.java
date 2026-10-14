package model;

import java.util.Date;

public class Occurrence {
    private Date date;
    private double latitude;
    private double longitude;
    private Species speciesRef;
    private boolean horarioConhecido = true;

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

    public static class Builder {
        private Date date;
        private Double latitude;   
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

        public Occurrence build() {
            if (latitude == null || longitude == null) {
                throw new IllegalArgumentException("Ocorrência sem coordenadas.");
            }
            return new Occurrence(this);
        }
    }
}
