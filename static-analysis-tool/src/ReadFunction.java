import java.util.*;
import com.tunnelvisionlabs.postgresql.PostgreSqlLexer;
import com.tunnelvisionlabs.postgresql.PostgreSqlLexerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CharStream;
import org.junit.Assert;
import org.junit.Test;


public class ReadFunction{

  final int SELECT = 73;
  final int INSERT = 219;
  final int DELETE = 168;
  final int SC = 7;
  final int FROM = 49;
  final int WHERE = 88;
  final int INTO = 56;
  final int UPDATE = 349;
  final int AS = 20;
  final int SET = 313;
  final int NAME = 415;
  final int IF = 206;
  final int ELSE = 42;
  final int END = 43;
  final int RP = 3; //   ')'
  final int LP = 2; //   '('
  final int EQ = 10; //    '='
  final int SPACE=439;
  final int VALUES=404;
  final int COMMA = 6 ; // ","
  final int ALL = 9; // "*"
  final int DESC = 39; // mot clé DESC
  final int AND = 17;
  final int DOT = 11;

  ArrayList<Result> Rmay = new ArrayList<Result>();
  ArrayList<Result> Wmay = new ArrayList<Result>();
  ArrayList<Result> Rmust = new ArrayList<Result>();
  ArrayList<Result> Wmust = new ArrayList<Result>();

  boolean in_condition = false;
  String name_function;
  ArrayList<String> list_param;
  List<Token> list_token;

  ArrayList<String> tmp_cols_args;
  Database database;

