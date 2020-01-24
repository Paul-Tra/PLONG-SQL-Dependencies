package sample;

import com.sun.jdi.StringReference;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
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
import org.w3c.dom.css.Rect;

import javax.lang.model.element.AnnotationValue;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import static sample.Main.primaryStage;

public class Controller {
    @FXML private ListView lv_data;
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
          //  lv_data.getItems().add("pppppppppppppppppp");
        }
    }

    @FXML
    private void doClear(ActionEvent actionEvent) { // netoyage de la vue (effacement du grpahe)
        anchorPane.getChildren().clear();
    }

    private void fill_listView(Parser p) {
        //lv_data = new ListView();

        //NodeList nodeList = p.nodeList;
        for (Transaction transaction : p.list_transaction) {
            lv_data.getItems().add(transaction.id + " , " + transaction.nom);
        }
        for (Relation relation : p.list_relation) {
            lv_data.getItems().add( relation.source+ " , " +relation.destination + " , " + relation.nom);
        }
        //fillPane(p.list_relation,p.list_transaction);
    }
    // rempli la listView "d'informaton" a partir d'une transaction (lorsqu'on clique dessus)
    public void fillData(Transaction t) {
        consumer.accept("DANS Fill data transaction ");
        lv_data.getItems().clear();
        lv_data.getItems().add("id :");
        lv_data.getItems().add(t.id);
        lv_data.getItems().add("nom :");
        lv_data.getItems().add(t.nom);

    }

    // rempli la listView "d'informaton" a partir d'une relation (lorsqu'on clique dessus)
    public void fillData(Relation r) {
        lv_data.getItems().clear();
        lv_data.getItems().add("source :");
        lv_data.getItems().add(r.source);
        lv_data.getItems().add("desstination :");
        lv_data.getItems().add(r.destination);
        lv_data.getItems().add("nom :");
        lv_data.getItems().add(r.nom);
    }

    private void fillPane(ArrayList<Relation> l_relation, ArrayList<Transaction> l_transaction) {
        ElementVisuel elementVisuel = new ElementVisuel(l_relation, l_transaction,anchorPane);
        for (Shape shape : elementVisuel.list_shape) {
            anchorPane.getChildren().add(shape);
            addHandlerShape(shape,elementVisuel);
        }
    }

    private void addHandlerShape(Shape shape,ElementVisuel elementVisuel) {
        shape.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                consumer.accept("dans le handle d'une shape");
                lv_data.setStyle("-fx-font-size : 10");
                if (Rectangle.class == shape.getClass()) {
                    consumer.accept("SAHPE DE TYPE TRANSACTION !!");
                    consumer.accept("shape est une rectangle !!");
                    Rectangle rectangle = (Rectangle) shape;
                    Transaction transaction = matchTransaction(rectangle, elementVisuel.list_transaction);
                    //fillData(transaction);
                    consumer.accept(" id de la transaction " + transaction.id);
                    fillData(transaction);
                } else if (Path.class == shape.getClass()) {
                    Path path = (Path) shape;
                    Relation relation = matchRelation(path, elementVisuel.list_relation);
                    consumer.accept("nom de la relation" + relation.nom);
                    fillData(relation);
                }
            }
        });
    }
    private Transaction matchTransaction(Rectangle rectangle,ArrayList<Transaction> list_transaction) {
        for (Transaction transaction : list_transaction) {
            if (rectangle.getAccessibleText().equals(transaction.id)) { // on a trouve la transcation correspondant au rectangle
                return transaction;
            }
        }
        return null;
    }
    private Relation matchRelation(Path path,ArrayList<Relation> list_relation) {
        for (Relation relation : list_relation) {
            if (path.getAccessibleText().equals(relation.nom)) { // on a trouve la transcation correspondant au rectangle
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
                new FileChooser.ExtensionFilter("dot files", "*.dot"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        File file = fil_chooser.showOpenDialog(primaryStage);
        if (file != null) {
            this.label1.setText(file.getAbsolutePath());
        }
    }
}
