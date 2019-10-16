import java.util.*;
import java.io.*;

public class FunctionResult{
  String name_function;
  ArrayList<String> parameter_list;

  public ArrayList<Result> Rmay;
  public ArrayList<Result> Rmust;
  public ArrayList<Result> Wmay;
  public ArrayList<Result> Wmust;

  public HashMap<String,ArrayList<String>> edge;


  public FunctionResult(String name_function, ArrayList<String> parameter_list, ArrayList<Result> Rmay, ArrayList<Result> Rmust, ArrayList<Result> Wmay, ArrayList<Result> Wmust){
    this.name_function = name_function;
    this.parameter_list = parameter_list;
    this.Rmay = Rmay;
    this.Rmust = Rmust;
    this.Wmay = Wmay;
    this.Wmust = Wmust;
    this.edge = new HashMap<String,ArrayList<String>>();
  }
  public String getName(){
    return name_function;
  }
  
  public void print_function_result(){ // à compléter
    System.out.println();
    System.out.println("Fonction: " + name_function);
    System.out.println("Liste paramétre: " + parameter_list );
    System.out.println("-------------------");
    System.out.println("Rmay:\n");
    this.print_array_result(Rmay);
    System.out.println("-------------------");
    System.out.println("Rmust:\n");
    this.print_array_result(Rmust);
    System.out.println("-------------------");
    System.out.println("Wmay:\n");
    this.print_array_result(Wmay);
    System.out.println("-------------------");
    System.out.println("Wmust:\n");
    this.print_array_result(Wmust);
  }

  public void print_array_result(ArrayList<Result> array){
    for(int i = 0 ; i < array.size() ; i++ ){
      array.get(i).print_result();
      if(i != array.size() -1){
        System.out.println("+");
      }

    }
  }
}
