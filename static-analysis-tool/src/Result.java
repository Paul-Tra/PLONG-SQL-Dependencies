import java.util.*;

public class Result {

  public ArrayList<String> list_cols;
  public ArrayList<String> list_tables;
  public ArrayList<String> list_args;
  public ArrayList<String> list_cols_args;

  public Result(ArrayList<String> n_list_cols, ArrayList<String> n_list_tables, ArrayList<String> n_list_args, ArrayList<String> n_list_col_args){
    list_cols = n_list_cols;
    list_tables = n_list_tables;
    list_args = n_list_args;
    list_cols_args = n_list_col_args;
  }

  public ArrayList<String> get_list_cols(){
    return list_cols;
  }
  public ArrayList<String> get_list_tables(){
    return list_tables;
  }

  public ArrayList<String> get_list_args(){
    return list_args;
  }

  public ArrayList<String> get_list_cols_args(){
    return list_cols_args;
  }

  public void print_result(){
    System.out.println("Tables: " + list_tables );
    System.out.println("Colonnes: " + list_cols );
    System.out.println("Arguments: " + list_args );
    System.out.println("Colonnes arguments: " + list_cols_args);
  }

}
