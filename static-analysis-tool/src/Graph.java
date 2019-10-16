import java.util.*;
public class Graph{
	//{(noeud,voisins)}
	protected Map<String,Set<String>> origin;
	//{(noeud,funct)}
	protected Map<String,FunctionResult> fnctsdb;
	//{(arete,valeur)}
	protected Map<String,String> edgedb;

	public Graph(){
		origin = new HashMap<String,Set<String>>();
		fnctsdb = new HashMap<String,FunctionResult>();
		edgedb = new HashMap<String,String>();
	}

	public void addNode(FunctionResult fr){
		if(!fnctsdb.containsKey(fr.name_function)) fnctsdb.put(fr.name_function,fr);

		Map<String,Set<String>> copy = copyOrigin();

		for(Map.Entry<String,Set<String>>e : copy.entrySet()){
			checklink(fr,fnctsdb.get(e.getKey()));
			checklink(fnctsdb.get(e.getKey()),fr);
    	}
		checklink(fr,fr);
	}

	public void checklink(FunctionResult fr1,FunctionResult fr2){
		String value = "",v=fr1.name_function,w=fr2.name_function;
		value = interNode(fr1,fr2);
		if(!value.equals("")){
			addlink(v,w);
			String key = v+","+w;
			edgedb.put(key,value);
		}else{
			if(!origin.containsKey(w))	origin.put(w,new HashSet<String>());
		}
	}

	public void addlink(String v,String w){
		if(!origin.containsKey(v))	origin.put(v,new HashSet<String>());
		if(!origin.containsKey(w))	origin.put(w,new HashSet<String>());
		if(!origin.get(v).contains(w)) origin.get(v).add(w);
		if(!origin.get(w).contains(v)) origin.get(w).add(v);
	}


	public String interNode(FunctionResult fr1,FunctionResult fr2){
		String res = "";
		String intWmuRmu = interTuple(fr1.Wmay,fr2.Rmay,"++wr");
		String intWmuWmu = interTuple(fr1.Wmay,fr2.Wmay,"++ww");
		String intRmuWmu = interTuple(fr1.Rmay,fr2.Wmay,"++rw");
		String intWmaWma = interTuple(fr1.Wmust,fr2.Wmust,"--ww");
		
		res = intWmuRmu + intWmuWmu+intRmuWmu + intWmaWma;

		return res;
	}

	public String interTuple(ArrayList<Result> r1,ArrayList<Result> r2,String type){
		String res = "",intRes;
		for(int i = 0; i < r1.size();i++){
			for(int j = 0; j < r2.size();j++){
				intRes = interResult(r1.get(i),r2.get(j));
				if(!intRes.equals("") && !res.contains(type+","+intRes))
					res +=type+","+intRes+";\n";
			}
		}
		return res;
	}

	public String interResult(Result r1,Result r2){
		String res = "";
		String interTab = interList(r1.list_tables,r2.list_tables);
		String interCol = interList(r1.list_cols,r2.list_cols);
		String interArg = interList(r1.list_args,r2.list_args);

		if(!interTab.equals("") && !interCol.equals("") && !interArg.equals(""))
			res = interTab+"("+interArg+")."+interCol;

		return res;
	}

	public String interList(ArrayList<String> l1,ArrayList<String> l2){
		String res = "";
		if(l1.contains("*")){ res = concatList(l2);}
		else if(l2.contains("*")){ res = concatList(l1);}
		else{
			for(int i = 0; i < l1.size();i++){
				for(int j = 0; j < l2.size();j++){
					if(l1.get(i).equals(l2.get(j)) && !l1.get(i).equals("")){ 
						if(j < l2.size()-1 && !res.equals("")) res += ",";
						res += l1.get(i);
						
					}
				}
			}
		}
		return res;
	}

	public String concatList(ArrayList<String> l){
		String res = "";
		for(int i = 0; i < l.size();i++){
			res += l.get(i);
			if(i < l.size()-1)
				res +=",";
		}	
		return res;
	}



	public String toString(){
		StringBuilder s = new StringBuilder();
		for(String v : origin.keySet()){
			s.append(v+" : \n");
			for(String w : origin.get(v)){
				String value = edgedb.get(v+","+w);
				value = value == null?"":value;
				s.append(w + "\nValue : \n"+value);
			}
			s.append('\n');
		}
		return s.toString();
	}

	public void export(String format,String path){
		ExportGraph e = new ExportGraph(path);
		if(format.equals("dot"))
			e.write(format,origin,fnctsdb,edgedb);
		else if(format.equals("graphml"))
			e.write(format,origin,fnctsdb,edgedb);
	}

	public Map<String,Set<String>> copyOrigin(){
    	Map<String,Set<String>> copy = new HashMap<String,Set<String>>();
    	
    	for(Map.Entry<String,Set<String>>e : origin.entrySet()){
    		copy.put(e.getKey(),new HashSet<String>(e.getValue()));
    	}

    	return copy;
	}

	public int nb_req(){
    	return origin.size();
  	}
}