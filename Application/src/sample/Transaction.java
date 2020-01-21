package sample;

import com.sun.management.UnixOperatingSystemMXBean;

public class Transaction {
    private final int NB_COTE = 4;
    String id, nom;
    int nbRelationCote[];
    public Transaction(String id, String nom) {
        this.id = id;
        this.nom = nom;
        nbRelationCote = new int[NB_COTE];
    }

    protected void incremente(int cote) {
        if (cote < 0 || cote > 3) {
            return;
        }
        nbRelationCote[cote]++;
    }
    public int getNB_COTE() {
        return NB_COTE;
    }

}
