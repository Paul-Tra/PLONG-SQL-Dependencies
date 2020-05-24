package sample.style;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import sample.control.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class StyleController implements Initializable {
    private Controller mainController;
    private final String[] vertexElements = {"strokes", "background", "text"};
    private final String[] edgeElements = {"selected arrow", "other arrow"};

    @FXML
    private ChoiceBox<String> choiceBoxVertex,choiceBoxEdge;
    @FXML
    private ColorPicker colorPickerVertex, colorPickerEdge;

    @FXML
    private void onClickButtonVertex() {
        String name = this.choiceBoxVertex.getValue();
        switch (name) {
            case "strokes":
                this.mainController.style.setStrokeColor(this.colorPickerVertex.getValue());
                break;
            case "background":
                this.mainController.style.setBackgroundColor(this.colorPickerVertex.getValue());
                break;
            case "text":
                this.mainController.style.setTextColor(this.colorPickerVertex.getValue());
                break;
        }
        this.mainController.colorTransactions();
    }

    @FXML
    private void onRadioButtonNO() {
        this.mainController.style.setPattern("");
        this.mainController.colorRelations();
    }

    @FXML
    private void onRadioButtonRW() {
        this.mainController.style.setPattern("rw");
        this.mainController.colorRelations();
    }

    @FXML
    private void onRadioButtonWW() {
        this.mainController.style.setPattern("ww");
        this.mainController.colorRelations();
    }

    @FXML
    private void onRadioButtonWR() {
        this.mainController.style.setPattern("wr");
        this.mainController.colorRelations();
    }

    @FXML
    private void onClickButtonEdge() {
        String name = this.choiceBoxEdge.getValue();
        switch (name) {
            case "selected arrow":
                this.mainController.style.setSelectedDependencyColor(this.colorPickerEdge.getValue());
                break;
            case "other arrow":
                this.mainController.style.setClassicDependencyColor(this.colorPickerEdge.getValue());
                break;
        }
        this.mainController.colorRelations();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* default value */
        this.choiceBoxVertex.setValue(this.vertexElements[0]);
        this.choiceBoxEdge.setValue(this.edgeElements[0]);

        /* filling of the choice box */
        this.choiceBoxVertex.getItems().addAll(this.vertexElements);
        this.choiceBoxEdge.getItems().addAll(this.edgeElements);
    }

    /**
     * Affect a main controller whose manages the principal window
     * @param mainController    main controller whose manages the principal window
     */
    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }
}
