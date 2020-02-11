package sample;

import com.sun.jdi.StringReference;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.Rect;

import javax.lang.model.element.AnnotationValue;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import static sample.Main.primaryStage;

public class Controller {
    private final double deltaY = 1.1; //
    private Consumer<String> consumer = e -> System.out.println(e);
    // borderPane left area
    @FXML private ListView lv_data;
    @FXML private Label label1;
    // borderPane center area
    @FXML private HBox hBox;
    @FXML private ListView listViewSource;
    @FXML private ListView listViewTarget;
    @FXML private AnchorPane anchorPane3;
    @FXML private AnchorPane anchorPane2;
    @FXML private ScrollPane scrollPane;

    @FXML
    private void graphClick() {
        changeVisibilityPopUpWindow(true);
    }
    @FXML
    private void doClick(MouseEvent event) {
        // check if the data-cell clicked is a Relation or not
        if (lv_data.getAccessibleText() == null) {
            consumer.accept("we don't find a Relation ");
            // its a Transaction or nothing so no pop-up window about dependancy
            doClearPopUpWindow(); // do clear if we had something in the pop-up before
            changeVisibilityPopUpWindow(true); // hide the pop-up window
            return;
        }
        for (int i = 0; i < lv_data.getItems().size(); i++) {
            if (lv_data.getItems().get(i).equals(lv_data.getSelectionModel().getSelectedItem())) {
                if (i >= Integer.valueOf(lv_data.getAccessibleText())) {
                    // the cell i looks after dependancy
                    consumer.accept("We found the cell number : "+i+" looks after dependancy");
                    // management of the pop-up window filling
                    String id = lv_data.getSelectionModel().getSelectedItem().toString();
                    // the clicked cell will be the ID of the dependancy
                    consumer.accept("id : "+id+"FIN");
                    String source = getItemValue("source");
                    consumer.accept("source : "+source+"FIN");
                    String target = getItemValue("destination");
                    consumer.accept("target : "+ target+"FIN");
                    ParserG parserG = new ParserG("./src/dependences.gogol");
                    ArrayList<String[]> list = parserG.getRelationLines(parserG.getList_lines(),id,source,target);
                    String Id = "ww,BID(*).*";
                    String src = "StoreBId";
                    String dst = "StoreBId";
                   // ArrayList<String[]> list = parserG.getRelationLines(parserG.getList_lines(),Id,src,dst);

                    //String dependancy = lv_data.getSelectionModel().getSelectedItem().toString();
                    //consumer.accept("our dependancy : " + dependancy);
                    fillPop_up(list.get(0),list.get(1));
                }else{
                    consumer.accept("the clicked cell is not about dependancy");
                    doClearPopUpWindow();
                    changeVisibilityPopUpWindow(true); // hide the pop-up
                }
            }
        }
        //ArrayList<String> printing_lines = manageName();
    }

    // use to find anything in the data's ListView
    // return the of the 'item' which is located at the next cell of the lv_data
    private String getItemValue(String item) {
        for (int i = 0; i < lv_data.getItems().size(); i++) {
            if (lv_data.getItems().get(i).getClass() == String.class) {
                String s = (String) lv_data.getItems().get(i);
                if (s.contains(item)) {
                    //we found the cell of the item
                    return (String) lv_data.getItems().get(i+1);
                }
            }
        }
        return null;
    }
    private void changeVisibilityPopUpWindow(boolean visible) {
        if (visible) anchorPane3.setVisible(false);
        else anchorPane3.setVisible(true);
    }
    // llokks after the clearing of listViews contained by the pop-up window
    private void doClearPopUpWindow() {
        consumer.accept("in doClearPopUpWindow");
        listViewSource.getItems().clear();
        listViewTarget.getItems().clear();
    }
    // Fill the the pop-up window which shows the diferents lines causing the dependancy
    private void fillPop_up(String[] lines1, String[] lines2) {
        // before to fill we have to clear
        doClearPopUpWindow();
        changeVisibilityPopUpWindow(false); // set the pop-up visible
        // file dependancies description parsing
        // management filling listView source
        for (String s : lines1) {
            listViewSource.getItems().add(s);
        }
        // management filling listView target
        for (String s : lines2) {
            listViewTarget.getItems().add(s);
        }
    }
    // manage the scrolling and the zooming
    @FXML void doScroll(ScrollEvent event){
        if (event.isControlDown()) {
            //if we pressed ctrl , we zoom/dezoom
            doZoom(event);
            event.consume();
        }
        // else we just scroll into the pane
        // nothing to do (because we use scrollPane)
    }
    // look after the zooming/dezooming
    private void doZoom(ScrollEvent event){
        if (event.getDeltaY() == 0) {
            return;
        }
        double coef =deltaY;
        if (event.getDeltaY() < 0) {
            // if the getDeltaY -> dezooming
            coef = 1/deltaY;
        }
        anchorPane2.setScaleX(anchorPane2.getScaleX() * coef);
        anchorPane2.setScaleY(anchorPane2.getScaleY() * coef);
    }

