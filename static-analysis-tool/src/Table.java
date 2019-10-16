import java.util.*;

public class Table {

  public String name;
  private ArrayList<String> list_fields;
  private ArrayList<String> list_keys;

  public Table(String name){
    this.name = name;
    this.list_fields = new ArrayList<String>(); // liste des champs de la table
    this.list_keys = new ArrayList<String>(); // liste des champs faisant partie de la clé primaire
  }

  public boolean add_field(String field){
    if(list_fields.contains(field)){
      return false;
    }
    list_fields.add(field);
    return true;
  }

  public boolean add_to_primary_key(String field){
    if(list_keys.contains(field)){
      return false;
    }
    list_keys.add(field);
    return true;
  }

  public ArrayList<String> getAllColumn(){
    return this.list_fields;
  }

  public ArrayList<String> get_primary_key(){
    return this.list_keys;
  }

  public void print_primary_key(){
    System.out.println("Table: " + this.name);
    System.out.println("Primary key: ");
    for (int i = 0 ; i < this.list_keys.size() ; i++ ){
      System.out.println(this.list_keys.get(i));
    }
  }


}
