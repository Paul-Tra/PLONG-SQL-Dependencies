package sample;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;


//TODO: gerer le cas des double boucle (voir pourquoi si plusieurs boucle sur 1 mm Node, 1 seul est representé dans notre schema)
public class Parser {
    Consumer<String> consumer = e -> System.out.println(e);
    File file;
    DocumentBuilderFactory documentBuilderFactory;
    DocumentBuilder documentBuilder;
    Document document;
    NodeList nodeList;
    ArrayList<Relation> list_relation;
    ArrayList<Transaction> list_transaction;
    public Parser(String path) {
        file = new File(path);
        nodeList = null;
        list_relation = new ArrayList<>();
        list_transaction = new ArrayList<>();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(file);
        } catch (Exception e) {
            consumer.accept(" e message : " + e.getMessage());
            e.printStackTrace();
        }
        document.getDocumentElement().normalize();
        traitementDocument();
    }

    // identifie les differents elements du document
    private void traitementDocument() {
        nodeList = document.getElementsByTagName("node");
        fillListTransction();
        nodeList = document.getElementsByTagName("edge");
        fillListRelation();
    }

    private void fillListRelation() {
        for (int i = 0; i <nodeList.getLength() ; i++) {
            Element e = (Element) nodeList.item(i);
            String source = e.getAttribute("source");
            String destination = e.getAttribute("target");
            String nom = e.getElementsByTagName("data").item(0).getTextContent();
            // add here the recovering of the key ( d0, d1,...)
            String key = getFirstDataKey(e);
            /*if (!estContenue(source,destination,nom )) {
                Relation r = new Relation(source, destination, nom);
                list_relation.add(r);
            }*/
            if (!isContained(source, destination, nom, key)) {
                Relation r = new Relation(source, destination, nom, key);
                list_relation.add(r);
            }
        }
    }
    // return the key type of a data tag
    private String getFirstDataKey(Element e) {
        e.getElementsByTagName("data").item(0).normalize();
        NodeList n = e.getElementsByTagName("data");
        Element element = (Element) n.item(0);
        return element.getAttribute("key");
    }
    // remplie la liste avec les transactions en gerant l'unicité des transactions
    private void fillListTransction() {
        for (int i = 0; i < nodeList.getLength() ; i++) {
            Element e = (Element) nodeList.item(i);
            if (!estContenue(e.getAttribute("id"))) {
                Transaction t = new Transaction(e.getAttribute("id"), e.getTextContent());
                list_transaction.add(t);
            }
        }
    }
    // renvoie TRUE si l' id a deja ete utilisé pour une elts de la liste de Transaction
    private boolean estContenue(String id) {
        for (Transaction transaction : list_transaction) {
            if (transaction.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean estContenue(String source, String destination, String nom) {
        for (Relation relation : list_relation) {
            if (relation.source.equals(source) && relation.destination.equals(destination) && relation.nom.equals(nom)) {
                return true;
            }
        }
        return false;
    }
    //check if a relation with the arguments already exist
    private boolean isContained(String source, String destination, String nom,String key) {
        for (Relation relation : list_relation) {
            if (relation.source.equals(source) && relation.destination.equals(destination) && relation.nom.equals(nom) && relation.key.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
