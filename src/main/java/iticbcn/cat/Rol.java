package iticbcn.cat;

public class Rol {
    private int rolId;
    private String nom;

    public Rol(int rolId, String nom){
        this.rolId = rolId;
        this.nom = nom;
    }

    // Getters y Setters
    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    // toString para depuración y visualización
    @Override
    public String toString() {
        return "Rol{" +
                "rolId=" + rolId +
                ", nom='" + nom + '\'' + '}';
    }

}
