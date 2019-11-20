package sample;

import java.util.ArrayList;

public class Attribut {
    private String nom;
    private ArrayList<String> liste_element ;// ind 0 = attribut de la ligne 0


    public Attribut(String nom) {
        this.nom = nom;
    }


    public String get_Elememnt(int indice) {
        return this.liste_element.get(indice);
    }
    public void add_Element(String element) {
        this.liste_element.add(element);
    }

    /* GETTER SETTER */
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ArrayList<String> getListe() {
        return liste_element;
    }

    public void setListe(ArrayList<String> liste) {
        this.liste_element = liste;
    }
}
