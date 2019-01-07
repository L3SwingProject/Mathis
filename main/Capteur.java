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
    private String localisation;

    public Capteur(String nom, String description) {
        this.nom=nom;
        String separateur= ":";
        String des[] = description.split(separateur);
        //TODO: possible corrupted message -> need to throw an exception.
        this.type=TypeFluide.valueOf(des[0]);
        this.batiment=des[1];
        this.etage= Integer.valueOf(des[2]);
        this.lieu = des[3];
        this.localisation=des[1]+"-"+des[2]+"-"+des[3];
        initSeuil();
    }

    public void initSeuil() {
        if(type==TypeFluide.AIRCOMPRIME) {
            seuilMin = 0;
            seuilMax = 5;
        }
        if(type==TypeFluide.EAU) {
            seuilMin = 0;
            seuilMax = 10;
        }
        if(type==TypeFluide.ELECTRICITE) {
            seuilMin = 10;
            seuilMax = 500;
        }
        if(type==TypeFluide.TEMPERATURE) {
            seuilMin = 17;
            seuilMax = 22;
        }
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

    public float getValeurCourante() {
        return valeurCourante;
    }

    public float getSeuilMin() {
        return seuilMin;
    }

    public float getSeuilMax() {
        return seuilMax;
    }

    public TypeFluide getType() {
        return type;
    }

    public String getLocalisation() {
        return localisation;
    }
}
