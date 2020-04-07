package sample.Parser;

import sample.Relation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class GogolParser {
    private Consumer<String> consumer = e -> System.out.println(e);
    private final String EXTENSION = ".gogol";
    private final String tagRelation = "Relation", tagSource = "SRC", tagTarget = "DST";
    private final String attributeId = "ID", attributeSource = "SRC",
            attributeTarget = "DST", attributeCondition = "CONDITION";
    private File file;
    private ArrayList<String> file_lines = new ArrayList<>();
    private ArrayList<Relation> relations;
    private HashMap<String[], ArrayList<String>[]> regularDependenciesMap = new HashMap<>();
    private HashMap<String[], ArrayList<String>[]> conditionalDependenciesMap = new HashMap<>();

    public GogolParser(String path, ArrayList<Relation> relations) {
        if (!path.contains(EXTENSION)) {
            consumer.accept("the file extension is not correct ! ");
            return;
        }
        this.file = new File(path);
        this.relations = relations;
        getFileLines();
        fillDependencies();
    }

    /**
     * Fills the lines causing the dependency into two list
     *
     * @param dependency  the name of the dependency
     * @param source      the source's Transaction of the dependency
     * @param target      the target's Transaction of the dependency
     * @param conditional if the dependency is regular or conditional
     * @param sourceLines list of lines causing the dependency from the source
     *                    whose we have to fill
     * @param targetLines list of lines causing the dependency from the target
     *                    whose we have to fill
     */
    public void getDependencyLines(String dependency, String source, String target,
                                   boolean conditional, ArrayList<String> sourceLines,
                                   ArrayList<String> targetLines) {
        if (conditional) {
            getDependencyLinesFromMap(this.conditionalDependenciesMap, dependency,
                    source, target, sourceLines, targetLines);
        } else {
            getDependencyLinesFromMap(this.regularDependenciesMap, dependency,
                    source, target, sourceLines, targetLines);
        }

    }

    /**
     * Recovers a dependency's content in the map
     *
     * @param map         the map in which we want to find the dependency
     * @param dependency  the name of the dependency
     * @param source      the source's Transaction of the dependency
     * @param target      the target's Transaction of the dependency
     * @param sourceLines list of lines causing the dependency from the source
     *                    whose we have to fill
     * @param targetLines list of lines causing the dependency from the target
     *                    whose we have to fill
     */
    private void getDependencyLinesFromMap(HashMap<String[], ArrayList<String>[]> map,
                                           String dependency, String source, String target,
                                           ArrayList<String> sourceLines,
                                           ArrayList<String> targetLines) {
        Set keys = map.keySet();
        Iterator iterator = keys.iterator();
        String[] key;
        while (iterator.hasNext()) {
            key = (String[]) iterator.next();
            if (key[0].equals(dependency) && key[1].equals(source)
                    && key[2].equals(target)) {
                System.out.println("Give me a \"Hell Yeh\" !!");
                sourceLines.addAll(map.get(key)[0]);
                targetLines.addAll(map.get(key)[1]);
            }
        }
    }

    /**
     * fills the file_lines from the lines contained in the file
     */
    private void getFileLines() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.isEmpty()) {
                    continue;
                }
                file_lines.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fills the map representing the dependencies from all Relation's dependencies
     */
    private void fillDependencies() {
        for (Relation relation : this.relations) {
            ArrayList<String> dependencies = relation.getDependenciesLinesFromName();
            for (String dependency : dependencies) {
                String source = relation.getSource().getId();
                String target = relation.getTarget().getId();

                int index = indexRelation(dependency, source, target);
                String c = buildAttributeWithContent(this.attributeCondition, "True");
                boolean conditional = this.file_lines.get(index).contains(c);

                ArrayList<String> source_lines = new ArrayList<>();
                index = getTagLinesFromIndex(index, source_lines, this.tagSource);

                ArrayList<String> target_lines = new ArrayList<>();
                getTagLinesFromIndex(index, target_lines, this.tagTarget);

                fillDependenciesMap(dependency, source, target, source_lines,
                        target_lines, conditional);
            }
        }
    }

    /**
     * Finds the index from where the dependency begin in the file_lines
     * @param dependency    name of the dependency
     * @param source    name of the source Transaction of the dependency
     * @param target    name of the target Transaction of the dependency
     * @return  the index of the beginning of the dependency
     */
    private int indexRelation(String dependency, String source, String target) {
        for (int i = 0; i < file_lines.size(); i++) {
            String id = buildAttributeWithContent(this.attributeId, dependency);
            String s = buildAttributeWithContent(this.attributeSource, source);
            String t = buildAttributeWithContent(this.attributeTarget, target);
            if (file_lines.get(i).contains(id) && file_lines.get(i).contains(s)
                    && file_lines.get(i).contains(t)) {
                return i;
            }
        }
        return -1; // we did not find the searched dependency's Relation
    }

    /**
     * Fills the lines list by dependencies lines thanks to the index and the tag
     * @param index index where we began to search the tag lines
     * @param lines list where we plug the searched lines
     * @param tag   tag of the searched lines
     * @return the index of the last line red
     */
    private int getTagLinesFromIndex(int index, ArrayList<String> lines, String tag) {
        String startingTag = buildTag(tag, true);
        String closingTag = buildTag(tag, false);
        boolean start = false;
        while (!this.file_lines.get(index).contains(closingTag) && index < this.file_lines.size()) {
            if (start) {
                lines.add(this.file_lines.get(index));
            } else if (this.file_lines.get(index).contains(startingTag)) {
                start = true;
            }
            index++;
        }
        return index;
    }

    /**
     * Fills the appropriate dependencies's map with the information of a dependency
     * @param dependency    name of the dependency
     * @param source    name of the source Transaction of the dependency
     * @param target    name of the target Transaction of the dependency
     * @param sourceLines   list of the lines which cause the dependency in the source
     * @param targetLines   list of the lines which cause the dependency in the target
     * @param conditional   if the dependency is conditional orn ot
     */
    private void fillDependenciesMap(String dependency, String source,
                                     String target, ArrayList<String> sourceLines,
                                     ArrayList<String> targetLines,
                                     boolean conditional) {

        String[] key = new String[]{dependency, source, target};
        ArrayList<String>[] content = new ArrayList[2];
        content[0] = new ArrayList<>(sourceLines);
        content[1] = new ArrayList<>(targetLines);
        if (conditional) {
            this.conditionalDependenciesMap.put(key, content);
        } else {
            this.regularDependenciesMap.put(key, content);
        }
    }

    /**
     * associates the attribute and the searched content to correspond to the file syntax
     * @param attribute attribute whose we want to associate the content
     * @param content   content whose we want to add to the attribute
     * @return  the associated string made up by the attribute and the content
     */
    private String buildAttributeWithContent(String attribute, String content) {
        if (attribute.equals(attributeCondition)) {
            return attribute + "=" + content;
        }
        return attribute + "=\"" + content + "\"";
    }

    /**
     * created a tag from a tag name
     * @param tag   tag name
     * @param start if the created tag if an opening or a ending tag
     * @return
     */
    private String buildTag(String tag, boolean start) {
        return start ? "<" + tag : "</" + tag + ">";
    }
}
