package model;

public class Plant extends Species {
    private String floweringPeriod;

    public Plant(int id, String name, ConservationStatus status, String floweringPeriod, String habitat, String diet, String length, String mass) {
        super(id, name, status, habitat, diet, length, mass);
        this.floweringPeriod = floweringPeriod;
    }

    //para plantas, o "habitat" é descrito como bioma
    @Override
    protected String rotuloHabitat() {
        return "Bioma";
    }

    @Override
    public String describeHabitat() {
        StringBuilder sb = new StringBuilder();
        sb.append("Planta (").append(getScientificName()).append(") | Status: ").append(getStatus().emPortugues());
        appendCampo(sb, "Época de floração", floweringPeriod);
        sb.append(describeCommonTraits());
        return sb.toString();
    }

    @Override
    public String getCategoria() {
        return "Plantas";
    }
}
