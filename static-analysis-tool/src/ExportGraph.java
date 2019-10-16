import java.util.*;
import java.io.*;
public class ExportGraph{
	private String path;


	public ExportGraph(String path){
		this.path = path;
	}

	public void write(String format,Map<String,Set<String>> origin,
									Map<String,FunctionResult> fnctsdb,
		                            Map<String,String> edgedb){
		try{
      		File file = new File(this.path);
      		FileOutputStream fp = new FileOutputStream(file);
      		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fp));
      		String head = format.equals("dot")?"dotheader":"graphmlheader";
      		String foot = format.equals("dot")?"dotfooter":"graphmlfooter";
      		bw.write(read("ExportFormat/"+head));

			for(String v : origin.keySet()){
				if(origin.get(v).isEmpty()){
					if(format.equals("dot"))
						bw.write("\""+v+"("+getParam(fnctsdb,v)+")\"\n");
				}else{
					for(String w : origin.get(v)){
						String value = edgedb.get(v+","+w);
						writeLine(format,bw,v,w,value,fnctsdb);
					}
				}
			}
      		bw.write(read("ExportFormat/"+foot));
      		bw.close();
    	}catch(IOException e){
      		e.printStackTrace();
    	}
	}

	public String read(String file){
		String res = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null){
				res +=line+"\n";
			}
			fr.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void writeLine(String format,BufferedWriter bw,String v,String w,String value,
		                     Map<String,FunctionResult> fnctsdb){

		String[] valueDetails = value.split(";\n");
		String[] edgeValue = getDifferentEdgeVal(valueDetails);
		String v_param = getParam(fnctsdb,v);
		String w_param = getParam(fnctsdb,w);
		String intParam = intFnctParam(v_param,w_param);
		String vertexLink ="";
		
		if(format.equals("dot")){
			vertexLink ="\""+v+"("+v_param+")\"->"+"\""+w+"("+w_param+")\"";
			writeDotLine(bw,edgeValue,vertexLink,intParam);
		}else if(format.equals("graphml")){
			writeGraphmlLine(bw,v,w,edgeValue,intParam);
		}

	}

	public void writeDotLine(BufferedWriter bw,String[] edgeValue,String vertexLink,
		                     String intParam){
		try{
			String line = "";
			for(int i = 0; i < edgeValue.length;i++){
				if(!edgeValue[i].equals("")){
					if(i == 1)
						line = vertexLink+"[label=\""+edgeValue[i]+intParam+"\n\",dir=both,style=solid]\n";
					else
						line = vertexLink+"[label=\""+edgeValue[i]+intParam+"\",style=dashed]\n";
					bw.write(line);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
	public void writeGraphmlLine(BufferedWriter bw,String v,String w,String[] edgeValue,String intParam){
		try{
        	bw.write("<node id=\""+v+"\">\n");
        	bw.write("<data key=\"d0\">"+v+"</data></node>\n");
        	
        	bw.write("<edge source=\""+v+"\" target=\""+w+"\">\n");
        	for(int i = 0; i < edgeValue.length;i++){
        		bw.write("<data key=\"d1\">\n"
        			                   +edgeValue[i]+intParam+
        			                "\n</data>\n");
        	}
        	bw.write("</edge>\n");
      	}catch(IOException e){
        	e.printStackTrace();
      	}
	}

	public String[] getDifferentEdgeVal(String[] valueDetails){
		String[] res = {"",""};
		for(int i = 0; i < valueDetails.length;i++){
			String[] edgeValue = valueDetails[i].split(",");
			switch(edgeValue[0]){
				case "++wr":
					res[0] +="wr,"+edgeValue[1]+";\n";
					break;
				case "++ww":
					res[0] +="ww,"+edgeValue[1]+";\n";
					break;
				case "++rw":
					res[0] +="rw,"+edgeValue[1]+";\n";
					break;
				case "--ww":
					res[1] +="ww,"+edgeValue[1]+";\n";
					break;
			}
		}
		return res;
	}

	public String getParam(Map<String,FunctionResult> fnctsdb,String table){
		String res = "";
		ArrayList<String> param = fnctsdb.get(table).parameter_list;
		for(int i = 0; i < param.size();i++){
			res += param.get(i);
			if(i < param.size()-1){
				res +=",";
			}
		}
		return res;
	}

	public String intFnctParam(String v_param,String w_param){
		String res ="";
		String[] v_params= v_param.split(",");
		String[] w_params=w_param.split(",");
		for(int i = 0; i < v_params.length;i++){
			for(int j = 0; j < w_params.length;j++){
				if(v_params[i].equals(w_params[j]))
					res += v_params[i]+",";
			}
		}
		return addPrim(res);
	}

	public String addPrim(String intParam){
		String res = "";
		String[] intRes = intParam.split(",");
		for(int i = 0; i < intRes.length;i++){
			if(!(intRes[i].equals("")))
				res += intRes[i]+" = "+intRes[i]+"\'\n";
		}
		return res;
	}

}