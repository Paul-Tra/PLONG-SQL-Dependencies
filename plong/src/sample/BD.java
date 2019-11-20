package sample;

import java.util.ArrayList;

public class BD {
    private static int nb_Tables;
    private String nom;
    private Boolean tab_relationnelle[][];
    private ArrayList<Table> liste_table;

    public BD(String nom) {
        this.nom = nom;
    }

    public BD(String nom, ArrayList<String> liste) {
        this.nom = nom;
        fill_Liste_Table(liste);
    }

    private void fill_Liste_Table(ArrayList<String> liste) {
        for (String s : liste) {
            this.liste_table.add(new Table(s));
        }
    }

    /* GETTER SETTER */
    public static int getNb_Tables() {
        return nb_Tables;
    }

    public static void setNb_Tables(int nb_Tables) {
        BD.nb_Tables = nb_Tables;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Boolean[][] getTab_relationnelle() {
        return tab_relationnelle;
    }

    public void setTab_relationnelle(Boolean[][] tab_relationnelle) {
        this.tab_relationnelle = tab_relationnelle;
    }

    public ArrayList<Table> getListe_table() {
        return liste_table;
    }

    public void setListe_table(ArrayList<Table> liste_table) {
        this.liste_table = liste_table;
    }
}
