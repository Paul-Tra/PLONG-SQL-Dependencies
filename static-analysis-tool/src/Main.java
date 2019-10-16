import com.tunnelvisionlabs.postgresql.PostgreSqlLexer;
import com.tunnelvisionlabs.postgresql.PostgreSqlLexerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CharStream;
import org.junit.Assert;
import org.junit.Test;
import java.util.*;

public class Main{

  
  public static void saveGraph(Graph g){
    Scanner sc = new Scanner(System.in);
    String mess = "Chosissez un format d'enregistrement du graph\ndot ou graphml";
    String format = "";
    
    do{
      System.out.println(mess);
      format = sc.nextLine();
    }while(!format.equals("dot")&&!format.equals("graphml"));
    
    switch(format){
      case "dot":
        g.export("dot","Graph/graph.dot");break;
      case "graphml":
        g.export("graphml","Graph/graph.graphml");break;
    }
    //System.out.print(g);
  }


  public static void main(String[] args){
    if(args.length == 0){
      System.out.println("Aucune requête en entrée.");
      return;
    }
    ReadFunction reader;
    FunctionResult fr;
    Graph g = new Graph();
    Database database = null;
    if(args.length > 0){
      database = new Database(args[args.length-1]);
      System.out.println("Fichier du schema de la base: " + args[args.length-1] );
    }else{
      System.out.println("Fichier du schema de la base non trouvé");
      System.exit(0);
    }

    ArrayList<String> items = database.getAllColumn("ITEMS");
    System.out.println(items);

    /*affiche nom des tables + clé primaire de chacune
    for(int i = 0 ; i < database.list_tables.size() ; i++){
      database.list_tables.get(i).print_primary_key();
    }
    */
    for(int i = 0; i < args.length-1 ;i++){
      try{
        reader =  new ReadFunction(args[i],database);
      }catch( Exception e){
        System.out.println("Requête " + args[i] + " non trouvée.");
        continue;
      }
      fr = reader.read();
      fr.print_function_result();
      g.addNode(fr);
    }
    saveGraph(g);
    int nb_req = g.nb_req();
    if( nb_req == 0){
      System.out.println("Aucune requête trouvées.");
      return;
    }
    System.out.println(g.nb_req() + " requêtes utilisées.");
  }

}
