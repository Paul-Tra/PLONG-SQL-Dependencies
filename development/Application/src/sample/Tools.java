package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Tools {
    ArrayList<Relation> relations;
    ArrayList<Transaction> transactions;
    HashMap<Transaction, Boolean> selectedTransactionMap = new HashMap<>();
    /*TODO : set a parameter to choose which type of dependency have to be highlight*/

    public Tools(ArrayList<Relation> relations,
                 ArrayList<Transaction> transactions) {
        this.relations = relations;
        this.transactions = transactions;
    }

    /**
     * Fills the transaction map with all the transactions of the transaction list
     */
    private void fillTransactionMap() {
        this.transactions.forEach(t -> selectedTransactionMap.put(t, true));
    }

    /**
     * says if a Transaction is selected
     *
     * @param transaction the Transaction whose we want to know if it is selected
     * @return if the Transaction is seledted or not
     */
    public boolean isTransactionSelected(Transaction transaction) {
        Set keys = this.selectedTransactionMap.keySet();
        Iterator iterator = keys.iterator();
        Transaction key;
        while (iterator.hasNext()) {
            key = (Transaction) iterator.next();
            if (key == transaction) {
                System.out.println("Give me a \"Hell Yeh\" !!");
                return this.selectedTransactionMap.get(key);
            }
        }
        System.out.println("Transaction not found");
        return false;
    }

}
