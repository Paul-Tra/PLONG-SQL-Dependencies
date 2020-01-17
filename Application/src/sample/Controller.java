package sample;

import com.sun.jdi.StringReference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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

import javax.lang.model.element.AnnotationValue;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import static sample.Main.primaryStage;

public class Controller {
    @FXML
    private ListView listView;
    @FXML private Label label1;
    private Consumer<String> consumer = e -> System.out.println(e);
    @FXML
    private StackPane stackPane;
    @FXML private AnchorPane anchorPane;
    @FXML
    private void doParsing(ActionEvent actionEvent) {
        if (!this.label1.getText().equals("")) {
            consumer.accept(label1.getText());
            Parser parser = new Parser(this.label1.getText());
            //fill_listView(parser);
            fillPane(parser.list_relation, parser.list_transaction);
        }
    }

    @FXML
    private void doClear(ActionEvent actionEvent) { // netoyage de la vue (effacement du grpahe)
        anchorPane.getChildren().clear();
    }

    private void fill_listView(Parser p) {
        NodeList nodeList = p.nodeList;
        for (Transaction transaction : p.list_transaction) {
            listView.getItems().add(transaction.id + " , " + transaction.nom);
        }
        for (Relation relation : p.list_relation) {
            listView.getItems().add( relation.source+ " , " +relation.destination + " , " + relation.nom);
        }
        fillPane(p.list_relation,p.list_transaction);
    }

    private void fillPane(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction) {
        ElementVisuel elementVisuel = new ElementVisuel(l_relation, l_transaction,anchorPane);
        for (Shape shape : elementVisuel.list_shape) {
            anchorPane.getChildren().add(shape);
        }
    }


    @FXML
    private void onOpenFile(ActionEvent actionEvent) {
        FileChooser fil_chooser = new FileChooser();
        // add filters file's extension
        fil_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("graphml files", "*.graphml"),
                new FileChooser.ExtensionFilter("dot files", "*.dot"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        File file = fil_chooser.showOpenDialog(primaryStage);
        if (file != null) {
            this.label1.setText(file.getAbsolutePath());
        }
    }
}
