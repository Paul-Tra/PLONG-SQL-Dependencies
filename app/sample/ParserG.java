package sample;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

// use for .gogol file parsing
public class ParserG {
    Consumer<String> consumer = e -> System.out.println(e);
    private final String EXTANSION = ".gogol";
    private final String[] list_tags = {"Relation", "SRC", "DST"};
    private final String[] list_attributes = {"ID", "SRC", "DST"};
    private ArrayList<String> list_lines;
    public ParserG(String path) {
        //check if the file have the extansion .gogol
        if (!path.contains(EXTANSION)) {
            consumer.accept("the file extansion is not correct ! ");
            return;
        }
        list_lines = new ArrayList<>();
        setFileLines(list_lines,path);
        //this.file = file;
        /*String id = "ww;ORDERLINE(*).* ";
        String src = "neworder";
        String dst = "neworder";
        ArrayList<String[]> res = getRelationLines(list_lines, id, src, dst);
        if (res.size() == 0) {
            consumer.accept("res taille 0");
        }
        if (res.get(0) == null) {
            consumer.accept("res 0 null");
        }

        */
    }

    //  fill the list_lines from the lines contained in the file
    private void setFileLines(ArrayList<String> list_lines,String path) {
        consumer.accept("in getFileLines");
        File file = new File(path);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null)
                list_lines.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // return the lines conrresponding to the research Relation tag
    // return null if the start or end line is not found
    protected ArrayList<String[]> getRelationLines(ArrayList<String> lines, String id, String source, String target) {
        consumer.accept("in getRelationLines");
        String opening = "<Relation";
        String closing = "</Relation>";
        String att_id = buildRecognitionAttribute("ID", id);
        String att_source = buildRecognitionAttribute("SRC", source);
        String att_target = buildRecognitionAttribute("DST", target);
        printElement(Arrays.asList(id, source, target));
        int start = lineFinding(lines, new ArrayList<>(Arrays.asList(opening, att_id, att_source, att_target)), 0);
        if (start == -1 ) {
            consumer.accept("the start index of the Relation's tag is not found");
            return null;
        }else consumer.accept("line :"+start+" : "+lines.get(start));
        int end = lineFinding(lines, new ArrayList<>(Arrays.asList(closing)), start);
        if (end == -1) {
            consumer.accept("the end index of the Relation's tag is not found");
            return null;
        }else consumer.accept("line :"+end+" : "+lines.get(end));
        ArrayList<String> relation_lines = selectionLines(lines, start, end);
        String[] src_tab = getLabelLines(relation_lines, "SRC");
        consumer.accept("src :");
        for (String s : src_tab) {
            consumer.accept(s);
        }
        String[] dst_tab = getLabelLines(relation_lines, "DST");
        consumer.accept("dst :");
        for (String s : dst_tab) {
            consumer.accept(s);
        }
        ArrayList<String[]> res = new ArrayList<>();
        res.addAll(Arrays.asList(src_tab, dst_tab));
        return res;
    }

    private void printElement(List<String> list) {
        for (String s : list) {
            consumer.accept(s);
        }
    }
    // return the lines corresponding to the research label
    // use for the labels whitout arguments as <SRC> ... </SRC> not <Relation ID= ...> ... </Relation>
    private String[] getLabelLines(ArrayList<String> lines, String label) {
        String opening = "<" + label + ">";
        String closing = "</" + label + ">";
        int start = lineFinding(lines, new ArrayList<>(Collections.singletonList(opening)), 0);
        if (!isCorrectValue(start, -1)) return null;
        int end = lineFinding(lines, new ArrayList<>(Collections.singletonList(closing)), 0);
        if(!isCorrectValue(end,-1)) return null;
        return compilLines(lines, start+1, end-1);
    }


    // select the lines between start and end
    // use to provide lines (not to be a return lines selection as compilLines )
    private ArrayList<String> selectionLines(ArrayList<String> lines, int start, int end) {
        if( !isCorrectValue(start,-1) || !isCorrectValue(end,-1)) return null;
        ArrayList<String> res = new ArrayList<>();
        consumer.accept("");
        consumer.accept(" selected lines :");
        for (int i = start + 1; i < end; i++) {
            res.add(lines.get(i));
            consumer.accept(lines.get(i));
        }
        consumer.accept("");
        return res;
    }
    //return the lines between start and end indexes
    // use for the return lines of tag
    private String[] compilLines(ArrayList<String> lines, int start, int end) {
        if (!isCorrectValue(start, -1) || !isCorrectValue(end, -1)) {
            consumer.accept("is not a correct value");
            return null;
        }
        // build the result from start to end lines
        String[] res = new String[end-start+1];
        for (int i = 0; i < res.length; i++) {
            res[i] = lines.get(start+i);
        }
        return res;
    }

    //check if the value is not a correct value ( default or -1)
    // check if the value is the same as default value as
    private boolean isCorrectValue(int value,int def) {
        if (value == def) {
            consumer.accept("The value has default value as : " + def);
            return false ;
        }
        if (value == -1) {
            consumer.accept("The value has negative value as : " + def);
            return false;
        }
        return true;
    }
    // search the line containing all the labels of the list in the line's list  and return its index
    private int lineFinding(ArrayList<String> lines,ArrayList<String> list_labels , int start) {
        int res = -1;
        for (int i = start; i < lines.size(); i++) {
            boolean isNotContained = false;
            for (String label : list_labels) {
                if (!lines.get(i).contains(label)) {
                    //consumer.accept("label : " + label + " not found");
                    isNotContained = true;
                    break;
                }
            }
            if (!isNotContained) {
                // consumer.accept("line : "+lines.get(i));
                // if we arrive here its only because all labels are found into the line i
                res = i;
                return res;
            }
        }
        return res;
    }
    // associate the attribute and searched word to correspond to the file syntax
    private String buildRecognitionAttribute(String attribute, String word) {
        String res = attribute + "=\"" + word + "\"";
        return res;
    }

    public ArrayList<String> getList_lines() {
        return list_lines;
    }



    // find and return the diferent lines which cause the dependance between the source and the target
    protected ArrayList<String> getDependanceLines(String dependance, String source, String target) {
        ArrayList<String> res = new ArrayList<>();


        return res;
    }

}