  public ReadFunction(String filename, Database database) throws Exception{
    BasicTests bt = new BasicTests();
    this.database = database;
    String input = bt.loadSample(filename,"UTF-8");
    PostgreSqlLexer lexer = PostgreSqlLexerUtils.createLexer(new ANTLRInputStream(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    tokens.fill(); // récupére tous les tokens lu par le lexer
    list_token = tokens.getTokens();

  }

  public void read_select(){
    ArrayList<String> list_col = find_tokens(FROM,NAME,SELECT);
    ArrayList<String> list_table = find_tokens(WHERE,NAME,SELECT);
    ArrayList<String> list_args = find_tokens(SC,NAME,SELECT);

    if(list_table.size()>1){ // jointure

      for(String table : list_table){
        ArrayList<String> sub_list_col = new ArrayList<String>();
        ArrayList<String> sub_list_table = new ArrayList<String>();
        ArrayList<String> sub_list_args = new ArrayList<String>();
        ArrayList<String> sub_list_col_args = new ArrayList<String>();

        sub_list_table.add(table); // ajoute l'unique table

        for(String col : list_col){ // récupére les colonnes du select liées à cette table
          if(database.in_table(col,table)){
            sub_list_col.add(col);
          }
        }
        for(int i = 0 ; i <  this.tmp_cols_args.size() ; i++){
          String arg = this.tmp_cols_args.get(i);
          String [] tab_arg = arg.split("\\.");
          if(tab_arg.length > 1){

            /*System.out.println("0:" + tab_arg[0]);
            System.out.println("1:" + tab_arg[1]);
            */
            if(table.equals(tab_arg[0])){ // ajouter vérif col in table ?
              sub_list_args.add(list_args.get(i));
              sub_list_col_args.add(tab_arg[1]);
            }
          }
          else{
            if(database.in_table(arg,table)){
              sub_list_args.add(list_args.get(i));
              sub_list_col_args.add(arg);
            }
          }

        }
        Result result  = new Result(sub_list_col,sub_list_table,sub_list_args,sub_list_col_args);
        this.Rmay.add(result);
        if(! in_condition){
          this.Rmust.add(result);
        }
      }
    }

    else{
      Result result  = new Result(list_col,list_table,list_args,this.tmp_cols_args);
      this.Rmay.add(result);
      if(! in_condition){
        this.Rmust.add(result);
      }
      return;
    }

  }

  public void read_insert(){

    ArrayList<String> list_table = find_tokens(VALUES,NAME,INSERT);
    ArrayList<String> list_col = find_tokens(SC,NAME,INSERT);
    ArrayList<String> list_args = new ArrayList<String>();
    list_args.add("*");
    Result result  = new Result(list_col,list_table,list_args,null);
    this.Wmay.add(result);
    if(! in_condition){
      this.Wmust.add(result);
    }
    return;
  }

  public void read_update(){
    ArrayList<String> list_table = find_tokens(SET,NAME,UPDATE);
    ArrayList<String> list_col = find_tokens(WHERE,NAME,UPDATE);
    ArrayList<String> list_args = find_tokens(SC,NAME,UPDATE);

    Result result  = new Result(list_col,list_table,list_args, this.tmp_cols_args);
    Wmay.add(result);
    if(! in_condition){
      Wmust.add(result);
    }
    return;
  }

  public boolean is_param(String p){
    for(int i = 0 ; i < this.list_param.size() ; i++  ){
      if(this.list_param.get(i).equals(p)){
        return true;
      }
    }
    return false;
  }

  public void add_item(ArrayList<String> liste,String elt){
    if(!liste.contains(elt) && !liste.contains("*")){ // condition "*" probléme si plusieurs table ?
    liste.add(elt);
  }
}

public ArrayList<String> find_tokens(int token_stop, int token_look , int instruction){
  boolean stop_read = false;
  boolean equal_seen = false;
  ArrayList<String> liste = new ArrayList<String>();
  this.tmp_cols_args = new ArrayList<String>();

  while(list_token.get(0).getType() != token_stop){
    int type = list_token.get(0).getType();

    if(list_token.get(1).getType() == DOT){ // table.col lors des jointure
      CommonToken fusion = new CommonToken(NAME,list_token.get(0).getText() + list_token.get(1).getText() + list_token.get(2).getText());
      list_token.remove(1);
      list_token.remove(1);
      list_token.set(0,fusion);
      continue;
      //System.out.println("POINT");
    }

    if( (token_stop == SC && !equal_seen && type == NAME && (instruction == SELECT || instruction == UPDATE) )|| type == SPACE ){ // variable à gauche du = dans WHERE
      if(type != SPACE){
        this.add_item(tmp_cols_args,list_token.get(0).getText());
      }
      list_token.remove(0);
      continue;
    }

    if( (instruction == UPDATE | instruction == SELECT ) && token_stop == SC && type == NAME && equal_seen){ // args d'un UPDATE|SELECT, ajouté condition sur equal_seen ?
      if(this.is_param(list_token.get(0).getText())){
        this.add_item(liste,list_token.get(0).getText());
      }else{
        if(this.tmp_cols_args.size()>0){
            this.tmp_cols_args.remove(tmp_cols_args.size()-1); // pas dans la liste des args, on enléve la partie gauche qu'on avait précedement add à tmp_cols_args
        }
      }
      list_token.remove(0);
      continue;
    }

  if(instruction == UPDATE && token_stop == WHERE && type == NAME){ // col d'un UPDATE
    if(!equal_seen){
      this.add_item(liste,list_token.get(0).getText());
    }
    list_token.remove(0);
    continue;
  }

// cas du "_" dans les INSERT
if(instruction == INSERT && token_look == NAME && list_token.get(0).getText().equals("_") ){
  this.add_item(liste,"*");
  list_token.remove(0);
  continue;
}


if(token_stop == FROM && instruction == SELECT && type == ALL ){ // cas "*" d'un SELECT
this.add_item(liste,"*");
}

if( type == COMMA || type == AND){
  equal_seen = false;
  //stop_read =  false;
}
if( type == AS){
  stop_read = true;
}

else if(type == EQ){
  equal_seen = true;
}

else if((type == token_look && !stop_read) || (type == DESC && instruction == SELECT)){
  this.add_item(liste,list_token.get(0).getText());
}
list_token.remove(0);
}
list_token.remove(0); // on retire token_stop
return liste;
}

public void read_delete(){
  return;
}

public void read_if_else(){
  in_condition = true;
  return;
}

public void read_end(){
  if(in_condition && list_token.get(0).getType() == IF){
    in_condition = false;
  }
  list_token.remove(0);
  return;
}

static void print_array_result(ArrayList<Result> list_result){
  if(list_result.size() == 0){
    System.out.println("Vide");
    return;
  }
  for(int i = 0; i < list_result.size() ; i++){
    System.out.println("-----------------");
    System.out.println(list_result.get(i).get_list_cols());
    System.out.println(list_result.get(i).get_list_tables());
    System.out.println(list_result.get(i).get_list_args());
    System.out.println("-----------------");
  }
}

public void read_parameters(List<Token> list_token){

  this.name_function =  list_token.get(0).getText();
  list_token.remove(0);
  this.list_param = new ArrayList<String>();

  boolean in_param = false;

  int type =  0;

  while(type != RP ){
    type = list_token.get(0).getType();
    if(type == LP) { in_param  = true; }
    if(type == NAME && in_param){
      this.list_param.add( list_token.get(0).getText());
    }
    list_token.remove(0);
  }
}

public FunctionResult read(){

  this.read_parameters(list_token);

  while(list_token.size() != 0){

    int type = list_token.get(0).getType();
    list_token.remove(0); // enléve le 1er token (SELECT,INSERT...)

    switch(type){
      case SELECT:
      read_select();
      break;

      case INSERT:
      read_insert();
      break;

      case UPDATE:
      read_update();
      break;

      case DELETE:
      read_delete();
      break;

      case IF:
      read_if_else();
      break;

      case ELSE:
      read_if_else();
      break;

      case END:
      read_end();
      break;
    }
  }
  return new FunctionResult(this.name_function,this.list_param,this.Rmay,this.Rmust,this.Wmay,this.Wmust);
}
}
