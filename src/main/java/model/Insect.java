package model;

public class Insect extends Species {
    private String wingspan;

    public Insect(int id, String name, ConservationStatus status, String wingspan, String habitat, String diet, String length, String mass) {
        super(id, name, status, habitat, diet, length, mass);
        this.wingspan = wingspan;
    }

    @Override
    public String describeHabitat() {
        StringBuilder sb = new StringBuilder();
        sb.append("Inseto (").append(getScientificName()).append(") | Status: ").append(getStatus().emPortugues());
        appendCampo(sb, "Envergadura", wingspan);
        sb.append(describeCommonTraits());
        return sb.toString();
    }

    @Override
    public String getCategoria() {
        return "Insetos";
    }
}
