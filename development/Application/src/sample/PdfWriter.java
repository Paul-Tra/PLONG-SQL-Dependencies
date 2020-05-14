package sample;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PdfWriter {
    private static final String header = "tmp_";
    private String path;
    private String tempPath;
    private ArrayList<Relation> relations;
    private HashMap<Integer, String> relationMap;
    private PDDocument document;

    public PdfWriter(String path, ArrayList<Relation> relations) {
        this.path = path;
        this.relations = relations;
        this.document = new PDDocument();
        this.tempPath = this.header + this.path;
        this.tempPath.replaceFirst(".pdf", ".dot");
    }

    private void fillDocument(){

    }

    private void generateGraph() {
        ArrayList<Relation> newRelations = new ArrayList<>();
        for (int i = 0; i < this.relations.size(); i++) {
            newRelations.add(new Relation(this.relations.get(i).getSource(),
                    this.relations.get(i).getTarget(), Integer.toString(i),
                    Integer.toString(i)) );
        }
        // graph generation
        DotWriter dotWriter = new DotWriter(this.tempPath,newRelations,"");

    }

    private void createTable() throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        this.document.addPage(page);
        PDPageContentStream pcs = new PDPageContentStream(document, page);
        PDRectangle box = page.getMediaBox();
        final int rows = this.relations.size();
        //pcs.d
    }


    private int getMaxName() {
        int max =-1;
        for (Relation relation : relations) {
            if (relation.getName().length() > max) {
                max = relation.getName().length();
            }
        }
        return max;
    }

    private void fillRelationMap(){
        relations.forEach(relation -> {
            if (isContained(relation.getName()) ) {

            }
        });
    }

    private boolean isContained(String name){
        return this.relationMap.containsValue(name);
    }

}
