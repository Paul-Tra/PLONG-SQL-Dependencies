package sample.Parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.function.Consumer;

public class GraphmlParser {
    private Consumer<String> consumer = e -> System.out.println(e);
    private File file;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private NodeList nodeList;
    private HashMap<Integer, String> transactionMap = new HashMap<>();
    private HashMap<Integer, String[]> relationMap = new HashMap<>();

    public GraphmlParser(String path) {
        file = new File(path);
        nodeList = null;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(file);
        } catch (Exception e) {
            consumer.accept(" e message : " + e.getMessage());
            e.printStackTrace();
        }
        try {
            document.getDocumentElement().normalize();
            documentAnalysis();
        } catch (NullPointerException e) {
            System.out.println("The generated document is null");
        }
    }


    /**
     * recovers all Transactions and Relations
     */
    private void documentAnalysis() {
        // node == Transaction
        nodeList = document.getElementsByTagName("node");
        transactionRecovering();
        // edge == Relation
        nodeList = document.getElementsByTagName("edge");
        relationRecovering();
    }

    /**
     * recovers all Relations by filling the Relation's map
     */
    private void relationRecovering() {
        for (int i = 0; i <nodeList.getLength() ; i++) {
            Element e = (Element) nodeList.item(i);
            String source = e.getAttribute("source");
            String target = e.getAttribute("target");
            String name = e.getElementsByTagName("data").item(0).getTextContent();
            // recovering of the key value  ( d0, d1,...) :
            String key = getFirstDataKey(e);
            if (!isContained(source, target, name, key)) {
                relationMap.put(relationMap.size(), new String[]{source, target, name, key});
            }
        }
    }

    /**
     * Extracts the first key of data's tag of an Element
     * @param e Element from where we want to extract the key
     * @return the value of the key
     */
    private String getFirstDataKey(Element e) {
        e.getElementsByTagName("data").item(0).normalize();
        NodeList n = e.getElementsByTagName("data");
        Element element = (Element) n.item(0);
        return element.getAttribute("key");
    }

    /**
     * recovers all Transactions by filling the Transaction's map
     */
    private void transactionRecovering() {
        for (int i = 0; i < nodeList.getLength() ; i++) {
            Element e = (Element) nodeList.item(i);
            String id = e.getAttribute("id");
            if (!isContained(id)) {
                transactionMap.put(transactionMap.size(), id);
                consumer.accept("id: "+id);
            }
        }
    }

    /**
     * Checks if the Transaction's map already contains the searched id
     * @param id searched id in the Transaction's map
     * @return  if the map contains the id or not
     */
    private boolean isContained(String id) {
        for (int i = 0; i < transactionMap.size(); i++) {
            if (transactionMap.get(i).equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the Relation's map already contains a Relation describing
     * by the four parameters
     * @param source    name of the source Transaction of the Relation
     * @param target    name of the target Transaction of the Relation
     * @param name       name of the Relation
     * @param key       key of the Relation
     * @return  if the map contains the Relation or not
     */
    private boolean isContained(String source, String target, String name, String key) {
        for (int i = 0; i < relationMap.size(); i++) {
            String tab[] = relationMap.get(i);
            if (tab.length != 4) {
                // 4: required number of parameter that identify a Relation as
                // the function's parameters
                return true; // if it is contained, we will not add it so no issues
            }
            if (tab[0].equals(source) && tab[1].equals(target) && tab[2].equals(name) &&
                    tab[3].equals(key) ) {
                return true;
            }
        }
        return false;
    }

    public HashMap<Integer, String> getTransactionMap() {
        return transactionMap;
    }

    public HashMap<Integer, String[]> getRelationMap() {
        return relationMap;
    }
}