    @FXML
    private void doParsing(ActionEvent actionEvent) {
        // look after the parsing of the .graphml file
        if (!this.label1.getText().equals("")) {
            doClearPopUpWindow();
            changeVisibilityPopUpWindow(true);
            Parser parser = new Parser(this.label1.getText());
            fillPane(parser.list_relation, parser.list_transaction);
        }
    }

    @FXML
    private void doClear(ActionEvent actionEvent) { // netoyage de la vue (effacement du grpahe)
        anchorPane2.getChildren().clear();
        doClearPopUpWindow();
        changeVisibilityPopUpWindow(true);
        lv_data.getItems().clear();
    }

    private void fill_listView(Parser p) {
        for (Transaction transaction : p.list_transaction) {
            lv_data.getItems().add(transaction.id + " , " + transaction.nom);
        }
        for (Relation relation : p.list_relation) {
            lv_data.getItems().add( relation.source+ " , " +relation.destination + " , " + relation.nom);
        }
    }

    // fill the data listView from a transaction (when we do a click )
    public void fillData(Transaction t) {
        lv_data.getItems().clear();
        lv_data.setAccessibleText(null); // use for the Relation's comparison
        lv_data.getItems().add("id :");
        lv_data.getItems().add(t.id);
        lv_data.getItems().add("nom :");
        String printingName = t.nom;
        //deletion of '\n'
        printingName = printingName.replace("\n", "");
        lv_data.getItems().add(printingName);
    }

    // fill the data listView from a relation (when we do a click )
    public void fillData(Relation r) {
        ArrayList<String> printing_lines = manageName(r.nom);
        lv_data.getItems().clear();
        lv_data.getItems().add("source :");
        lv_data.getItems().add(r.source);
        lv_data.getItems().add("destination :");
        lv_data.getItems().add(r.destination);
        lv_data.getItems().add("nom :");
        // put the numbre of cells which don't look after dependancies
        int cpt = lv_data.getItems().size();
        lv_data.setAccessibleText(String.valueOf(cpt));
        for (String line : printing_lines) {
            lv_data.getItems().add(line);
        }
        // add event to listView's cells which contain a line from printing_lines

    }
    // recovering the lines available to be print in the element description (data's ListView)
    public ArrayList<String>  manageName(String nom){
        ArrayList<String> printing_lines = new ArrayList<String>();
        String[] line_tokens = nom.split("\n");
        for (String line_token : line_tokens) {
            boolean ww = line_token.contains("ww");
            boolean wr = line_token.contains("wr");
            boolean rw = line_token.contains("rw");
            if (ww || wr || rw) {
                printing_lines.add(line_token);
            }
        }
        return  printing_lines;
    }


    // put the diferents views elements into the Pane
    private void fillPane(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction) {
        ElementVisuel elementVisuel = new ElementVisuel(l_relation, l_transaction,anchorPane2);
        for (Shape shape : elementVisuel.list_shape) {
            anchorPane2.getChildren().add(shape);
            //anchorPane.getChildren().add(shape);
            addHandlerShape(shape,elementVisuel);
        }
    }

    private void addHandlerShape(Shape shape,ElementVisuel elementVisuel) {
        shape.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // reduction of police size in the data's listview
                lv_data.setStyle("-fx-font-size : 10");
                if (Rectangle.class == shape.getClass()) {
                    Rectangle rectangle = (Rectangle) shape;
                    Transaction transaction = matchTransaction(rectangle, elementVisuel.list_transaction);
                    fillData(transaction);
                } else if (Path.class == shape.getClass()) {
                    Path path = (Path) shape;
                    Relation relation = matchRelation(path, elementVisuel.list_relation);
                    fillData(relation);
                }
            }
        });
    }
    private Transaction matchTransaction(Rectangle rectangle,ArrayList<Transaction> list_transaction) {
        for (Transaction transaction : list_transaction) {
            if (rectangle.getAccessibleText().equals(transaction.id)) { 
//we found the corresponding transaction from the rectangle
                return transaction;
            }
        }
        return null;
    }
    private Relation matchRelation(Path path,ArrayList<Relation> list_relation) {
        for (Relation relation : list_relation) {
            if (path.getAccessibleText().equals(relation.nom)) {
 // we found the corresponding relation
                return relation;
            }
        }
        return null;
    }

    @FXML
    private void onOpenFile(ActionEvent actionEvent) {
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension
        fil_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("graphml files", "*.graphml"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        File file = fil_chooser.showOpenDialog(primaryStage);
        if (file != null) {
            this.label1.setText(file.getAbsolutePath());
        }
    }
}
