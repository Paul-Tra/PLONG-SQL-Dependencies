package sample;

public class Relation {
    String source,destination, nom,key;

    public Relation(String source, String destination, String nom) {
        this.source = source;
        this.destination = destination;
        this.nom = nom;
    }
    public Relation(String source, String destination, String nom,String key) {
        this.source = source;
        this.destination = destination;
        this.nom = nom;
        this.key = key;
    }
}
