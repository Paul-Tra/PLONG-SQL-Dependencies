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
    private void doClick(MouseEvent event) {
        // check if twe clicked on a data-cell's Relation and note Transaction
        if (lv_data.getAccessibleText() == null) {
            consumer.accept("we found a Transaction");
            // its a Transaction so no pop-up window about dependancy
            return;
        }
        for (int i = 0; i < lv_data.getItems().size(); i++) {
            if (lv_data.getItems().get(i).equals(lv_data.getSelectionModel().getSelectedItem())) {
                if (i >= Integer.valueOf(lv_data.getAccessibleText())) {
                    // the cell i looks after dependancy
                    consumer.accept("We found the cell number :"+i+" looks after dependancy");
                    // management of the pop-up window filling
                    String dependancy = lv_data.getSelectionModel().getSelectedItem().toString();
                    consumer.accept("our dependancy : " + dependancy);
                    fillPop_up(dependancy);
                }else{
                    consumer.accept("the clicked cell is not about dependancy");
                }
            }
        }
        //ArrayList<String> printing_lines = manageName();
    }

    // Fill the the pop-up window which shows the diferents lines causing the dependancy
    private void fillPop_up(String dependancy) {
        // before to fillwe have to clear
        listViewSource.getItems().clear();
        listViewTarget.getItems().clear();
        // file dependancies description parsing
        // management filling listView source
        listViewSource.getItems().add(dependancy);
        // management filling listView target
        listViewTarget.getItems().add(dependancy);

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
            Parser parser = new Parser(this.label1.getText());
            fillPane(parser.list_relation, parser.list_transaction);
        }
    }

    @FXML
    private void doClear(ActionEvent actionEvent) { // netoyage de la vue (effacement du grpahe)
        anchorPane2.getChildren().clear();
        listViewSource.getItems().clear();
        listViewTarget.getItems().clear();
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
    // recovering the lines available to be print in the element description
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
