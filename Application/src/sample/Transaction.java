package sample;

import com.sun.management.UnixOperatingSystemMXBean;

public class Transaction {
    String id, nom;

    public Transaction(String id, String nom) {
        this.id = id;
        this.nom = nom;
    }
}
