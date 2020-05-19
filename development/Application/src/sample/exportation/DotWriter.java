package sample.exportation;

import sample.Model.Relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DotWriter {
    private String path;
    private ArrayList<Relation> relations;

    private final String tagLabel = "label";
    private final String tagStyle = "style";
    private final String tagColor = "color";

    private final String highlightColor = "red";
    private final String basicColor = "black";
    private String selectedDependencyType;
    private final String head = "digraph G{\n" +
            "\trankdir=LR\n" +
            "\tsize=\"8,5\"\n" +
            "\tnodesep=1.0\n" +
            "\tnode[shape=box style=rounded peripheries=2]";
    private final String foot = "}";



    public DotWriter(String path, ArrayList<Relation> relations,String selectedDependencyType) {
        this.path = path;
        this.relations = relations;
        this.selectedDependencyType = selectedDependencyType;
        writeFile();
    }

    /**
     * looks after the filling of a .dot file from a list of relations
     */
    private void writeFile() {
        if (!this.path.endsWith(".dot")) {
            System.out.println("bad file extension");
            System.out.println("extension needed : .dot ");
            return;
        }
        File f = new File(this.path);
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            fillBufferWriterFromRelation(bw);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * fill a bufferedWriter with edge from the relation's list
     *
     * @param bw    BufferedWriter where we put our lines
     * @throws IOException exception relative to write function of BufferedWriter
     */
    private void fillBufferWriterFromRelation(BufferedWriter bw) throws IOException {
        bw.write(this.head+"\n");
        for (Relation relation : this.relations) {
            String line = buildLineFromRelation(relation);
            bw.write(line+"\n");
        }
        bw.write(this.foot);
    }

    /**
     * builds all the string elements which have to be plug in a line
     * and put them in a single string
     *
     * @param relation the relation from which we have to build a line
     * @return the line composed of an edge (=Relation)
     */
    private String buildLineFromRelation(Relation relation) {
        String line;
        String source = "\"" + relation.getSource().getId().trim() + "\"";
        String target = "\"" + relation.getTarget().getId().trim() + "\"";
        String label = "\"" + relation.getName().trim() + "\"";
        boolean dashed = !(relation.getArrow().getStrokeDashArray().isEmpty());
        if (isHighlightedRelation(relation)) {
            line = buildLineFromElements(source, target, label, dashed, this.highlightColor);
        } else {
            line = buildLineFromElements(source, target, label, dashed, this.basicColor);
        }
        return line;
    }

    /**
     * builds a line from several string elements
     *
     * @param source    name of the source of an edge
     * @param target    name of the target of an edge
     * @param label     name of the edge
     * @param dashed    if the edge have a dashed style or not
     * @param color     the color of the edge
     * @return  a string containing all elements for an in in DOT language
     */
    private String buildLineFromElements(String source, String target, String label,
                                         boolean dashed, String color) {
        String line;
        String style = dashed ? "dashed" : "solid";
        line = source + "->" + target
                + "[ "
                + this.tagLabel + " = " + label + ", "
                + this.tagStyle + " = " + style + ", "
                + this.tagColor + " = " + color
                + " ]";
        return line;
    }




    /**
     * says if the relation have to be highlight in comparison to the others
     *
     * @param relation the relation whose we want to know if we have to bring it to light
     * @return if the relation contains the searched selected dependency type or not
     */
    private boolean isHighlightedRelation(Relation relation) {
        if (this.selectedDependencyType == null || this.selectedDependencyType.isBlank()
                || this.selectedDependencyType.isEmpty()) {
            return false;
        }else{
            return relation.getName().contains(this.selectedDependencyType);
        }
    }
}
