package sample.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import sample.Transaction;

import java.net.URL;
import java.util.ResourceBundle;

public class StyleController implements Initializable {
    private Controller mainController;
    private final String[] vertexElements = {"strokes", "background", "text"};
    private final String[] edgeElements = {"selected arrow", "other arrow"};
    private final String[] dependenciesTypes = {"ww", "rw", "wr"};

    @FXML
    private ChoiceBox<String> choiceBoxVertex;
    @FXML
    private RadioButton radioButtonWW,radioButtonWR, radioButtonRW;
    @FXML
    private ColorPicker colorPickerVertex, colorPickerEdge;
    @FXML
    private Button buttonVertex, buttonEdge;

    @FXML
    private void onClickButtonVertex() {
        String name = this.choiceBoxVertex.getValue();
        switch (name) {
            case "strokes":
                setColorStrokesVertex(this.colorPickerVertex.getValue());
                break;
            case "background":
                setColorBackgroundVertex(this.colorPickerVertex.getValue());
                break;
            case "text":
                setColorTextVertex(this.colorPickerVertex.getValue());
                break;
        }
    }

    /**
     * looks after the Transaction's rectangle stroke coloration
     *
     * @param color color whose we want to apply to all Transaction rectangle's stroke
     */
    private void setColorStrokesVertex(Color color) {
        System.out.println("strokes changes");
        for (Transaction transaction : mainController.transactions) {
            //transaction.getRectangle().setStroke(color);
            transaction.getRectangle().setStroke(Color.RED);
        }
    }

    /**
     * looks after the Transaction's rectangle background coloration
     *
     * @param color color whose we want to apply to all Transaction rectangle's background
     */
    private void setColorBackgroundVertex(Color color) {
        System.out.println("background changes");
        for (Transaction transaction : mainController.transactions) {
            System.out.println("text: " + transaction.getText().getText());
            transaction.getRectangle().setFill(color);
        }
    }

    /**
     * looks after the Transaction's text coloration
     *
     * @param color color whose we want to apply to all Transaction text
     */
    private void setColorTextVertex(Color color) {
        System.out.println("text changes");
        for (Transaction transaction : mainController.transactions) {
            transaction.getText().setFill(color);
        }
    }

    @FXML
    private void onClickButtonEdge() {
        /*TODO*/
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation de fenetre appareance");
        /* default value */
        this.choiceBoxVertex.setValue(this.vertexElements[0]);
        //this.choiceBoxEdge.setValue(this.edgeElements[0]);

        /* filling of the choice box */
        this.choiceBoxVertex.getItems().addAll(this.vertexElements);
        //this.choiceBoxEdge.getItems().addAll(this.edgeElements);
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }
}
