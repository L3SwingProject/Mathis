package main;

import exceptions.BadFormatMessageException;

import java.util.Objects;

public class Capteur implements Comparable<Capteur> {
    private String nom;
    private String batiment;
    private int etage;
    private String lieu;
    private TypeFluide type;
    private float valeurCourante = 0.f;
    private float seuilMin;
    private float seuilMax;
    private boolean connecte = true;

    public Capteur(String nom, String message) throws BadFormatMessageException {
        String[] infos = message.split(":");
        this.nom = nom;
        switch (infos[0]){
            case "ELECTRICITE":
                this.type = TypeFluide.ELECTRICITE;
                this.seuilMin = 10;
                this.seuilMax = 500;
                break;
            case "AIRCOMPRIME":
                this.type = TypeFluide.AIRCOMPRIME;
                this.seuilMin = 0;
                this.seuilMax = 5;
                break;
            case "EAU":
                this.type = TypeFluide.EAU;
                this.seuilMin = 0;
                this.seuilMax = 10;
                break;
            case "TEMPERATURE":
                this.type = TypeFluide.TEMPERATURE;
                this.seuilMin = 17;
                this.seuilMax = 22;
                break;
            default:
                throw new BadFormatMessageException("Type inconnu");
        }
        this.batiment = infos[1];
        this.etage = Integer.parseInt(infos[2]);
        this.lieu = infos[3];
    }

    public void update(float newValue){
        this.valeurCourante = newValue;
    }

    public void deconnexion(){
        this.connecte = false;
    }

    @Override
    public int compareTo(Capteur arg){
        if (this.connecte == arg.connecte){
            return nom.compareTo(arg.nom);
        }else{
            if (this.connecte){
                return 10;
            }else{
                return -10;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capteur capteur = (Capteur) o;
        return nom.equals(capteur.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }

    @Override
    public String toString() {
        return "Capteur{" +
                "nom='" + nom + '\'' +
                ", batiment='" + batiment + '\'' +
                ", etage=" + etage +
                ", lieu='" + lieu + '\'' +
                ", type=" + type +
                ", valeurCourante=" + valeurCourante +
                ", seuilMin=" + seuilMin +
                ", seuilMax=" + seuilMax +
                ", connecte=" + connecte +
                '}';
    }

    public String getNom() {
        return nom;
    }

    public String getBatiment() {
        return batiment;
    }

    public int getEtage() {
        return etage;
    }

    public String getLieu() {
        return lieu;
    }

    public TypeFluide getType() {
        return type;
    }

    public float getValeurCourante() {
        return valeurCourante;
    }

    public float getSeuilMin() {
        return seuilMin;
    }

    public float getSeuilMax() {
        return seuilMax;
    }

    public boolean isConnecte() {
        return connecte;
    }
}
