import java.util.*;
import java.io.*;

public class Database {

  public ArrayList<Table> list_tables;
  public String filename;

  public Database(String filename){
    this.filename = filename;
    this.list_tables = new ArrayList<Table>();


    String line = null;

    try {
      FileReader fileReader = new FileReader(filename);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      while((line = bufferedReader.readLine()) != null) {
        String [] parts = line.split("\\(");
        Table table = new Table(parts[0]); // parts[0] contient ce qu'il y a avant '('  = le nom de la table
        String [] fields = parts[1].split(","); // parts[1] contient les champs de la table séparés par ',' suivi de ')'
        for(int i = 0 ; i < fields.length ; i++){

          fields[i] = fields[i].replace(")","");
          System.out.println("AJOUTE: " + fields[i] );
          fields[i] = fields[i].replaceAll("\\s+","");
          table.add_field(fields[i]);
        }
        if(parts.length > 2){
          String [] key_fields = parts[2].split(",");
          for(int i = 0 ; i < key_fields.length ; i++){
            key_fields[i] = key_fields[i].replace(")","");
            if(table.getAllColumn().contains(key_fields[i])){
              table.add_to_primary_key(key_fields[i]);
            }
          }
        }else{
          System.out.println("La table " + table.name + " n'a pas de clé primaire");
        }
        this.list_tables.add(table);
      }
      System.out.println(this.list_tables.size() + " tables trouvées.");
      bufferedReader.close();
    }
    catch(FileNotFoundException ex) {
      System.out.println("Impossible d'ouvrir le fichier " + filename);
    }
    catch(IOException ex) {
      System.out.println("Erreur lors de la lecture du fichier " + filename);
    }


  }
  public ArrayList<String> getAllColumn(String name){
    ArrayList<String> res = new ArrayList<>();
    for(int i = 0 ; i < list_tables.size();i++){
      if(list_tables.get(i).name.equals(name)){
        res = list_tables.get(i).getAllColumn();
        break;
      }
    }
    return res;
  }

  public Table table_by_name(String table_name){
    for(Table t : this.list_tables){
      if(t.name.equals(table_name)){
        return t;
      }
    }
    return null;
  }

  public boolean in_table(String col, String table){
    Table t = this.table_by_name(table);
    if(t == null){
      return false;
    }
    for(String col_table : t.getAllColumn()){
      if(col_table.equals(col)){
        return true;
      }
    }
    return false;
  }



}
