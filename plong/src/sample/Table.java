package sample;

import java.util.ArrayList;

public class Table {
    private String nom ;
    private ArrayList<Attribut> liste_attribut;

    public Table(String nom) {
        this.nom = nom;
    }

    public Table(String nom, ArrayList<String> liste) {
        this.nom = nom;
        fill_Liste_Attribut(liste);
    }
    //remplie la liste d'attribut a partir dd'une liste de String
    private void fill_Liste_Attribut(ArrayList<String> liste) {
        for (String s : liste) {
            this.liste_attribut.add(new Attribut(s));
        }
    }


    public Attribut get_Attribut(int indice) {
        return this.liste_attribut.get(indice);
    }
    public void add_Attribut(Attribut attribut) {
        this.liste_attribut.add(attribut);
    }
    public void add_Attribut(String nom) {
        this.liste_attribut.add(new Attribut(nom));
    }


    /* GETTER SETTER */
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ArrayList<Attribut> getListe_attribut() {
        return liste_attribut;
    }

    public void setListe_attribut(ArrayList<Attribut> liste_attribut) {
        this.liste_attribut = liste_attribut;
    }
}
